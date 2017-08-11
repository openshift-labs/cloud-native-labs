package com.redhat.cloudnative.gateway;


import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.circuitbreaker.CircuitBreaker;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.codec.BodyCodec;
import io.vertx.rxjava.ext.web.handler.CorsHandler;
import io.vertx.rxjava.servicediscovery.ServiceDiscovery;
import io.vertx.rxjava.servicediscovery.types.HttpEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Single;

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
        router.get("/health").handler(ctx -> ctx.response().end(new JsonObject().put("status", "UP").toString()));
        router.get("/api/products").handler(this::products);

        // Retrieve the service discovery
        ServiceDiscovery.create(vertx, discovery -> {

            // Catalog lookup
            Single<WebClient> catalogDiscoveryRequest = HttpEndpoint.rxGetWebClient(discovery,
                rec -> rec.getName().equals("catalog"));

            // Inventory lookup
            Single<WebClient> inventoryDiscoveryRequest = HttpEndpoint.rxGetWebClient(discovery,
                rec -> rec.getName().equals("inventory"));

            // Zip all 3 requests
            Single.zip(catalogDiscoveryRequest, inventoryDiscoveryRequest, (c, i) -> {
                // When everything is done
                catalog = c;
                inventory = i;
                return vertx.createHttpServer()
                    .requestHandler(router::accept)
                    .listen(config().getInteger("http.port", 8080));
            }).subscribe();
        });
    }

    private void products(RoutingContext rc) {
        // Retrieve catalog
        catalog.get("/api/catalog").as(BodyCodec.jsonArray()).rxSend()
            .map(resp -> {
                if (resp.statusCode() != 200) {
                    error("Invalid response from the catalog: " + resp.statusCode());
                }
                return resp.body();
            })
            .flatMap(products ->
                // For each item from the catalog, invoke the inventory service
                Observable.from(products)
                    .cast(JsonObject.class)
                    .flatMapSingle(product ->
                        circuit.rxExecuteCommandWithFallback(
                            future ->
                                inventory.get("/api/inventory/" + product.getString("itemId")).as(BodyCodec.jsonObject())
                                    .rxSend()
                                    .map(resp -> {
                                        if (resp.statusCode() != 200) {
                                            error("Invalid response from inventory: " + resp.statusCode());
                                        }
                                        return product.copy().put("availability", 
                                            new JsonObject().put("quantity", resp.body().getInteger("quantity")));
                                    })
                                    .subscribe(
                                        future::complete,
                                        future::fail),
                            error -> {
                                LOG.error("Inventory error for {}: {}", product.getString("itemId"), error.getMessage());
                                return product;
                            }
                        ))
                    .toList().toSingle()
            )
            .subscribe(
                list -> rc.response().end(Json.encodePrettily(list)),
                error -> rc.response().end(new JsonObject().put("error", error.getMessage()).toString())
            );
    }

    private void error(String message) {
        throw new RuntimeException(message);
    }
}
