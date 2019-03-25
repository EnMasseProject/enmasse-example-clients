package io.enmasse.example.quarkus;

import org.apache.qpid.proton.message.Message;

public interface EventHandler {
    Message process(Message message);
}
