# quarkus-kafka-inventory-demo project

There are currently 2 parts to this project:
1. Scripts to deploy the demo components. These are in the scripts directory. 
2. A quarkus app that generates data and sets up the demo conditions.

The idea is that the scripts will establish the following:
* That a Red Hat OpenShift Streams for Apache Kafka cluster has been created.
* That an Openshift project on a designated cluster is created.
* That Kafka topic processing demo apps are installed and running on the Openshift cluster and connecting to the Kafka cluster.
* They may also then kick off this quarkus app to connect to this setup and generate simulated inventory data representing indicative inventory management scenarios and the patternfly dashboard app (https://github.com/merlante/patternfly-inventory-demo-dashboard).

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Demo architecture

![RHOSAK solution_ Inventory management - Demo architecture](https://user-images.githubusercontent.com/1330712/117438331-533da180-af29-11eb-8b8d-9a995ac836c6.png)

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/quarkus-kafka-inventory-demo-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.

## Related guides

- RESTEasy JAX-RS ([guide](https://quarkus.io/guides/rest-json)): REST endpoint framework implementing JAX-RS and more

## Provided examples

### RESTEasy JAX-RS example

REST is easy peasy with this Hello World RESTEasy resource.

[Related guide section...](https://quarkus.io/guides/getting-started#the-jax-rs-resources)
