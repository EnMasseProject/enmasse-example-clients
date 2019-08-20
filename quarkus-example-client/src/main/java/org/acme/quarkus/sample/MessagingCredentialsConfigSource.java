package org.acme.quarkus.sample;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MessagingCredentialsConfigSource implements ConfigSource {
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
        try {
            Map<String, String> properties = new HashMap<>();
            properties.put("amqp-username", "@@serviceaccount@@");
            properties.put("amqp-password", readTokenFromFile());
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getValue(String key) {
        if ("amqp-username".equals(key)) {
            return "@@serviceaccount@@";
        } else if ("amqp-password".equals(key)) {
            try {
                return readTokenFromFile();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "messaging-credentials-config";
    }

    private static String readTokenFromFile() throws IOException {
        return new String(Files.readAllBytes(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/token")), StandardCharsets.UTF_8);
    }
}
