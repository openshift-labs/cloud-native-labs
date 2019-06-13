package com.redhat.cloudnative.inventory;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class InventoryVerticle extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(InventoryVerticle.class);

  private JDBCClient inventoryClient;

  @Override
  public Completable rxStart() {

    Router router = Router.router(vertx);
    router.get("/*").handler(StaticHandler.create("assets"));
    router.get("/health").handler(ctx -> ctx.response().end(new JsonObject().put("status", "UP").toString()));
    router.get("/api/inventory/:itemId").handler(this::findQuantity);

    Single<HttpServer> serverSingle = vertx.createHttpServer()
        .requestHandler(router)
        .rxListen(Integer.getInteger("http.port", 8080));

    ConfigRetrieverOptions configOptions = new ConfigRetrieverOptions()
        .addStore(new ConfigStoreOptions()
            .setType("file")
            .setFormat("yaml")
            .setConfig(new JsonObject()
                .put("path", "config/app-config.yml")));
    ConfigRetriever retriever = ConfigRetriever.create(vertx, configOptions);
    Single<JsonObject> s = retriever.rxGetConfig();

    return s
        .flatMap(this::populateDatabase)
        .flatMap(rs -> serverSingle)
        .ignoreElement();
  }

  private void findQuantity(RoutingContext rc) {
    String itemId = rc.pathParam("itemId");
    inventoryClient.queryWithParams(
        "select \"QUANTITY\" from \"INVENTORY\" where \"ITEMID\"=?",
        new JsonArray().add(itemId),
        ar -> {
          if (ar.succeeded()) {
            ResultSet resultSet = ar.result();
            List<JsonObject> rows = resultSet.getRows();
            if (rows.size() == 1) {
              int quantity = rows.get(0).getInteger("QUANTITY");
              JsonObject body = new JsonObject()
                  .put("quantity", quantity)
                  .put("itemId", itemId);
              rc.response()
                  .putHeader("content-type", "application/json")
                  .end(body.encodePrettily());
            } else {
              rc.response().setStatusCode(404).end("Product " + itemId + " not found");
            }
          } else {
            LOG.error("Could not access database", ar);
            rc.fail(500, ar.cause());
          }
        });
  }

  private Single<ResultSet> populateDatabase(JsonObject config) {
    LOG.info("Will use database " + config.getValue("jdbcUrl"));
    inventoryClient = JDBCClient.createNonShared(vertx, config);
    String sql = "" +
        "drop table if exists INVENTORY;" +
        "create table \"INVENTORY\" (\"ITEMID\" varchar(32) PRIMARY KEY, \"QUANTITY\" int);" +
        "insert into \"INVENTORY\" (\"ITEMID\", \"QUANTITY\") values (329299, 35);" +
        "insert into \"INVENTORY\" (\"ITEMID\", \"QUANTITY\") values (329199, 12);" +
        "insert into \"INVENTORY\" (\"ITEMID\", \"QUANTITY\") values (165613, 45);" +
        "insert into \"INVENTORY\" (\"ITEMID\", \"QUANTITY\") values (165614, 87);" +
        "insert into \"INVENTORY\" (\"ITEMID\", \"QUANTITY\") values (165954, 43);" +
        "insert into \"INVENTORY\" (\"ITEMID\", \"QUANTITY\") values (444434, 32);" +
        "insert into \"INVENTORY\" (\"ITEMID\", \"QUANTITY\") values (444435, 53);";
    return inventoryClient.rxQuery(sql);
  }
}
