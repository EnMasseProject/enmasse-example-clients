# Quarkus example

This example clients demonstrates how you can use [Quarkus](https://quarkus.io/) and its reactive messaging API to connect to an EnMasse messaging service running on the same Kubernetes cluster as the Quarkus application. The example is based on the [amqp-quickstart](https://github.com/quarkusio/quarkus-quickstarts/tree/master/amqp-quickstart), modified to inject EnMasse resource configuration.

`src/main/resources/k8s` contains example YAML files for configuring an EnMasse AddressSpace,
Address and MessagingUser. Run the following command to create them (replace oc with kubectl if
you're using a different Kubernetes distribution):

NOTE: This requires [GraalVM](https://www.graalvm.org/) to be installed.

```
oc new-project myapp
oc apply -f src/main/resources/k8s/addressspace
oc apply -f src/main/resources/k8s/address
```

## Build and deploy (Kubernetes):

On Kubernetes, you need to push the image to a docker registry and reference it. You can adjust the
docker org of the image by setting `-Ddocker.org=<docker hub username>`.

```
mvn -Pnative -Dfabric8.mode=kubernetes -Dfabric8.build.strategy=docker -Ddocker.registry=docker.io package fabric8:build fabric8:push fabric8:resource fabric8:apply
```

## Build and deploy (OpenShift):

On OpenShift, you can build the application directly on the cluster:

```
mvn -Pnative -Dfabric8.mode=openshift -Dfabric8.build.strategy=docker package fabric8:build fabric8:resource fabric8:apply

# Get the route URL
export URL="http://$(oc get route quarkus-example-client -o jsonpath={.spec.host})"
```

You should be able to go to $URL/prices.html in your browser and see the prices getting updated.
