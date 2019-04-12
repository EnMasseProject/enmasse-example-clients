package io.enmasse.example.quarkus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.amqp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventProcessor extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(EventProcessor.class);

    private final AppConfiguration appConfiguration;
    private final EventHandler eventHandler;

    public EventProcessor(AppConfiguration appConfiguration, EventHandler eventHandler) {
        this.appConfiguration = appConfiguration;
        this.eventHandler = eventHandler;
    }

    @Override
    public void start(Future<Void> startPromise) {
        AmqpClientOptions options = new AmqpClientOptions();
        options.setSsl(true);

        // Remove the below in a production OpenShift cluster
        options.setPemKeyCertOptions(new PemKeyCertOptions()
                .addCertValue(Buffer.buffer(appConfiguration.getCa())));

        options.setHost(appConfiguration.getHostname());
        options.setPort(appConfiguration.getPort());
        options.setUsername(appConfiguration.getUsername());
        options.setPassword(appConfiguration.getPassword());

        AmqpClient client = AmqpClient.create(vertx, options);
        client.connect(ar -> {
            if (ar.succeeded()) {
                log.info("Connected to {}:{}", appConfiguration.getHostname(), appConfiguration.getPort());
                AmqpConnection connection = ar.result();

                connection.createSender(appConfiguration.getControlAddress(), done -> {
                    if (done.succeeded()) {
                        AmqpSender sender = done.result();
                        connection.createReceiver(appConfiguration.getEventsAddress(), msg -> {
                            log.info("Received message: {}", msg.bodyAsString());
                            AmqpMessage controlMessage = eventHandler.process(msg);
                            if (controlMessage != null) {
                                sender.send(controlMessage);
                            }
                        }, rdone -> {
                            if (rdone.succeeded()) {
                                log.info("Receiver attached to '{}'", appConfiguration.getEventsAddress());
                                startPromise.complete();
                            } else {
                                log.info("Error attaching to {}", appConfiguration.getEventsAddress(), rdone.cause());
                                startPromise.fail(rdone.cause());
                            }
                        });
                    } else {
                        log.warn("Error attaching sender");
                        startPromise.fail(done.cause());
                    }
                });
            } else {
                log.info("Error connecting to {}:{}: {}", appConfiguration.getHostname(), appConfiguration.getPort(), ar.cause().getMessage());
                startPromise.fail(ar.cause());
            }
        });
    }
}
