package com.mycompany.kafka.schema.monitor;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class Application implements CommandLineRunner {

    private static final String TOPIC = "schema.audit.topic";
    private static final String PARTITIONS = "schema.audit.partitions";

    @Autowired
    private AdminClient adminClient;
    @Autowired
    private Properties applicationProperties;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {

        String topicName = applicationProperties.getProperty(TOPIC);
        int partitions = Integer.parseInt(applicationProperties.getProperty(PARTITIONS));
        createTopic(topicName, partitions);
    }

    private void createTopic(String topicName, int partitions) {

        final NewTopic newTopic = new NewTopic(topicName, Optional.of(partitions), Optional.empty());
        try {
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
        } catch (final InterruptedException | ExecutionException e) {
            // Ignore if TopicExistsException, which may be valid if topic exists
            if (!(e.getCause() instanceof TopicExistsException)) {
                throw new RuntimeException(e);
            }
        }
    }
}