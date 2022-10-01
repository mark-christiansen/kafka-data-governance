#!/bin/bash

BASE=$(dirname "$0")
cd ${BASE}
. ../env.sh

[[ -z "$1" ]] && { echo "Rule topic not specified" ; exit 1; }
TOPIC=$1

RULES=("com.mycompany.kafka.model.Customer-firstName~{\"record\":\"com.mycompany.kafka.model.Customer\",\"field\":\"firstName\",\"type\":\"pattern-match\",\"regex\":\"^L.*$\"}")

echo "Populating rules in rule topic $TOPIC"
for RULE in ${RULES[@]}; do echo "$RULE"; done | kafka-console-producer --bootstrap-server $BROKER_URL --topic $TOPIC --property parse.key=true --property key.separator=~ --compression-codec lz4 --property key.deserializer=org.apache.kafka.common.serialization.StringSerializer \
  --property key.deserializer=org.apache.kafka.common.serialization.ByteArraySerializer
echo "Successfully populated rules in rule topic $TOPIC"