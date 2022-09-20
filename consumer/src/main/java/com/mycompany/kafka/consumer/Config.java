package com.mycompany.kafka.consumer;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class Config {

    @Bean
    @ConfigurationProperties(prefix = "kafka.consumer")
    public Properties kafkaProperties() {
        return new Properties();
    }

    @Bean
    @ConfigurationProperties(prefix = "application")
    public Properties applicationProperties() {
        return new Properties();
    }

    @Bean
    public KafkaConsumer<Long, GenericRecord> kafkaConsumer() {
        return new KafkaConsumer<>(kafkaProperties());
    }
}