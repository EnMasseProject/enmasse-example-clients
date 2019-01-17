package io.enmasse.example.jms;

import io.fabric8.openshift.client.OpenShiftConfig;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;
import io.swagger.client.Configuration;
import io.swagger.client.api.AddressspacesApi;
import io.swagger.client.model.IoEnmasseV1beta1AddressSpace;
import io.swagger.client.model.IoEnmasseV1beta1AddressSpaceStatusEndpointStatuses;
import io.swagger.client.model.IoEnmasseV1beta1AddressSpaceStatusServicePorts;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

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

    public String getAddress() {
        return address;
    }

    public static AppConfiguration create(Map<String, String> env) throws Exception {
        String addressSpaceName = env.getOrDefault("ADDRESS_SPACE", "jms-example");

        OpenShiftConfig openShiftconfig = new OpenShiftConfigBuilder().build();

        String url = openShiftconfig.getMasterUrl();
        String basePath = url.substring(0, url.length() - 1);

        Configuration.getDefaultApiClient().setBasePath(basePath);
        Configuration.getDefaultApiClient().setAccessToken(openShiftconfig.getOauthToken());
        Configuration.getDefaultApiClient().setDebugging(true);

        ResteasyClient client = new ResteasyClientBuilder()
                .disableTrustManager()
                .build();
        Configuration.getDefaultApiClient().setHttpClient(client);


        AddressspacesApi addressspacesApi = new AddressspacesApi(Configuration.getDefaultApiClient());

        IoEnmasseV1beta1AddressSpace addressSpace = addressspacesApi.readEnmasseV1alpha1NamespacedAddressSpace(openShiftconfig.getNamespace(), addressSpaceName);
        for (IoEnmasseV1beta1AddressSpaceStatusEndpointStatuses endpointStatus : addressSpace.getStatus().getEndpointStatuses()) {
            if ("messaging".equals(endpointStatus.getName())) {
                return readAppConfiguration(env, openShiftconfig, endpointStatus);
            }
        }
        throw new RuntimeException("Unable to find endpoint 'messaging'");
    }

    private static AppConfiguration readAppConfiguration(Map<String, String> env, OpenShiftConfig openShiftConfig, IoEnmasseV1beta1AddressSpaceStatusEndpointStatuses endpointStatus) throws Exception {
        String hostname = endpointStatus.getServiceHost();
        Integer port = endpointStatus.getServicePorts().stream()
                .filter(externalPort -> "amqp".equals(externalPort.getName()))
                .map(IoEnmasseV1beta1AddressSpaceStatusServicePorts::getPort)
                .findAny().orElse(null);

        if (port == null) {
            throw new IllegalArgumentException("Unable to find port 'amqp' for endpoint");
        }

        String userName = "@@serviceaccount@@";
        String password = openShiftConfig.getOauthToken();
        String address = env.getOrDefault("ADDRESS", "myqueue");

        return new AppConfiguration(hostname, port, userName, password, address);
    }
}
