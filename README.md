# Kafka Data Governance Demo

## Summary

This is a demo project to demonstrate ways of identifying and collection PII violations by Kafka applications. PII data
should not be stored in Kafka topics in clear text. One way of encrypting this data is to use the 
[Confluent E2E Encryption Accelerator](https://www.confluent.io/confluent-accelerators/#end-to-end-encryption-and-tokenization).
It isn't enough to provide tools to developers to use, companies should be monitoring their data and enforcing the
encryption of PII data in Kafka applications. That's one of the things this project is set out to demonstrate. This
project contains the following applications:

* Data Protection library that contains rules-based code used to detect plaintext PII data and Kafka interceptors
* Data Protection Kafka Streams Application usd to monitor for plaintext PII data
* Example Kafka Producer
* Example Kafka Streams Application
* Example Kafka Consumer
* Platform folder with docker-compose file to launch Kafka and example applications

## Requirements

This project requires the following tools:

* [OpenJDK](https://download.java.net/openjdk/jdk11/ri/openjdk-11+28_linux-x64_bin.tar.gz)
* [Maven](https://dlcdn.apache.org/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.zip)
* [docker & docker-compose](https://desktop.docker.com/mac/main/amd64/Docker.dmg?utm_source=docker&utm_medium=webreferral&utm_campaign=dd-smartbutton&utm_location=module)

## Build Project

Make sure that [Maven](https://dlcdn.apache.org/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.zip) is in your path
and that [docker](https://desktop.docker.com/mac/main/amd64/Docker.dmg?utm_source=docker&utm_medium=webreferral&utm_campaign=dd-smartbutton&utm_location=module)
is running. To build the project, go to the base directory and run `mvn install`. This will compile the Java classes for
`data-protection-streams`, `java-kafka-consumer`, `java-kafka-producer`, and `java-kafka-streams` and 
create bootable jar files for each. Then it will generate `docker` images for each and push them to your local repo. The 
`Dockerfile` for each project is located in the base directory for each module.

To deploy the docker images to a remote repository, update the `docker.registry` and `docker.image.registry` in the base
`pom.xml` of this project. The `docker.registry` is the URl of the remote repository and the `docker.image.registry` is
the first part of the docker image names for your company (`<registry>/<image-name>:<version`).
```
    <properties>
        ...
        <docker.registry>https://index.docker.io/v1/</docker.registry>
        <docker.image.registry>com.mycompany</docker.image.registry>
    </properties>
```
Then go to the base directory and run `mvn deploy -Ddocker.username=<username> -Ddocker.password=<password>`. This will
deploy your docker images to the docker registry specified by `docker.registry` as both `linux/amd64` and `linux/arm64`
architectures.

## Setup Project

Now that the Kafka clients are built and have `docker` images in your local repo, you can run an them all
by going into the `platform` directory of this project and running the `setup.sh` script. This script will launch [Zookeeper](https://zookeeper.apache.org/),
[Kafka](https://kafka.apache.org/) brokers, and the [Confluent Schema Registry](https://docs.confluent.io/platform/current/schema-registry/index.html).
In addition, it will launch the `data-protection-streams`, `java-kafka-consumer`, `java-kafka-producer`, and 
`java-kafka-streams` applications will be launched and start processing messages as described below.

1) `java-kafka-producer` will generate an Avro message and send it to the topic `demo.customer` every second. It will 
also send any PII violations to the topic `demo.data.violation`.
2) `java-kafka-streams` will consume the message from the topic `demo.customer` and send it to the topic 
`demo.transform.customer`. It will also send any PII violations to the topic `demo.data.violation`.
3) `java-kafka-consumer` will consume the message from the topic `demo.transform.customer` and print its contents to the 
console. It will also send any PII violations to the topic `demo.data.violation`.
4) `data-protection-streams` will consume `demo.customer` and `demo.transform.customer` and send any PII violations to 
the topic `demo.data.violation.monitor`.

## Data Violations

### Kafka Producer/Consumer Interceptors

The example Kafka applications are configured to send data violations to `demo.data.violation` by using 
[Consumer Interceptors](https://kafka.apache.org/30/javadoc/org/apache/kafka/clients/consumer/ConsumerInterceptor.html) and
[Producer Interceptors](https://kafka.apache.org/30/javadoc/org/apache/kafka/clients/producer/ProducerInterceptor.html).
These interceptors apply rules specified by [Avro](https://avro.apache.org/docs/1.11.1/specification/_print/) record and 
field names to each incoming produced or consumed record. These interceptors then send JSON messages to the configured 
topic `demo.data.violation` with details about the rule violated and the message (topic, partition, offset, client ID,
group ID, timestamp). Lastly, the interceptors allow the message to be passed through. This solution has some advantages
over other monitoring techniques.

1) The monitoring is real-time. As violations occur a Kafka topic is notified.
2) This solution has the ability to fix violations real-time by encrypting the data or stopping messages from being
produced (using the Producer Interceptor).

The solution has the disadvantages listed below.

1) Even with a Java agent forcing the consumer and producer interceptors to be set on clients, the developers need to
make changes to their application deployment. This also means changes to the data protection rules means redeployment
of the Kafka applications that are affected.
2) This monitoring happens real-time in the Kafka pipeline, so naturally it will affect throughput and latency.
3) For Kafka Streams applications, messages are consumed as byte arrays and then converted later in the topology into
the objects specified by the serdes. To avoid converting the byte array twice into an Avro record one would be tempted 
to perform the rule check in the serde rather than the consumer interceptor. The problem with this approach is that you
no longer have access to where the record came from (partition, offset) in the serde which you want to send to the data
violation topic. Therefore, in the interceptors an optional conversion of byte arrays to
[Avro](https://avro.apache.org/docs/1.11.1/specification/_print/) records is provided. This obviously will have an
effect on throughput and latency, but is the only option for this approach. A more efficient approach would be to
perform the rules violation check in the streams topology (such as demonstrated in `data-protection-streams`). This is
very invasive to application code as opposed to consumer and producer interceptors though.

### Independent Monitoring Application

Another approach to data violation monitoring is to have independent applications consuming data from topics and 
reporting any data violations to a topic. An example of this approach is shown in the  `data-protection-streams` 
application. The advantages to this approach are listed below.

1) This type of monitoring does not affect the throughput or latency of other Kafka applications except that it uses
Kafka broker resources (indirect impact).
2) This type of monitoring does not affect the deployment cycle of other Kafka applications.

The solution has the disadvantages listed below.

1) The notification of data violations is technically not real-time. Although, if the consumers are scaled to keep up
with all other Kafka applications it can get close.
2) This solution requires all messages to be consumed twice, once by the actual Kafka applications using the data and
once by the monitoring application. This means more resources to run the monitoring applications which may not be
trivial depending on how "real-time" you need the monitoring to be and how much data is monitored. It also means more
resources on the Kafka broker side (more consumers, consumer groups, etc).
3) This solution provides no way to fix the data violation issues in real-time.

