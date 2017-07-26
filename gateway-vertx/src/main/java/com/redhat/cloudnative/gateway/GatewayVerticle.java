package com.redhat.cloudnative.gateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

public class GatewayVerticle extends AbstractVerticle {
    @Override
    public void start(Future<Void> future) {
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
        WebClient.create(vertx)
                .getAbs(config().getString("endpoint.catalog", "http://catalog:8080") + "/api/catalog")
                .send((AsyncResult<HttpResponse<Buffer>> ar) -> {
                    if (ar.succeeded()) {
                        rc.response().end(ar.result().bodyAsString());
                    } else {
                        rc.response().end(new JsonObject()
                                        .put("service", "catalog")
                                        .put("error", ar.cause().getMessage())
                                        .toString());
                    }
                });
    }
}
