# EnMasse Client Examples

This repository contains examples you can use as base for your client code to access EnMasse. Any
client supporting the standard protocols used by EnMasse (AMQP, MQTT etc), so these clients should
only be considered an example of the different ways you can retrieve endpoint information and access
the endpoints automatically.

The examples assume that you have already setup EnMasse on a Kubernetes or OpenShift cluster.

There are currently 2 Java-based clients:

* vertx-example-client - Vert.X based client configured using a properties file, shows how to access EnMasse externally
* jms-example-client - JMS-based configured to read AddressSpace info and use service account for authentication.

Both examples come with resources that you deploy to provision messaging.
