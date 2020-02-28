package org.acme.quarkus.sample;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MessagingCredentialsConfigSource implements ConfigSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingCredentialsConfigSource.class);

    private static final Set<String> propertyNames;

    static {
        propertyNames = new HashSet<>();
        propertyNames.add("amqp-username");
        propertyNames.add("amqp-password");
    }

    @Override
    public Set<String> getPropertyNames() {
        return propertyNames;
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("amqp-username", "@@serviceaccount@@");
        properties.put("amqp-password", readTokenFromFile());
        return properties;
    }

    @Override
    public String getValue(String key) {
        if ("amqp-username".equals(key)) {
            return "@@serviceaccount@@";
        } else if ("amqp-password".equals(key)) {
            return readTokenFromFile();
        }
        return null;
    }

    @Override
    public String getName() {
        return "messaging-credentials-config";
    }

    private static String readTokenFromFile(){
        try {
            return new String(Files.readAllBytes(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/token")), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.warn("Error reading SA token", e);
            return null;
        }
    }

}
