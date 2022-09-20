package com.mycompany.kafka.governance.streams;

import com.mycompany.kafka.governance.data.protection.ConsumerRuleViolation;
import com.mycompany.kafka.governance.data.protection.rules.FieldValidationRules;
import com.mycompany.kafka.governance.data.protection.RuleViolation;
import com.mycompany.kafka.governance.data.protection.util.JsonDeserializer;
import com.mycompany.kafka.governance.data.protection.util.JsonSerializer;
import com.mycompany.kafka.governance.streams.common.SerdeCreator;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.StoreBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

public class TopologyBuilder {

    private static final Logger log = LoggerFactory.getLogger(TopologyBuilder.class);

    private final String inputTopic;
    private final String outputTopic;
    private final SerdeCreator serdes;
    private final FieldValidationRules rules;

    public TopologyBuilder(Properties applicationProperties,
                           SerdeCreator serdes,
                           FieldValidationRules rules) {
        this.serdes = serdes;
        this.inputTopic = applicationProperties.getProperty("input.topic");
        this.outputTopic = applicationProperties.getProperty("output.topic");
        this.rules = rules;
    }

    public Topology build(Properties streamProperties) {

        log.info("Subscribing to input topic {}", inputTopic);
        Pattern inputTopicPattern = Pattern.compile(inputTopic);
        final StreamsBuilder builder = new StreamsBuilder();
        builder.stream(inputTopicPattern, Consumed.with(Serdes.Long(), serdes.createGenericSerde(false)))
                .transform(new TopologyTransformerSupplier())
                .to(outputTopic, Produced.with(Serdes.String(), getJsonSerde()));
        return builder.build(streamProperties);
    }

    private class TopologyTransformerSupplier implements TransformerSupplier<Long, GenericRecord, KeyValue<String, ConsumerRuleViolation>> {

        @Override
        public Transformer<Long, GenericRecord, KeyValue<String, ConsumerRuleViolation>> get() {
            return new TopologyTransformer();
        }

        @Override
        public Set<StoreBuilder<?>> stores() {
            return null;
        }
    }

    private class TopologyTransformer implements Transformer<Long, GenericRecord, KeyValue<String, ConsumerRuleViolation>> {

        private String clientId;
        private String groupId;
        private ProcessorContext context;

        @Override
        public void init(ProcessorContext context) {
            this.context = context;
            this.clientId = (String) context.appConfigs().get(ConsumerConfig.CLIENT_ID_CONFIG);
            this.groupId = (String) context.appConfigs().get(ConsumerConfig.GROUP_ID_CONFIG);
        }

        @Override
        public KeyValue<String, ConsumerRuleViolation> transform(Long key, GenericRecord value) {

            // test if the record violates any data protection rules and return
            List<RuleViolation> violations = rules.test(value);
            if (violations != null) {

                for (RuleViolation violation : violations) {
                    ConsumerRuleViolation consumerViolation = new ConsumerRuleViolation(context.topic(),
                            context.partition(), context.partition(), context.timestamp(), clientId, groupId, violation);
                    context.forward(null, consumerViolation);
                }
            }
            return null;
        }

        @Override
        public void close() {
        }
    }

    private Serde<ConsumerRuleViolation> getJsonSerde() {
        Deserializer<ConsumerRuleViolation> jsonDeserializer = new JsonDeserializer<>(ConsumerRuleViolation.class);
        Serializer<ConsumerRuleViolation> jsonSerializer = new JsonSerializer<>();
        return Serdes.serdeFrom(jsonSerializer, jsonDeserializer);
    }
}
