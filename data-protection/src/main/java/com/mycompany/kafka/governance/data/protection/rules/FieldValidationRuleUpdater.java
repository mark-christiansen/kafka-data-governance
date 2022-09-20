package com.mycompany.kafka.governance.data.protection.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.TopicExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class FieldValidationRuleUpdater implements Runnable, Closeable {

    private static final Logger log = LoggerFactory.getLogger(FieldValidationRuleUpdater.class);
    private static final long POLL_TIMEOUT_SECS = 30;
    private static final long COMMIT_TIMEOUT_SECS = 10;


    private final KafkaConsumer<String, byte[]> consumer;
    private final AdminClient adminClient;
    private final String topicName;
    private final FieldValidationRules rules;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private boolean running = false;

    public FieldValidationRuleUpdater(KafkaConsumer<String, byte[]> consumer,
                                      AdminClient adminClient,
                                      String topicName,
                                      FieldValidationRules rules) {
        this.consumer = consumer;
        this.adminClient = adminClient;
        this.topicName = topicName;
        this.rules = rules;
    }

    @Override
    public void run() {

        log.info("Field validation rule updater creating topic {}", topicName);
        createTopic(this.topicName);

        log.info("Field validation rule updater consumer subscribing to topic {}", topicName);
        subscribe(topicName);

        running = true;
        log.info("Field validation rule updater consumer started");
        ConsumerRecords<String, byte[]> records;
        try {
            while (running) {

                records = consumer.poll(Duration.ofSeconds(POLL_TIMEOUT_SECS));
                log.debug("Field validation rule updater consumed {} records", records.count());
                records.forEach(record -> {

                    byte[] value = record.value();
                    if (value != null) {
                        try {

                            JsonNode jsonNode = objectMapper.readTree(value);
                            Map<String, String> ruleProps = new HashMap<>();
                            Iterator<String> fieldNames = jsonNode.fieldNames();
                            while (fieldNames.hasNext()) {
                                String fieldName = fieldNames.next();
                                ruleProps.put(fieldName, jsonNode.get(fieldName).textValue());
                            }
                            rules.add(FieldValidationRulesFactory.createRule(ruleProps));

                        } catch (IOException e) {
                            log.error("Could not consume rule for topic {}, partition {}, and offset {}", record.topic(),
                                    record.partition(), record.offset());
                        }
                    }
                });
                consumer.commitSync(Duration.ofSeconds(COMMIT_TIMEOUT_SECS));
            }
        } catch (Exception e) {
            log.error("Field validation rule updater encountered error consuming messages", e);
            throw e;
        } finally {
            consumer.close();
        }
        log.info("Field validation rule updater finished");
    }

    @Override
    public void close() {
        running = false;
    }

    private void createTopic(String topicName) {

        final NewTopic newTopic = new NewTopic(topicName, Optional.of(1), Optional.empty());
        newTopic.configs(Collections.singletonMap("cleanup.policy", "compact"));
        try {
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
        } catch (final InterruptedException | ExecutionException e) {
            // Ignore if TopicExistsException, which may be valid if topic exists
            if (!(e.getCause() instanceof TopicExistsException)) {
                throw new RuntimeException(e);
            }
        }
    }

    private void subscribe(String topicName) {

        consumer.subscribe(Collections.singleton(topicName), new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {}

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                // reset offsets for all partitions to the beginning on the partition
                for (TopicPartition partition : partitions) {
                    consumer.seek(partition, 0);
                }
            }
        });
    }
}
