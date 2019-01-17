package io.enmasse.example.vertx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfiguration {
    private static final Logger log = LoggerFactory.getLogger(AppConfiguration.class);
    private final String hostname;
    private final int port;
    private final String username;
    private final String password;
    private final String address;

    public AppConfiguration(String hostname, int port, String username, String password, String address) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.address = address;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public static AppConfiguration create() throws Exception {
        log.info("Loading configuration from properties");
        return fromProperties();
    }

    public static AppConfiguration fromProperties() throws Exception {
        Properties properties = loadProperties("config.properties");
        return new AppConfiguration(
                properties.getProperty("hostname"),
                Integer.parseInt(properties.getProperty("port")),
                properties.getProperty("username"),
                properties.getProperty("password"),
                properties.getProperty("address"));
    }

    private static Properties loadProperties(String resource) throws IOException {
        Properties properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(resource);
        properties.load(stream);
        return properties;
    }

    public String getAddress() {
        return address;
    }
}
