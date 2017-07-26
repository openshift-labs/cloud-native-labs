package com.redhat.cloudnative.gateway;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
//import io.vertx.ext.web.client.HttpResponse;
//import io.vertx.ext.web.client.WebClient;
//import rx.Single;

import java.util.*;

public class GatewayVerticle extends AbstractVerticle {
    private HttpClient client;
    private String inventoryUrl, catalogUrl;

    @Override
    public void start(Future<Void> future) {
        inventoryUrl = config().getString("inventory.url", "http://inventory:8080");
        catalogUrl = config().getString("catalog.url", "http://catalog:8080");
        client = vertx.createHttpClient();
        Router router = Router.router(vertx);

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
        client.getAbs(catalogUrl + "/api/catalog", response -> {
            if (response.statusCode() == 200) {
                response.bodyHandler(productBuff -> {
                    JsonArray products = new JsonArray(productBuff);
                    // for each product, retrieve inventory
                    // client.getAbs(inventoryUrl + "/api/inventory/33243", response -> {}

                    rc.response().end(Json.encodePrettily(products));
                });
            } else {
                rc.response().end("{\"error\": \"Catalog service failed: r" + response.statusMessage() + "\"}");
            }
        }).end();
    }
}
