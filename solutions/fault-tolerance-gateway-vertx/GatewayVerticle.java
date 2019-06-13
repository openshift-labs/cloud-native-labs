package com.redhat.cloudnative.gateway;

import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.client.predicate.ResponsePredicate;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.ArrayList;
import java.util.List;

public class GatewayVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(GatewayVerticle.class);

    private WebClient catalog;
    private WebClient inventory;
    private CircuitBreaker circuit;

    @Override
    public void start() {

        circuit = CircuitBreaker.create("inventory-circuit-breaker", vertx,
            new CircuitBreakerOptions()
                .setFallbackOnFailure(true)
                .setMaxFailures(3)
                .setResetTimeout(5000)
                .setTimeout(1000)
        );

        Router router = Router.router(vertx);
        router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET));
        router.get("/*").handler(StaticHandler.create("assets"));
        router.get("/health").handler(ctx -> ctx.response().end(new JsonObject().put("status", "UP").toString()));
        router.get("/api/products").handler(this::products);

        ServiceDiscovery.create(vertx, discovery -> {
            // Catalog lookup
            Single<WebClient> catalogDiscoveryRequest = HttpEndpoint.rxGetWebClient(discovery,
                rec -> rec.getName().equals("catalog"))
                .onErrorReturn(t -> WebClient.create(vertx, new WebClientOptions()
                    .setDefaultHost(System.getProperty("catalog.api.host", "localhost"))
                    .setDefaultPort(Integer.getInteger("catalog.api.port", 9000))));

            // Inventory lookup
            Single<WebClient> inventoryDiscoveryRequest = HttpEndpoint.rxGetWebClient(discovery,
                rec -> rec.getName().equals("inventory"))
                .onErrorReturn(t -> WebClient.create(vertx, new WebClientOptions()
                    .setDefaultHost(System.getProperty("inventory.api.host", "localhost"))
                    .setDefaultPort(Integer.getInteger("inventory.api.port", 9001))));

            // Zip all 3 requests
            Single.zip(catalogDiscoveryRequest, inventoryDiscoveryRequest, (c, i) -> {
                // When everything is done
                catalog = c;
                inventory = i;
                return vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(Integer.getInteger("http.port", 8080));
            }).subscribe();
        });
    }

    private void products(RoutingContext rc) {
        // Retrieve catalog
        catalog
            .get("/api/catalog")
            .expect(ResponsePredicate.SC_OK)
            .as(BodyCodec.jsonArray())
            .rxSend()
            .map(resp -> {
                // Map the response to a list of JSON object
                List<JsonObject> listOfProducts = new ArrayList<>();
                for (Object product : resp.body()) {
                    listOfProducts.add((JsonObject)product);
                }
                return listOfProducts;
            })
            .flatMap(products -> {
                    // For each item from the catalog, invoke the inventory service
                    // and create a JsonArray containing all the results
                    return Observable.fromIterable(products)
                        .flatMapSingle(product ->
                            circuit.rxExecuteCommandWithFallback(
                                future ->
                                    getAvailabilityFromInventory(product).subscribe(future::complete, future::fail),
                                error -> {
                                    LOG.warn("Inventory error for {}: status code {}", product.getString("itemId"), error);
                                    return product.copy();
                                })
                        )
                        .collect(JsonArray::new, JsonArray::add);
                }
            )
            .subscribe(
                list -> rc.response().end(list.encodePrettily()),
                error -> rc.response().setStatusCode(500).end(new JsonObject().put("error", error.getMessage()).toString())
            );
    }

    private Single<JsonObject> getAvailabilityFromInventory(JsonObject product) {
        // Retrieve the inventory for a given product
        return inventory
            .get("/api/inventory/" + product.getString("itemId"))
            .as(BodyCodec.jsonObject())
            .rxSend()
            .map(resp -> {
                if (resp.statusCode() != 200) {
                    LOG.warn("Inventory error for {}: status code {}",
                        product.getString("itemId"), resp.statusCode());
                    return product.copy();
                }
                return product.copy().put("availability",
                    new JsonObject().put("quantity", resp.body().getInteger("quantity")));
            });
    }
}
