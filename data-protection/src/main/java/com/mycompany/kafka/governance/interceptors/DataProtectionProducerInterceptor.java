package com.mycompany.kafka.governance.interceptors;

import com.mycompany.kafka.governance.interceptors.rules.FieldValidationRules;
import com.mycompany.kafka.governance.interceptors.rules.RuleViolation;
import com.mycompany.kafka.governance.interceptors.util.ClientIdGenerator;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.errors.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
public class DataProtectionProducerInterceptor<K, V> implements ProducerInterceptor<K, V> {

    private static final Logger log = LoggerFactory.getLogger(DataProtectionProducerInterceptor.class);
    private static final String TOPIC_NAME = "DATA_PROTECTION_VIOLATION_TOPIC_NAME";

    private Producer<K, ProducerRuleViolation> producer;
    private KafkaAvroDeserializer deserializer;
    private String topicName;
    private final FieldValidationRules rules;
    private String clientId;

    public DataProtectionProducerInterceptor() {
        this.rules = new FieldValidationRules();
    }

    @Override
    public ProducerRecord<K, V> onSend(ProducerRecord<K, V> producerRecord) {

        // the expectation is that the record can be produced as an avro record
        V recordValue = producerRecord.value();
        GenericRecord genericRecord = null;
        if (recordValue instanceof GenericRecord) {
            genericRecord = (GenericRecord) recordValue;
        // if record is produced as a byte array serialize it into a generic avro record
        } else if (recordValue instanceof byte[]) {
            try {
                genericRecord = (GenericRecord) deserializer.deserialize(producerRecord.topic(), (byte[]) recordValue);
            } catch(SerializationException e) {
                // if record cannot be serialized to an Avro object than ignore the record violation check
            }
        }

        // test if this producer record violates any field validation rules
        if (genericRecord != null) {

            List<RuleViolation> violations = rules.test(genericRecord);
            if (violations != null) {
                for (RuleViolation violation : violations) {
                    ProducerRuleViolation producerViolation = new ProducerRuleViolation(producerRecord.topic(),
                            producerRecord.partition(), producerRecord.timestamp(), clientId, violation);
                    this.producer.send(new ProducerRecord<>(topicName, null, producerViolation));
                }
            }

        }
        // continue to send record even if there is a data violation
        return producerRecord;
    }

    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {
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

        String interceptorClientId = "data-protect-producer-" + clientId;
        producerConfigs.put(ProducerConfig.CLIENT_ID_CONFIG, interceptorClientId);
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
