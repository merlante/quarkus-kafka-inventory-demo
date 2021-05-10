#!/bin/bash
  
# Usage ./create_kafka.sh <KAFKA_NAME> [topic]...

# This script creates a Red Hat Openshift on Apache Kafka cluster and specified topics.
# Pre-existing clusters will be reused (not deleted and recreated), but topics of the same
# name as specified will be deleted and recreated.
# Run this script with the rhoas cli installed on the PATH.

if [ "$#" -lt 1 ]
then
  echo "Usage ./create_kafka.sh <KAFKA_NAME> [topic]..."
  exit 1
fi

KAFKA_NAME=$1
TOPICS="${@:2}"

rhoas logout # In case we are logged in with SSO as the wrong user
rhoas login

if [ $? -ne 0 ]
then
  echo "rhoas login failed. Can't do anything."
  exit 1
fi

if ! rhoas kafka use "$KAFKA_NAME" &> /dev/null # cluster already exists
then
  rhoas kafka create "$KAFKA_NAME" &> /dev/null

  if [ $? -ne 0 ]
  then
    echo "Could not create the kafka cluster."
    exit 1
  fi

  echo "Kafka cluster being provisioned."

  KAFKA_STATUS=""
  CLUSTER_POLLING_RETRIES=50 # polls every 8 seconds
  COUNTER=0
  while [ "$KAFKA_STATUS" != "ready" ] && [ "$COUNTER" -lt "$CLUSTER_POLLING_RETRIES" ]; do
    sleep 8

    KAFKA_STATUS=$(rhoas kafka describe "$KAFKA_NAME" | jq -r .status)
    (( COUNTER=COUNTER+1 ))
  done

  if [ "$KAFKA_STATUS" != "ready" ]
  then
    echo "Kafka cluster taking too long to provision. Bombing out."
    exit 1
  else
    echo "Kafka cluster now ready."
  fi

else
  echo "Kafka cluster already exists -- going to reuse it."
fi

for TOPIC in $TOPICS
do
  # Ensure any pre-existing topic is deleted.
  rhoas kafka topic delete -y "$TOPIC" &> /dev/null

	rhoas kafka topic create "$TOPIC" > /dev/null

	if [ $? -ne 0 ]
  then
    echo "rhoas kafka topic create failed on ${TOPIC}. Bombing out."
    exit 1
  fi
done

