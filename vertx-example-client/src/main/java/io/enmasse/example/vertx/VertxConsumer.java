package io.enmasse.example.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonClientOptions;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonReceiver;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
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
        ProtonClient client = ProtonClient.create(vertx);

        ProtonClientOptions options = new ProtonClientOptions();
        options.setSsl(true);

        // Remove the below in a production OpenShift cluster
        options.setHostnameVerificationAlgorithm("");
        options.setTrustAll(true);

        client.connect(options, appConfiguration.getHostname(), appConfiguration.getPort(), appConfiguration.getUsername(), appConfiguration.getPassword(), connection -> {
            if (connection.succeeded()) {
                log.info("Connected to {}:{}", appConfiguration.getHostname(), appConfiguration.getPort());
                ProtonConnection connectionHandle = connection.result();
                connectionHandle.open();

                ProtonReceiver receiver = connectionHandle.createReceiver(appConfiguration.getAddress());
                receiver.handler((protonDelivery, message) -> {
                    String payload = (String) ((AmqpValue)message.getBody()).getValue();
                    log.info("Received '{}'", payload);
                });
                receiver.openHandler(link -> {
                    if (link.succeeded()) {
                        log.info("Receiver attached to '{}'", appConfiguration.getAddress());
                        startPromise.complete();
                    } else {
                        log.info("Error attaching to {}", appConfiguration.getAddress(), link.cause());
                        startPromise.fail(link.cause());
                    }
                });
                receiver.closeHandler(link -> log.info("Receiver for {} closed", appConfiguration.getAddress()));
                receiver.open();
            } else {
                log.info("Error connecting to {}:{}: {}", appConfiguration.getHostname(), appConfiguration.getPort(), connection.cause().getMessage());
                startPromise.fail(connection.cause());
            }
        });
    }
}
