package io.enmasse.example.vertx;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String [] args) throws Exception {
        AppConfiguration appConfiguration = AppConfiguration.create();

        Vertx vertx = Vertx.vertx();
        Future<Void> producer = Future.future();
        Future<Void> consumer = Future.future();

        CompositeFuture starts = CompositeFuture.all(producer, consumer);
            vertx.deployVerticle(new VertxConsumer(appConfiguration), res1 -> {
            if (res1.succeeded()) {
                consumer.complete();
                vertx.deployVerticle(new VertxProducer(appConfiguration), res2 -> {
                    if (res2.succeeded()) {
                        producer.complete();
                    } else {
                        producer.fail(res2.cause());
                    }
                });
            } else {
                consumer.fail(res1.cause());
            }
        });
        starts.setHandler(result -> {
            if (result.succeeded()) {
                log.info("Client started successfully");
            } else {
                log.info("Error starting client: {}", result.cause().getMessage());
                System.exit(1);
            }
        });
    }

}