## Data Violations Rules

### Rule Configuration

Data violation rules are specified in the `data-protection` library (located in the  `data-protection` folder under the 
base project folder). The rules are defined in the `data-protection.yml` in the `data-protection/src/main/resources`
folder. Below is an example of this file.
```
rules:
  - record: "com.mycompany.kafka.model.Customer"
    field: "creditCardNumber"
    type: "pattern-match"
    regex: "^4[0-9]{12}(?:[0-9]{3})?$"
  - record: "com.mycompany.kafka.model.Customer"
    field: "firstName"
    type: "pattern-match"
    regex: "^M.*$"
  - record: "com.mycompany.kafka.model.Customer"
    field: "lastName"
    type: "proper-name-match"
```
Underneath the `rules` property is a list of rules which contain the following properties:
* `record`: The full Avro record name ("<record-namespace>.<record-name>")
* `field`: The Avro field name which can be nested (ex: `address.addressLine1` is the field `address`'s field `addressLine1`)
* `type`: The type of rule (only `pattern-match` and `proper-name-match` is currently supported)

### Pattern Match Rule

The `pattern-match` rule also contains a `regex` property that is a regular expression that matches values that 
"violate" the rule. For instance, if first names that start with the letter `M` are rule violations you would set the
property `regex` as `^M.*$`.

The `pattern-match` rule can only be applied to certain Avro record field types. The field can be the following 
[Avro Primitive Types](https://avro.apache.org/docs/1.11.1/specification/#primitive-types):

* `string`
* `bytes`

The field can also be an `array` [Avro Complex Type](https://avro.apache.org/docs/1.11.1/specification/#complex-types)
provided that its items are in primitive types listed above. Also, the field can be nested in a `record` 
[Avro Complex Type](https://avro.apache.org/docs/1.11.1/specification/#complex-types) provided that the nested field
has a value matching the primitive types listed above.

### Proper Name Match Rule

The `proper-name-match` rule matches values that resemble English proper names (first name, last name or full name). It
only matches the value if the name is capitalized correctly. It will not detect names that are all lowercase or all 
uppercase. The English name model is the [Open NLP Model](http://opennlp.sourceforge.net/models-1.5/) named 
`en-ner-person.bin` which was downloaded from [here](http://opennlp.sourceforge.net/models-1.5/en-ner-person.bin).

The `proper-name-match` rule can only be applied to certain Avro record field types. The field can be the following
[Avro Primitive Types](https://avro.apache.org/docs/1.11.1/specification/#primitive-types):

* `string`
* `bytes`

The field can also be an `array` [Avro Complex Type](https://avro.apache.org/docs/1.11.1/specification/#complex-types)
provided that its items are in primitive types listed above. Also, the field can be nested in a `record`
[Avro Complex Type](https://avro.apache.org/docs/1.11.1/specification/#complex-types) provided that the nested field
has a value matching the primitive types listed above.