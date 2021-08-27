# Creating Red Hat Openshift Streams for Apache Kafka inventory demo cluster, topics and service account using terraform

## Steps

1. Clone this repo (if you haven't already done so) and change to terraform dir:
```bash
git clone https://github.com/merlante/quarkus-kafka-inventory-demo.git
cd quarkus-kafka-inventory-demo/terraform
```
2. Obtain a long-lived OpenShift Cluster Manager API Token:

Go to https://console.redhat.com/openshift/token/show, login, and click the "Copy to clipboard" icon in the "Your API token" section.

3. Set the long lived token as an env var, e.g.
```
export OFFLINE_TOKEN=<pasted_token_from_above> 
```
4. Initialise terraform:
```
terraform init
```
5. Apply terrafom plan to create service account, managed kafka cluster and required demo topics: orders, shipments, stock-levels, reserved-stock, available-stock:
```
terraform apply
```
## Debugging
If something does not complete due to a timeout or intermittent issue, ```terraform apply``` can be run again (since it is idempotent) and it will re-attempt any failed steps in order to reach the desired state.
