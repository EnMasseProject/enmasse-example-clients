package io.enmasse.example.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.amqp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class VertxProducer extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(VertxProducer.class);
    private final AppConfiguration appConfiguration;
    private final int timerInterval = 2000;
    private final AtomicLong counter = new AtomicLong(0);

    public VertxProducer(AppConfiguration appConfiguration) {
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

                connectionHandle.createSender(appConfiguration.getAddress(), done -> {
                    if (done.succeeded()) {
                        log.info("Sender attached to '{}'", appConfiguration.getAddress());
                        AmqpSender sender = done.result();
                        startPromise.complete();
                        vertx.setTimer(timerInterval, id -> sendNext(sender));
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

    private void sendNext(AmqpSender sender) {
        AmqpMessage message = AmqpMessage.create().withBody("Hello " + counter.incrementAndGet()).build();
        sender.send(message);
        vertx.setTimer(timerInterval, id -> sendNext(sender));
    }
}
