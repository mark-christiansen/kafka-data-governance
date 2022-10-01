#!/bin/bash

BASE=$(dirname "$0")
cd ${BASE}
. ../env.sh

[[ -z "$1" ]] && { echo "Rule topic not specified" ; exit 1; }
TOPIC=$1

[[ -z "$2" ]] && { echo "Rule key not specified" ; exit 1; }
RULE_KEY=$2

[[ -z "$3" ]] && { echo "Rule value not specified" ; exit 1; }
RULE_VALUE=$3

echo "Populating rule $RULE_KEY in rule topic $TOPIC"
echo "$RULE_KEY~$RULE_VALUE" | kafka-console-producer --bootstrap-server $BROKER_URL --topic $TOPIC --property parse.key=true --property key.separator=~ --compression-codec lz4 --property key.deserializer=org.apache.kafka.common.serialization.StringSerializer \
  --property key.deserializer=org.apache.kafka.common.serialization.ByteArraySerializer
echo "Successfully populated rule $RULE_KEY in rule topic $TOPIC"