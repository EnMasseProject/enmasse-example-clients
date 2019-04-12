package io.enmasse.example.quarkus;

import io.vertx.ext.amqp.AmqpMessage;

public interface EventHandler {
    AmqpMessage process(AmqpMessage message);
}
