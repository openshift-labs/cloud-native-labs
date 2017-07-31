package com.redhat.cloudnative.gateway;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.*;
import io.vertx.core.http.*;
import io.vertx.core.json.*;
import io.vertx.ext.web.*;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.List;
import java.util.stream.Collectors;

public class GatewayVerticle extends AbstractVerticle {
    private HttpClient client;

    @Override
    public void start(Future<Void> future) {
        client = vertx.createHttpClient();

        Router router = Router.router(vertx);
        router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET));
        router.get("/health").handler(ctx -> ctx.response().end(new JsonObject().put("status", "UP").toString()));
        router.get("/api/products").handler(this::products);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(config().getInteger("http.port", 8080), result -> {
                    if (result.succeeded()) {
                        future.complete();
                    } else {
                        future.fail(result.cause());
                    }
                });
    }

    private void products(RoutingContext rc) {
        client.getAbs(config().getString("catalog.url", "http://catalog:8080") + "/api/catalog", resp -> {
            if (resp.statusCode() == 200) {
                resp.bodyHandler(buff -> {
                    JsonArray products = new JsonArray(buff);
                    List<Future> inventory = products.stream()
                            .map(product -> inventory((JsonObject) product))
                            .collect(Collectors.toList());

                    CompositeFuture.join(inventory).setHandler(ar -> {
                        rc.response().end(Json.encodePrettily(products));
                    });
                });
            } else {
                rc.response().end(new JsonObject().put("error", "catalog: " + resp.statusMessage()).toString());
            }
        }).end();
    }

    private Future<Void> inventory(JsonObject product) {
        Future<Void> inventoryFuture = Future.future();
        String baseUrl = config().getString("inventory.url", "http://inventory:8080") + "/api/inventory/";

        CircuitBreakerOptions options = new CircuitBreakerOptions()
                .setMaxFailures(5)
                .setTimeout(1000);

        CircuitBreaker breaker = CircuitBreaker.create("inventory", vertx, options);

        breaker.execute(future -> {
            client.getAbs(baseUrl + product.getString("itemId"),
                    resp -> {
                        if (resp.statusCode() == 200) {
                            resp.bodyHandler(buff -> {
                                product.put("availability",
                                        new JsonObject().put("quantity", new JsonObject(buff).getInteger("quantity")));
                                inventoryFuture.complete();
                                future.complete();
                            });
                        } else {
                            inventoryFuture.fail(resp.statusMessage());
                            future.fail(resp.statusMessage());
                        }
                    }).end();
        }).setHandler(ar -> {
            if (ar.failed()) {
                product.put("availability", new JsonObject().put("quantity", -1));
                inventoryFuture.fail(ar.cause().getMessage());
            }
        });

        return inventoryFuture;
    }
}
