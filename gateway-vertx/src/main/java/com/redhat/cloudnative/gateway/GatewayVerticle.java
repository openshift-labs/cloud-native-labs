package com.redhat.cloudnative.gateway;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class GatewayVerticle extends AbstractVerticle {
    @Override
    public void start(Future<Void> future) {
        Router router = Router.router(vertx);

        router.get("/*").handler(StaticHandler.create("assets"));

        vertx.createHttpServer().requestHandler(router::accept)
            .listen(Integer.getInteger("http.port", 8080));
    }
}