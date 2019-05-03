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

    private int count = 0;

    @Incoming("orders")
    @Outgoing("confirmations")
    public Message<String> process(Message<String> event) throws InterruptedException {
        Thread.sleep(1000);
        return Message.of("Thanks for the order: " + event.getPayload() + ". Now go do something useful");
    }

    public static void main(String[] args) {
        SeContainerInitializer.newInstance().initialize();
    }
}
