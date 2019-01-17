package io.enmasse.example.jms;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.JmsQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String [] args) throws Exception {
        AppConfiguration appConfiguration = AppConfiguration.create(System.getenv());

        JmsConnectionFactory connectionFactory = new JmsConnectionFactory();
        connectionFactory.setRemoteURI(String.format("amqp://%s:%d", appConfiguration.getHostname(), appConfiguration.getPort()));
        connectionFactory.setUsername(appConfiguration.getUsername());
        connectionFactory.setPassword(appConfiguration.getPassword());

        JmsQueue destination = new JmsQueue(appConfiguration.getAddress());
        destination.setAddress(appConfiguration.getAddress());

        JMSConsumer consumer = new JMSConsumer(connectionFactory, destination);
        JMSProducer producer = new JMSProducer(connectionFactory, destination);

        Executor executor = Executors.newFixedThreadPool(2);
        executor.execute(consumer);
        executor.execute(producer);
    }
}
