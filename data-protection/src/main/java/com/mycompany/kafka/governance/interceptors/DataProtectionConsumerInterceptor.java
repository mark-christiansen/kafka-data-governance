package com.mycompany.kafka.governance.interceptors;

import com.mycompany.kafka.governance.interceptors.rules.FieldValidationRules;
import com.mycompany.kafka.governance.interceptors.rules.RuleViolation;
import com.mycompany.kafka.governance.interceptors.util.ClientIdGenerator;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressWarnings("unused")
public class DataProtectionConsumerInterceptor<K, V> implements ConsumerInterceptor<K, V> {

    private static final Logger log = LoggerFactory.getLogger(DataProtectionProducerInterceptor.class);
    private static final String TOPIC_NAME = "DATA_PROTECTION_VIOLATION_TOPIC_NAME";

    private Producer<K, ConsumerRuleViolation> producer;
    private KafkaAvroDeserializer deserializer;
    private String topicName;
    private final FieldValidationRules rules;
    private String clientId;
    private String groupId;

    public DataProtectionConsumerInterceptor() {
        this.rules = new FieldValidationRules();
    }

    @Override
    public ConsumerRecords<K, V> onConsume(ConsumerRecords<K, V> consumerRecords) {

        consumerRecords.forEach(consumerRecord -> {

            // the expectation is that the record can be consumed as an avro record
            V recordValue = consumerRecord.value();
            GenericRecord genericRecord = null;
            if (recordValue instanceof GenericRecord) {
                genericRecord = (GenericRecord) recordValue;
            // if record is consumed as a byte array deserialize it into a generic avro record
            } else if (recordValue instanceof byte[]) {
                try {
                    genericRecord = (GenericRecord) deserializer.deserialize(consumerRecord.topic(), (byte[]) recordValue);
                } catch(SerializationException e) {
                    // if record cannot be serialized to an Avro object than ignore the record violation check
                }
            }

            // test if this consumer record violates any field validation rules
            if (genericRecord != null) {

                List<RuleViolation> violations = rules.test(genericRecord);
                if (violations != null) {
                    for (RuleViolation violation : violations) {
                        ConsumerRuleViolation consumerViolation = new ConsumerRuleViolation(consumerRecord.topic(),
                                consumerRecord.partition(), consumerRecord.offset(), consumerRecord.timestamp(),
                                clientId, groupId, violation);
                        this.producer.send(new ProducerRecord<>(topicName, null, consumerViolation));
                    }
                }

            }
        });
        // continue to consume record even if there is a data violation
        return consumerRecords;
    }

    @Override
    public void onCommit(Map<TopicPartition, OffsetAndMetadata> map) {
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> map) {

        final Map<String, Object> producerConfigs = new HashMap<>(map);
        // remove interceptor for this interceptor's forwarding producer configs
        producerConfigs.remove(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG);

        this.clientId = (String) producerConfigs.get(ProducerConfig.CLIENT_ID_CONFIG);
        if (this.clientId == null) {
            this.clientId =  String.valueOf(ClientIdGenerator.nextClientId());
        }
        this.groupId = (String) map.get(ConsumerConfig.GROUP_ID_CONFIG);

        String interceptorClientId = "data-protect-producer-" + clientId;
        producerConfigs.put(ProducerConfig.CLIENT_ID_CONFIG, interceptorClientId);
        producerConfigs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());

        this.topicName = System.getenv(TOPIC_NAME);
        if (this.topicName == null) {
            throw new RuntimeException("The environment variable \"" + TOPIC_NAME + "\" must be set");
        }
        this.producer = new KafkaProducer<>(producerConfigs);

        SchemaRegistryClient schemaClient = new CachedSchemaRegistryClient(
                (String) producerConfigs.get(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG), 5000);
        this.deserializer = new KafkaAvroDeserializer(schemaClient);
    }
}
