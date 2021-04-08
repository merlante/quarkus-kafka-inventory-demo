#!/bin/bash

# Usage ./run_demo_apps.sh <DEMO_PROJECT> <BOOTSTRAP_SERVERS> <KAFKA_CLIENT_ID> <KAFKA_CLIENT_SECRET>  <KAFKA_TOKEN_ENDPOINT_URI>

# Run this script with kafkacat, jq and oc available and oc logged into your cluster with permissions to create
# a project. DEMO_PROJECT will be deleted if it already exists. Also ensure that the "demo_apps.json" file
# is in the CWD. This script will create the project and deploy the apps of the demo to that project.

if [ "$#" -ne 5 ]
then
  echo "Usage ./run_demo.sh <DEMO_PROJECT> <BOOTSTRAP_SERVERS> <KAFKA_CLIENT_ID> <KAFKA_CLIENT_SECRET> <KAFKA_TOKEN_ENDPOINT_URI>"
  exit 1
fi

DEMO_PROJECT=$1
BOOTSTRAP_SERVERS=$2
CLIENT_ID=$3
CLIENT_SECRET=$4
TOKEN_ENDPOINT_URI=$5

echo "Running some checks..."

if [[ ! -f demo_apps.json ]]
then
    echo "demo_apps.json needs to exist in the current working directory."
    exit 1
fi

which jq > /dev/null

if [ $? -ne 0 ]
then
  echo "You need jq installed and on your PATH to run this demo."
  exit 1
fi

jq -e . demo_apps.json >/dev/null 2>&1

if [ $? -ne 0 ]
then
    echo "Error: demo_apps.json is not a valid json file."
    exit 1
fi

which kafkacat > /dev/null

if [ $? -ne 0 ]
then
  echo "You need kafkacat installed and on your PATH to run this demo."
  exit 1
fi

kafkacat -b "$BOOTSTRAP_SERVERS" -L -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN  \
         -X sasl.username="$CLIENT_ID" -X sasl.password="$CLIENT_SECRET" &> /dev/null

if [ $? -ne 0 ]
then
  echo "Error: Could not verify connection to the kafka cluster with supplied parameters."
  exit 1
fi

which oc > /dev/null

if [ $? -ne 0 ]
then
  echo "You need oc installed and on your PATH to run this demo."
  exit 1
fi

oc whoami &> /dev/null

if [ $? -ne 0 ]
then
  echo "You need to be logged into your demo Openshift cluster with oc to run this demo."
  exit 1
fi

echo "Cleaning up old project (if needed)..."

if oc project "$DEMO_PROJECT" &> /dev/null
then
  oc delete namespace "$DEMO_PROJECT" --wait=true &> /dev/null

  if [ $? -ne 0 ]
  then
    echo "Error: Couldn't delete \"${DEMO_PROJECT}\" demo project. User has insufficient access?"
    exit 1
  fi
fi

echo "Creating new demo project \"${DEMO_PROJECT}\"..."

oc new-project "$DEMO_PROJECT" &> /dev/null

if [ $? -ne 0 ]
then
  echo "Error: Couldn't create \"${DEMO_PROJECT}\" demo project. User has insufficient access?"
  exit 1
fi

echo "Deploying apps..."

for app in $(jq -c '.[]' demo_apps.json)
do
  docker_image=$(jq -r '."docker-image"' <<<"$app")
  required_topics=$(jq -r '."required-topics"' <<<"$app")

  echo "  Ensuring required topics are created for ${docker_image}"

  for topic in ${required_topics//,/ }
  do
    kafkacat -b "$BOOTSTRAP_SERVERS" -C \
             -X security.protocol=SASL_SSL \
             -X sasl.mechanisms=PLAIN \
             -X sasl.username="$CLIENT_ID" \
             -X sasl.password="$CLIENT_SECRET" \
             -e -t "$topic" &> /dev/null

    if [ $? -ne 0 ]
      then
        echo "Error: Required topic \"${topic}\" does not exist. Exiting."
        exit 1
    fi
  done

  echo "  Creating app in project."

  oc new-app --namespace "${DEMO_PROJECT}" --docker-image="${docker_image}" \
             -e BOOTSTRAP_SERVERS="${BOOTSTRAP_SERVERS}" \
             -e CLIENT_ID="${CLIENT_ID}" \
             -e CLIENT_SECRET="${CLIENT_SECRET}" \
             -e TOKEN_ENDPOINT_URI="${TOKEN_ENDPOINT_URI}" > /dev/null

  if [ $? -ne 0 ]
  then
    echo "Error: Something went wrong creating app from image. Exiting."
    exit 1
  fi

done

echo -e "All done.\n"

echo "Waiting for deployments to complete..."
oc wait --for=condition=available --timeout=60s --all deployments
echo -e "Done.\n"

echo "Openshift cluster project:"
oc project
echo
echo "Openshift pods:"
oc get pods

