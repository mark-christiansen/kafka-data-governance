package com.mycompany.kafka.schema.monitor;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings("unused")
@Configuration
public class Config {

    private static final String SCHEMA_REGISTRY_URL = "schema.registry.url";
    private static final String SCHEMA_CACHE_CAPACITY = "schema.cache.capacity";

    @Bean(name = "applicationProperties")
    @ConfigurationProperties(prefix = "application")
    public Properties applicationProperties() {
        return new Properties();
    }

    @Bean
    @ConfigurationProperties(prefix = "kafka.admin")
    public Properties adminProperties() {
        return new Properties();
    }

    @Bean
    @ConfigurationProperties(prefix = "kafka.consumer")
    public Properties consumerProperties() {
        return new Properties();
    }

    @Bean
    @ConfigurationProperties(prefix = "kafka.producer")
    public Properties producerProperties() {
        return new Properties();
    }

    @Bean
    public SchemaRegistryClient schemaRegistryClient() {

        Properties kafkaProperties = consumerProperties();

        // pull out schema registry properties from kafka properties to pass to schema registry client
        Map<String, Object> schemaProperties = new HashMap<>();
        for (Map.Entry<Object, Object> entry : kafkaProperties.entrySet()) {
            String propertyName = (String) entry.getKey();
            if (propertyName.startsWith("schema.registry.") || propertyName.startsWith("basic.auth.")) {
                schemaProperties.put(propertyName, entry.getValue());
            }
        }
        return new CachedSchemaRegistryClient(kafkaProperties.getProperty(SCHEMA_REGISTRY_URL),
                Integer.parseInt(kafkaProperties.getProperty(SCHEMA_CACHE_CAPACITY)), schemaProperties);
    }

    @Bean
    public AdminClient adminClient() {
        return AdminClient.create(adminProperties());
    }

    @Bean
    public KafkaConsumer<String, String> kafkaConsumer() {
        return new KafkaConsumer<>(consumerProperties());
    }

    @Bean
    public KafkaProducer<String, String> kafkaProducer() {
        return new KafkaProducer<>(producerProperties());
    }
}
