# Quarkus example

This example clients demonstrates how you can connect to an EnMasse messaging service (or any
AMQP 1.0 based service in general) and send/recv messages using a Quarkus-based client. This client is intended to run on
a Kubernetes cluster.

`src/main/resources/k8s` contains example YAML files for configuring an EnMasse AddressSpace,
Address and MessagingUser. Run the following command to create them (replace `oc` with `kubectl` if
you're using a different Kubernetes distribution):

```
oc new-project myapp
oc apply -f src/main/resources/k8s
```

```
oc get addressspace vertx-example -o jsonpath={.status.endpointStatuses[?(@.name==\'messaging\')].externalHost}
```

## Building and running client

```
mvn package
mvn exec:java
```
