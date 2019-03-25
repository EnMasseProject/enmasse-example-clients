package io.enmasse.example.quarkus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.proton.*;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.message.Message;
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
        ProtonClient client = ProtonClient.create(vertx);

        ProtonClientOptions options = new ProtonClientOptions();
        options.setSsl(true);
        options.setPemTrustOptions(new PemTrustOptions()
                .addCertValue(Buffer.buffer(appConfiguration.getCa())));
        client.connect(options, appConfiguration.getHostname(), appConfiguration.getPort(), appConfiguration.getUsername(), appConfiguration.getPassword(), connection -> {
            if (connection.succeeded()) {
                log.info("Connected to {}:{}", appConfiguration.getHostname(), appConfiguration.getPort());
                ProtonConnection connectionHandle = connection.result();
                connectionHandle.open();

                ProtonSender sender = connectionHandle.createSender(appConfiguration.getControlAddress());

                sender.closeHandler(link -> log.info("Sender to {} closed", appConfiguration.getControlAddress()));
                sender.openHandler(result -> {
                    if (result.succeeded()) {
                        log.info("Sender attached to control address '{}'", appConfiguration.getControlAddress());
                        ProtonReceiver receiver = connectionHandle.createReceiver(appConfiguration.getEventsAddress());
                        receiver.handler((protonDelivery, message) -> {
                            log.info("Received message: {}", message.getBody());
                            Message controlMessage = eventHandler.process(message);
                            if (controlMessage != null) {
                                sender.send(message);
                            }
                        });
                        receiver.openHandler(link -> {
                            if (link.succeeded()) {
                                log.info("Receiver attached to '{}'", appConfiguration.getEventsAddress());
                                startPromise.complete();
                            } else {
                                log.info("Error attaching to {}", appConfiguration.getEventsAddress(), link.cause());
                                startPromise.fail(link.cause());
                            }
                        });
                        receiver.closeHandler(link -> log.info("Receiver for {} closed", appConfiguration.getEventsAddress()));
                        receiver.open();
                    } else {
                        log.warn("Error attaching sender");
                        startPromise.fail(result.cause());
                    }
                });

                sender.open();
            } else {
                log.info("Error connecting to {}:{}: {}", appConfiguration.getHostname(), appConfiguration.getPort(), connection.cause().getMessage());
                startPromise.fail(connection.cause());
            }
        });
    }
}
