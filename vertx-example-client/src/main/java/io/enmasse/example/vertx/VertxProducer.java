package io.enmasse.example.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonClientOptions;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonSender;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.message.Message;
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

                ProtonSender sender = connectionHandle.createSender(appConfiguration.getAddress());
                sender.openHandler(link -> {
                    if (link.succeeded()) {
                        log.info("Sender attached to '{}'", appConfiguration.getAddress());
                        startPromise.complete();
                        vertx.setTimer(timerInterval, id -> sendNext(sender));
                    } else {
                        log.info("Error attaching to {}", appConfiguration.getAddress(), link.cause());
                        startPromise.fail(link.cause());
                    }
                });
                sender.open();
            } else {
                log.info("Error connecting to {}:{}: {}", appConfiguration.getHostname(), appConfiguration.getPort(), connection.cause().getMessage());
                startPromise.fail(connection.cause());
            }
        });
    }

    private void sendNext(ProtonSender sender) {
        Message message = Proton.message();
        message.setBody(new AmqpValue("Hello " + counter.incrementAndGet()));
        message.setAddress(appConfiguration.getAddress());
        sender.send(message);
        vertx.setTimer(timerInterval, id -> sendNext(sender));
    }
}
