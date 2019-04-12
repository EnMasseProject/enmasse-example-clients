package io.enmasse.example.quarkus;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Vertx;
import io.vertx.ext.amqp.AmqpMessage;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class Main {

    /*
    @Incoming("events")
    @Outgoing("control")
    @Acknowledgment(Acknowledgment.Mode.POST_PROCESSING)
    public Message<String> process(Message<String> event) {
        return Message.of("Thanks for the event: " + event.getPayload() + ". Now go do something useful");
    }
    */

    void onStart(@Observes StartupEvent ev) throws Exception {
        Vertx vertx = Vertx.vertx();
        EventHandler eventHandler = message -> AmqpMessage.create().withBody(message.bodyAsString()).build();
        vertx.deployVerticle(new EventProcessor(AppConfiguration.create(System.getenv()), eventHandler));
    }
}
