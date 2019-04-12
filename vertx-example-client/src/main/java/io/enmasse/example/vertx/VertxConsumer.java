package io.enmasse.example.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.amqp.AmqpClient;
import io.vertx.ext.amqp.AmqpClientOptions;
import io.vertx.ext.amqp.AmqpConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VertxConsumer extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(VertxConsumer.class);
    private final AppConfiguration appConfiguration;

    public VertxConsumer(AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
    }

    @Override
    public void start(Future<Void> startPromise) {
        AmqpClientOptions options = new AmqpClientOptions();
        options.setSsl(true);

        // Remove the below in a production OpenShift cluster
        options.setHostnameVerificationAlgorithm("");
        options.setTrustAll(true);
        options.setHost(appConfiguration.getHostname());
        options.setPort(appConfiguration.getPort());
        options.setUsername(appConfiguration.getUsername());
        options.setPassword(appConfiguration.getPassword());

        AmqpClient client = AmqpClient.create(vertx, options);
        client.connect(connection -> {
            if (connection.succeeded()) {
                log.info("Connected to {}:{}", appConfiguration.getHostname(), appConfiguration.getPort());
                AmqpConnection connectionHandle = connection.result();

                connectionHandle.createReceiver(appConfiguration.getAddress(),
                        msg -> log.info("Received '{}'", msg.bodyAsString()),
                        done -> {
                            if (done.succeeded()) {
                                log.info("Receiver attached to '{}'", appConfiguration.getAddress());
                                startPromise.complete();
                            } else {
                                log.info("Error attaching to {}", appConfiguration.getAddress(), done.cause());
                                startPromise.fail(done.cause());
                            }
                        });
            } else {
                log.info("Error connecting to {}:{}: {}", appConfiguration.getHostname(), appConfiguration.getPort(), connection.cause().getMessage());
                startPromise.fail(connection.cause());
            }
        });
    }
}
