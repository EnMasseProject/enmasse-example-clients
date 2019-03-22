package io.enmasse.example.quarkus;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.se.SeContainerInitializer;

@ApplicationScoped
public class Main {
    @Outgoing("myqueue")
    public PublisherBuilder<String> source() {
        return ReactiveStreams.of("hello", "world");
    }

    @Incoming("myqueue")
    public void sink(String word) {
        System.out.println(">> " + word);
    }

    public static void main(String[] args) {
        SeContainerInitializer.newInstance().initialize();
    }
}
