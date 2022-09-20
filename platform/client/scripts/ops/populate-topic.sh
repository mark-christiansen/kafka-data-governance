#!/bin/bash

BASE=$(dirname "$0")
cd ${BASE}
. ../env.sh

[[ -z "$1" ]] && { echo "Topic not specified" ; exit 1; }
TOPIC=$1

kafka-console-producer -bootstrap-server $BROKER_URL --compression-codec lz4 --property parse.key=true --property key.separator="|" \
  --property key.deserializer=org.apache.kafka.common.serialization.StringSerializer \
  --property key.deserializer=org.apache.kafka.common.serialization.ByteArraySerializer \
  --topic $TOPIC