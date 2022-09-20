package com.mycompany.kafka.governance.streams.common;

import com.mycompany.kafka.governance.data.protection.rules.FieldValidationRuleUpdater;
import com.mycompany.kafka.governance.data.protection.rules.FieldValidationRules;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.Properties;

import static java.lang.String.format;

public class StreamsLifecycle {

    private static final Logger log = LoggerFactory.getLogger(StreamsLifecycle.class);
    private static final String RULES_TOPIC = "rules.topic";
    private final String applicationId;
    private final Topology topology;
    private final KafkaStreams streams;
    private final ApplicationContext applicationContext;
    private final FieldValidationRuleUpdater rulesUpdater;

    public StreamsLifecycle(Topology topology,
                            Properties applicationProperties,
                            Properties streamsProperties,
                            ApplicationContext applicationContext,
                            FieldValidationRules rules,
                            KafkaConsumer<String, byte[]> rulesConsumer,
                            AdminClient adminClient) {

        this.topology = topology;
        this.applicationId = streamsProperties.getProperty(StreamsConfig.APPLICATION_ID_CONFIG);
        streamsProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        streamsProperties.put(StreamsConfig.CLIENT_ID_CONFIG, applicationId);
        this.applicationContext = applicationContext;

        String rulesTopic = (String) applicationProperties.get(RULES_TOPIC);
        this.rulesUpdater = new FieldValidationRuleUpdater(rulesConsumer, adminClient, rulesTopic, rules);
        this.streams = new KafkaStreams(topology, streamsProperties);
    }

    @PostConstruct
    private void construct() {

        // start rules updater thread
        new Thread(this.rulesUpdater).start();

        final TopologyDescription description = topology.describe();
        log.info("=======================================================================================");
        log.info("Topology: {}", description);
        log.info("=======================================================================================");

        log.info("Starting Stream {}", applicationId);
        if (streams != null) {

            streams.setUncaughtExceptionHandler(e -> {
                log.error(format("Stopping the application %s due to unhandled exception", applicationId), e);
                SpringApplication.exit(applicationContext, () -> 1);
                return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_APPLICATION;
            });

            streams.setStateListener((newState, oldState) -> {
                if (newState == KafkaStreams.State.ERROR) {
                    throw new RuntimeException("Kafka Streams went into an ERROR state");
                }
            });

            streams.start();
        }
    }

    @PreDestroy
    private void destroy() {

        log.warn("Closing Kafka Streams application {}", applicationId);
        if (streams != null) {
            streams.close(Duration.ofSeconds(5));
            log.info("Closed Kafka Streams application {}", applicationId);
        }

        log.info("Closing rules updater thread for Kafka Streams application {}", applicationId);
        if (rulesUpdater != null) {
            rulesUpdater.close();
            log.info("Closed rules updater thread for Kafka Streams application {}", applicationId);
        }
    }

    public Boolean isHealthy() {
        return streams.state().isRunningOrRebalancing();
    }

    public String topologyDescription() {
        return topology.describe().toString();
    }
}
