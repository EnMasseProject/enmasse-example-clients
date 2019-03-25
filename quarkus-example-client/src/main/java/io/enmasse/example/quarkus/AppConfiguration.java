package io.enmasse.example.quarkus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class AppConfiguration {
    private static final Logger log = LoggerFactory.getLogger(AppConfiguration.class);
    private final String hostname;
    private final int port;
    private final String username;
    private final String password;
    private final String controlAddress;
    private final String eventsAddress;
    private final byte[] ca;

    public AppConfiguration(String hostname, int port, String username, String password, String controlAddress, String eventsAddress, byte[] ca) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.controlAddress = controlAddress;
        this.eventsAddress = eventsAddress;
        this.ca = ca;
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

    public String getControlAddress() {
        return controlAddress;
    }

    public String getEventsAddress() {
        return eventsAddress;
    }

    public byte[] getCa() {
        return ca;
    }

    public static AppConfiguration create(Map<String, String> env) throws Exception {
        String hostname = env.get("MESSAGING_HOST");
        Integer port = Integer.parseInt(env.get("MESSAGING_PORT"));
        String userName = "@@serviceaccount@@";
        String password = getOauthToken(env);
        String controlAddress = env.getOrDefault("CONTROL_ADDRESS", "control");
        String eventsAddress = env.getOrDefault("EVENTS_ADDRESS", "events");
        byte [] ca = getClusterCa(env);

        return new AppConfiguration(hostname, port, userName, password, controlAddress, eventsAddress, ca);
    }

    private static byte[] getClusterCa(Map<String, String> env) throws IOException {
        String caString = env.get("CA_CERT");
        if (caString != null) {
            return caString.getBytes(StandardCharsets.UTF_8);
        } else {
            return readCaFromFile();
        }
    }

    private static byte[] readCaFromFile() throws IOException {
        return Files.readAllBytes(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/ca.crt"));
    }

    private static String getOauthToken(Map<String, String> env) throws IOException {
        String token = env.get("TOKEN");
        if (token == null) {
            token = readTokenFromFile();
        }
        return token;
    }

    private static String readTokenFromFile() throws IOException {
        return new String(Files.readAllBytes(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/token")), StandardCharsets.UTF_8);
    }
}
