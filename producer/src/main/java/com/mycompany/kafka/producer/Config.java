package com.mycompany.kafka.producer;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    @Bean
    @ConfigurationProperties(prefix = "kafka.producer")
    public Properties producerProperties() {
        return new Properties();
    }

    @Bean
    @ConfigurationProperties(prefix = "kafka.admin")
    public Properties adminProperties() {
        return new Properties();
    }

    @Bean
    @ConfigurationProperties(prefix = "application")
    public Properties applicationProperties() {
        return new Properties();
    }

    @Bean
    public KafkaProducer<Long, GenericRecord> kafkaProducer() {
        Properties props = producerProperties();
        log.info("props=" + props);
        return new KafkaProducer<>(props);
    }

    @Bean
    public AdminClient adminClient() {
        return AdminClient.create(adminProperties());
    }
}
