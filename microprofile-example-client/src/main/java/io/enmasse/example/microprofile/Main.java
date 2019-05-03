package io.enmasse.example.microprofile;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.se.SeContainerInitializer;

@ApplicationScoped
public class Main {

    private int count = 0;

    @Incoming("orders")
    @Outgoing("confirmations")
    public Message<String> process(Message<String> event) {
        return Message.of("Thanks for the order: " + event.getPayload() + ". Now go do something useful");
    }

    public static void main(String[] args) {
        SeContainerInitializer.newInstance().initialize();
    }
}
