package io.enmasse.example.jms;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class AppConfiguration {
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

    public String getAddress() {
        return address;
    }

    public static AppConfiguration create(Map<String, String> env) throws Exception {
        String hostname = env.get("MESSAGING_HOST");
        Integer port = Integer.parseInt(env.get("MESSAGING_PORT"));
        String userName = "@@serviceaccount@@";
        String password = getOauthToken(env);
        String address = env.getOrDefault("ADDRESS", "myqueue");

        return new AppConfiguration(hostname, port, userName, password, address);
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
