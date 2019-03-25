# Quarkus example

This example clients demonstrates how you can connect to an EnMasse messaging service running on the same Kubernetes cluster,
and send/recv messages. This client is not intended to run outside the Kubernetes cluster (see vertx-example-client for that), although it can be manipulated into doing so.

## Building 

```
mvn package -Pnative
```

## To instantiate the application

```
oc new-project myapp
oc apply -f src/main/resources/openshift
oc apply -f src/main/resources/k8s/addressspace
oc apply -f src/main/resources/k8s/address
oc start-build quarkus-example-client --from-dir=. --follow
```
