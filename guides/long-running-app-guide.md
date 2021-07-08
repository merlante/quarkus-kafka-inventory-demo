# Inventory demo as a long running observable app

This guide outlines how to install the inventory management demo as a long running app on Openshift.
The quarkus components of the app will generate, process and consume inventory management messages for as long as they 
are running. Metrics are exposed to prometheus, running on Openshift, which can be used to monitor the stability and 
performance of the app, as well as its connectivity with Red Hat Openshift Streams for Apache Kafka, over time. It is also 
possible to observe the effect of changes to the service, such as upgrades, on uptime as perceived by the app.

## Architecture view

![RHOSAK solution_ Inventory management - Long running app (1)](https://user-images.githubusercontent.com/1330712/124911141-57974100-dfe4-11eb-9aa1-3b3a80360f86.png)


## Prerequisites

1. You have a Red Hat Openshift for Apache Kafka managed cluster. (See: https://developers.redhat.com/products/red-hat-openshift-streams-for-apache-kafka/getting-started.)
2. rhoas cli is installed. (See https://github.com/redhat-developer/app-services-guides/tree/main/rhoas-cli.)
3. You have an Openshift cluster. (e.g. the Red Hat Developer Sandbox: https://developers.redhat.com/developer-sandbox.)
4. oc cli is installed. (See https://docs.openshift.com/container-platform/4.7/cli_reference/openshift_cli/getting-started-cli.html#installing-openshift-cli.)
5. jq tool is installed. (See https://stedolan.github.io/jq/.)
6. kafkacat tool is installed. (See https://github.com/edenhill/kafkacat.)

## Steps

1. Clone this repo to get the scripts:
```bash
git clone https://github.com/merlante/quarkus-kafka-inventory-demo.git
cd quarkus-kafka-inventory-demo
```
2. Create the managed Kafka cluster with the demo topics:
```bash
scripts/create_kafka.sh my-inventory-demo orders shipments stock-levels reserved-stock available-stock
```
(As part of the script, the rhoas cli will prompt you to login. If a kafka cluster with the supplied name,
i.e. my-inventory-demo, already exists, it will reuse it, but will delete and recreate the topics specified.)

| :warning: Note             |
|:---------------------------|
| For now, if you want to run this command again with a completely free slate, you'll have to manually delete internal available-stock-* topics as well. (This is a bug because the command should clear all state.)     |

3. Set BOOTSTRAP_SERVERS in your environment. This is the url exposed by the Kafka cluster that apps will connect to.

Copy the BOOTSTRAP_SERVERS var from the output of the create_kafka.sh script in the previous step, then export it:
```bash
export BOOTSTRAP_SERVERS=<what_you_copied_from_the_output>
```

4. Create a service account to be used by the demo apps (if you haven't already):
```bash
rhoas serviceaccount create
```
and follow the interactive prompt, choosing any 'name', the 'env' file format, and file location as '.env' the current
working directory (or wherever you want to run the demo commands from).
```
Example:

11:45 $ rhoas serviceaccount create
? Name: my-service-account
? Credentials file format: env
? Credentials file location: /Users/someuser/.env
? Description [optional]: [Enter 2 empty lines to finish]
? Description [optional]: 
Creating service account "my-service-account"
Service account "my-service-account" created successfully with ID "ad967b4e-445d-4780-8bf0-xxxxxxxxxxx".
Credentials saved to /Users/someuser/.env
```
You now have authentication credentials stored in .env that allows the apps to connect to your managed Kafka cluster.

5. Log in to your Openshift cluster using 'oc'.
With a browser, go to your Openshift Console page and click on the user account drop down on the top right of the screen (you should see your login name displayed). Click "Copy login command". In the new window that is spawned, click "Display Token". Copy the command line displayed under "Log in with this token" and paste it into the terminal.

e.g.
```bash
oc login --token=sha256~XXXXXXXXXXXXXXXXXXXXXXXXXXXXX --server=https://cluster1234.containers.cloud.company.com:3333
```
For more details, see: https://docs.openshift.com/container-platform/4.7/cli_reference/openshift_cli/getting-started-cli.html#cli-logging-in_cli-developer-commands

6. Deploy the kafka processing apps to a project on your Openshift cluster:
```bash
cd scripts
./run_demo_apps.sh kafka-inventory-demo $BOOTSTRAP_SERVERS $CLIENT_ID $CLIENT_SECRET https://identity.api.openshift.com/auth/realms/rhoas/protocol/openid-connect/token
```
where $CLIENT_ID and $CLIENT_SECRET are the two vars stored in .env. (TODO: supply from .env in a classy way!)

(This script installs any apps specified in scripts/demo_apps.json.)

WARNING: If the specified project, i.e. kafka-inventory-demo, already exists, it will delete it and recreate everything.

7. Deploy the inventory demo app to the same project on your Openshift cluster.

```bash
oc new-app --namespace kafka-inventory-demo --docker-image=quay.io/mmclaugh/quarkus-kafka-inventory-demo -e BOOTSTRAP_SERVERS="${BOOTSTRAP_SERVERS}" --env-file=../.env --env TOKEN_ENDPOINT_URI=https://identity.api.openshift.com/auth/realms/rhoas/protocol/openid-connect/token
```

8. Ensure that cluster monitoring for user defined projects is enabled (see https://quarkus.io/blog/micrometer-prometheus-openshift/).

Determine the Openshift cluster Server Version:
```bash
oc version
```
If the Server Version is 4.6 or greater, enable using:
```bash
oc apply -f ../openshift/cluster-monitoring-config.yaml -n openshift-monitoring
```
otherwise (4.5.X and lower), enable using:
```bash
oc apply -f ../openshift/cluster-monitoring-config_pre45.yaml -n openshift-monitoring
```

9. Add ServiceMonitor to indicate to prometheus which service to scrape:
```bash
oc apply -f ../openshift/service-monitor.yaml -n kafka-inventory-demo
```
This will tell prometheus to scrape services with the label "app-with-metrics: quarkus-kafka-inventory-demo" in the 
kafka-inventory-demo namespace on port 8080 and path /q/metrics.

10. Add "app-with-metrics: quarkus-kafka-inventory-demo" label to the quarkus-kafka-inventory-demo service. This 
    service/quarkus app is the only component of the inventory demo that exposes metrics.
```bash
oc label svc quarkus-kafka-inventory-demo app-with-metrics=quarkus-kafka-inventory-demo
```

11. To check if everything is working, go to the Openshift console in "</> Developer" mode, select "Monitoring". On the 
    monitoring page, select the "Metrics" tab. Click "Select query" and then "Custom query". Paste the following as the 
    query:
```promql
inventorydemo_orders_count_total
```

As the data points arrive, this query will graph the orders count as it continues to increment.

## Useful metrics

* Rate of change of the sum of round-trip times it takes for created orders to be produced to kafka and consumed back
  again. Basically, all prometheus gets is an ever increasing sum of round-trips. The sum should increase by 
  roughly the same low round-trip time for each order. The derivative should therefore be a flat line stuck to a low value.
  Bumps on the line indicate periods where one or more round-trips were longer.
```promql
deriv(inventorydemo_orders_roundtrip_seconds_sum[1m])
```

## Debugging

You can expose the metrics endpoint outside the cluster by adding a route with oc expose. This may be useful for 
debugging purposes.

1. Set project:
```bash
oc project kafka-inventory-demo
```

2. Expose the route:
```bash
oc expose svc/quarkus-kafka-inventory-demo -n kafka-inventory-demo
```

3. Get the external url:
```bash
oc get route.route.openshift.io/quarkus-kafka-inventory-demo -o json | jq -r '.spec.host'
```

3. Put the url from the previous step and put it into a browser adding /q/metrics as the path.
