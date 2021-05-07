# Kafka Inventory Demo project

This project contains the scaffolding to run an Inventory Management Demo using Red Hat Openshift Streams for Apache Kafka. It is intended to be used to validate the developer experience of using the service in conjunction with other open source tooling, as well as provide a runnable and extendable demo that can be used for a range of purposes. It is also intended to be used to help people understand how Apache Kafka works, how it can be used to solve real world problems and possibly also for regression and/or performance testing of the service.

## The Inventory Management "problem"

In the standard legacy ecommerce IT landscape, it is typical for there to be an ecommerce/order management system (OMS) and a warehouse management system (WMS). The OMS is the master of "Order", i.e. it tracks the full lifecycle of orders that have been placed and what status they are in. The WMS is the master of "Stock", i.e. what stock is physically located in the warehouse stock locations. In order to sell something on a website, the ecommere system must have a real-time view of available stock for product, or SKU (stock-keeping unit). In the simplest case, this is the total stock available in the warehouse minus the total stock that has already been "reserved" for fulfilling a previous order. (Just because there is 1 item of stock for a SKU remaining in the warehouse, doesn't mean you can keep selling the SKU until that item is dispatched -- you need one item reserved for each order.) The problem is that the OMS is the master of stock reservations and the WMS is the master of stock in the warehouse, so to do that subtraction across two systems in real-time is "hard".

In the worst case, the WMS will send a massive file to the ecommerce system/OMS by batch job once daily in the middle of the night. In this case, the reserved stock part of the equation will be accurate, but the stock levels in the warehouse will be up to 1 day out of date. What can happen in one day?
* New shipments of stock can arrive.
* Orders will be dispatched.
* Stock takes will reveal "shrinkage".

What is the cost of this?
* The online shop will sell items for which there is no stock remaining in the warehouse. (An uncomfortable phone call for customer service beckons.)
* The online shop will not sell items for which there is newly arrived stock in the warehouse. (This is inefficiency and a loss to the business.)

### What is the solution?

The solution is (surprise) retrofitting some Apache Kafka goodness into this legacy equation. By publishing orders, shipments and stock-levels from those two systems into those respective topics, a real-time view of stock can be made available to the ecommerce system, and nasty batch jobs can be phased out. 

KafkaStreams, which offers stream semantics such as "merge" and "join" is great for doing this on the fly arithmetic. Reactive messaging frameworks like Smallrye are perfect for other types of apps that are monitoring topics for particular events and performing particular actions.

As more events are emmitted onto Kafka topics, applications can orient themselves around kafka rather than more tightly coupled ESBs or calls directly into systems and databases. As such, this kind of demo in extendable into other aspects of order management, such as fraud, or updates to user profiles.

## What's in the box?

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
