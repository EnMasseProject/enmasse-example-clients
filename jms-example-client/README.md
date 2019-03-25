# Qpid JMS example

This example clients demonstrates how you can connect to an EnMasse messaging service running on the same Kubernetes cluster,
and send/recv messages. This client is not intended to run outside the Kubernetes cluster (see vertx-example-client for that), although it can be manipulated into doing so.

`src/main/resources/k8s` contains example YAML files for configuring an EnMasse AddressSpace,
Address and MessagingUser. Run the following command to create them (replace oc with kubectl if
you're using a different Kubernetes distribution):

```
oc new-project myapp
oc apply -f src/main/resources/k8s/addressspace
oc apply -f src/main/resources/k8s/address
```

## Building and running client

```
mvn clean package
```

## Build and deploy:

```
mvn -Dfabric8.mode=openshift package fabric8:build
mvn fabric8:resource fabric8:deploy
```
