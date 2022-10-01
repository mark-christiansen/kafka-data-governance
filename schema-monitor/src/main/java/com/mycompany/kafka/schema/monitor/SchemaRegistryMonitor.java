package com.mycompany.kafka.schema.monitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.flipkart.zjsonpatch.JsonDiff;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.lang.String.format;

@Component
public class SchemaRegistryMonitor {

    private static final Logger log = LoggerFactory.getLogger(SchemaRegistryMonitor.class);

    private static final long COMMIT_TIMEOUT_SECS = 10;
    private static final String INPUT_TOPIC = "input.topic";
    private static final String OUTPUT_TOPIC = "output.topic";
    private static final String OFFSET_SECONDS = "offset.seconds";
    private static final int OFFSET_SECONDS_DEFAULT = 300;
    private static final String POLL_TIMEOUT_SECONDS = "poll.timeout.seconds";
    private static final int POLL_TIMEOUT_SECONDS_DEFAULT = 30;

    private final KafkaConsumer<String, String> consumer;
    private final KafkaProducer<String, String> producer;
    private final String inputTopic;
    private final String outputTopic;
    private final int offsetSeconds;
    private final int pollTimeoutSeconds;
    private final SchemaRegistryState schemaState;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private boolean running;

    @Autowired
    public SchemaRegistryMonitor(KafkaConsumer<String, String> kafkaConsumer,
                                 KafkaProducer<String, String> kafkaProducer,
                                 Properties applicationProperties,
                                 SchemaRegistryState schemaState) {
        this.consumer = kafkaConsumer;
        this.producer = kafkaProducer;
        this.inputTopic = applicationProperties.getProperty(INPUT_TOPIC);
        this.outputTopic = applicationProperties.getProperty(OUTPUT_TOPIC);
        this.offsetSeconds = applicationProperties.getProperty(OFFSET_SECONDS) != null ?
            Integer.parseInt(applicationProperties.getProperty(OFFSET_SECONDS)) : OFFSET_SECONDS_DEFAULT;
        this.pollTimeoutSeconds = applicationProperties.getProperty(POLL_TIMEOUT_SECONDS) != null ?
                Integer.parseInt(applicationProperties.getProperty(POLL_TIMEOUT_SECONDS)) : POLL_TIMEOUT_SECONDS_DEFAULT;
        this.schemaState = schemaState;
    }

    @PostConstruct
    public void start() {

        subscribe(inputTopic, offsetSeconds);
        running = true;

        log.info("Consumer started");
        try {
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(pollTimeoutSeconds));
                log.info("Consumed {} records", records.count());
                records.forEach(record -> {

                    // Incoming message might not be a schema, there are other messages in the schema topic such as
                    // mode switches. Therefore, the read below might throw an error, but we don't care about the
                    // other messages so we will just skip over them.
                    Schema schema = null;
                    if (record.value() != null) {
                        try {
                            schema = objectMapper.readValue(record.value(), Schema.class);
                        } catch (JsonProcessingException e) {
                            log.warn(format("Received message that wasn't a schema from the schemas topic %s", inputTopic), e);
                        }
                    }

                    if (schema != null) {
                        try {

                            String subject = schema.getSubject();
                            int version = schema.getVersion();
                            SchemaMetadata latestSchema = schemaState.getSchema(subject);

                            String jsonDiff = null;
                            if (latestSchema == null) {

                                JsonNode beforeNode = objectMapper.createObjectNode();
                                JsonNode afterNode = objectMapper.readTree(schema.getSchema());
                                jsonDiff = JsonDiff.asJson(beforeNode, afterNode).toString();

                            // incoming version is greater than existing version for this subject, so figure out the
                            // differences between the schemas
                            } else if (schema.getVersion() > latestSchema.getVersion()) {

                                JsonNode beforeNode = objectMapper.readTree(latestSchema.getSchema());
                                JsonNode afterNode = objectMapper.readTree(schema.getSchema());
                                jsonDiff = JsonDiff.asJson(beforeNode, afterNode).toString();
                            }

                            // send the JSON diff to schemas audit topic (output topic)
                            producer.send(new ProducerRecord<>(outputTopic, subject + "-" + version, jsonDiff));

                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                consumer.commitSync(Duration.ofSeconds(COMMIT_TIMEOUT_SECS));
            }
        } catch (Exception e) {
            log.error("Error consuming messages", e);
            throw e;
        } finally {
            consumer.close();
        }
        log.info("Consumer finished");
    }

    private void subscribe(String topicName, long secondOffset) {

        long offsetTimestamp = Instant.now().minus(secondOffset, ChronoUnit.SECONDS).toEpochMilli();
        consumer.subscribe(Collections.singleton(topicName), new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {}

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {

                Map<TopicPartition, Long> timestamps = new HashMap<>();
                for (TopicPartition partition : partitions) {
                    timestamps.put(partition, offsetTimestamp);
                }

                Map<TopicPartition, OffsetAndTimestamp> offsets = consumer.offsetsForTimes(timestamps);
                for (TopicPartition partition : partitions) {
                    OffsetAndTimestamp offset = offsets.get(partition);
                    if (offset != null) {
                        consumer.seek(partition, offset.offset());
                    }
                }
            }
        });
    }

}
