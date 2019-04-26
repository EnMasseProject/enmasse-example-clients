package io.enmasse.example.microprofile;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.se.SeContainerInitializer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class Main {

    @Incoming("orders")
    @Outgoing("confirmations")
    public Message<String> process(Message<String> event) {
        return Message.of("Thanks for the order: " + event.getPayload() + ". Now go do something useful");
    }

    @Outgoing("orders")
    public PublisherBuilder<Message<String>> source() {
        return ReactiveStreams.of(Message.of("hello"), Message.of("with"), Message.of("SmallRye"), Message.of("reactive"), Message.of("message"));
    }

    @Incoming("confirmations")
    public CompletionStage<Integer> sink(Message<String> event) {
        System.out.println("Sink: " + event.getPayload());
        CompletableFuture<Integer> promise = new CompletableFuture<>();
        promise.complete(1);
        return promise;
    }


    public static void main(String[] args) {
        SeContainerInitializer.newInstance().initialize();
    }
}
