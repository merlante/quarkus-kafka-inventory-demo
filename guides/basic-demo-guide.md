# Running the Inventory Management "hello world" demo
This guide gives the steps for getting the basic inventory demo setup working. It's possible to modify and extend this 
demo in various ways, but the intention is to keep this guide up to date, so people can get started and see something
working.

This opinionated setup installs the kafka processing demo apps[^1] on an Openshift cluster. With some tweaking, the apps could 
equally be installed locally on docker. For now, the app that creates the test data and serves topic data to the 
dashboard, and the app that serves the dashboard itself, is run locally on docker.
For reasons of scriptability, the rhoas cli is used in preference to the console.

[^1] These are apps that do nothing but produce and consume kafka messages, or perform some logic based on a kafka
message.

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

3. Set BOOTSTRAP_SERVERS in your environment. This is the url exposed by the Kafka cluster that apps will connect to.

Copy the BOOTSTRAP_SERVERS var from the output of the create_kafka.sh script in the previous step, then export it:
```bash
export BOOTSTRAP_SERVERS=<what_you_copied_from_the_output>
```

4. Create a service account to be used by the demo apps:
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

5. Start the quarkus-kafka-inventory-demo app, which generates simulated orders, stock-levels, and, after a time period, 
   translates orders into shipments:
```bash
docker run --rm --env-file .env --env BOOTSTRAP_SERVERS=$BOOTSTRAP_SERVERS --env TOKEN_ENDPOINT_URI=https://identity.api.openshift.com/auth/realms/rhoas/protocol/openid-connect/token -p8080:8080 quay.io/mmclaugh/quarkus-kafka-inventory-demo
```
(If you want to tweak something, and since you've already cloned the repo, you could also build and run the code from
./mvnw -- see https://github.com/merlante/quarkus-kafka-inventory-demo#running-the-application-in-dev-mode, with the
same vars and .env referenced. In this case, you need to also run the dashboard server, below, locally as well.)

6. Start the patternfly-inventory-demo-dashboard (which is the app that serves the single page app dashboard):
```bash
docker run --rm -d -p 9100:8080 quay.io/mmclaugh/patternfly-inventory-demo-dashboard
```
(To run locally, with the demo app, above, see https://github.com/merlante/patternfly-inventory-demo-dashboard#development-quick-start.)

7. Open a browser and go to: http://localhost:9100. You should see the dashboard, with orders and shipments accumulating,
and stock "SKUs" starting to appear as stock level bars.
 
8. Log in to your Openshift cluster using 'oc':
```bash
oc login
```
For more details, see: https://docs.openshift.com/container-platform/4.7/cli_reference/openshift_cli/getting-started-cli.html#cli-logging-in_cli-developer-commands
  
9. Deploy the kafka processing apps to a project on your Openshift cluster:
```bash
scripts/run_demo_apps.sh kafka-inventory-demo $BOOTSTRAP_SERVERS $CLIENT_ID $CLIENT_SECRET https://identity.api.openshift.com/auth/realms/rhoas/protocol/openid-connect/token
```
where $CLIENT_ID and $CLIENT_SECRET are the two vars stored in .env. (TODO: supply from .env in a classy way!)

(This script installs any apps specified in scripts/demo_apps.json.)

WARNING: If the specified project, i.e. kafka-inventory-demo, already exists, it will delete it and recreate everything.
