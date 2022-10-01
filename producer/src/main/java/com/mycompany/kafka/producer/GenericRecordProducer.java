package com.mycompany.kafka.producer;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static java.lang.String.format;

public class GenericRecordProducer {

    private static final Logger log = LoggerFactory.getLogger(GenericRecordProducer.class);
    private static final String TOPIC = "topic";
    private static final String SCHEMA = "schema";
    private static final String MESSAGES = "messages";
    private static final String BATCH_SIZE = "batch.size";
    private static final String FREQUENCY_MS = "frequency.ms";
    private static final String ENCRYPT_FIELDS = "encrypt.fields";
    private static final String ENCODE_FIELDS = "encode.fields";

    private final KafkaProducer<Long, GenericRecord> producer;
    private final String topicName;
    private final long messages;
    private final String schemaName;
    private final int batchSize;
    private final long frequencyMs;
    private final Set<String> encryptFields = new HashSet<>();
    private final Set<String> encodeFields = new HashSet<>();

    public GenericRecordProducer(KafkaProducer<Long, GenericRecord> producer, Properties applicationProperties) {
        this.producer = producer;
        topicName = applicationProperties.getProperty(TOPIC);
        messages = Long.parseLong(applicationProperties.getProperty(MESSAGES));
        schemaName = applicationProperties.getProperty(SCHEMA);
        batchSize = Integer.parseInt(applicationProperties.getProperty(BATCH_SIZE));
        frequencyMs = Long.parseLong(applicationProperties.getProperty(FREQUENCY_MS));
        if (applicationProperties.getProperty(ENCRYPT_FIELDS) != null &&
                !"".equals(applicationProperties.getProperty(ENCRYPT_FIELDS).trim())) {
            encryptFields.addAll(Arrays.asList(applicationProperties.getProperty(ENCRYPT_FIELDS).split(",")));
        }
        if (applicationProperties.getProperty(ENCODE_FIELDS) != null &&
                !"".equals(applicationProperties.getProperty(ENCODE_FIELDS).trim())) {
            encodeFields.addAll(Arrays.asList(applicationProperties.getProperty(ENCODE_FIELDS).split(",")));
        }
    }

    public void start() throws IOException {

        SchemaLoader schemaLoader = new SchemaLoader();
        Schema schema = schemaLoader.getSchema(schemaName);
        if (schema == null) {
            throw new RuntimeException(format("Schema \"%s.avsc\" was not found in the classpath", schemaName));
        }

        Map<String, String> customFields = new HashMap<>();
        customFields.put("Customer.creditCardNumber", "4[0-9]{12}");
        DataGenerator dataGenerator = new DataGenerator(customFields);

        log.info("Producer started");
        long count = 0;
        try {
            while (count < messages) {
                long currentBatch = count + batchSize < messages ? batchSize : messages - count;
                List<GenericRecord> records = convert(schema, dataGenerator.generate(schema, (int) currentBatch));
                for (GenericRecord record : records) {
                    //encryptFields(record);
                    //encodeFields(record);
                    producer.send(new ProducerRecord<>(topicName, (Long) record.get("id"), record));
                }
                producer.flush();
                log.info("Produced {} messages", batchSize);
                count += batchSize;
                try {
                    Thread.sleep(frequencyMs);
                } catch (InterruptedException ignored) {}
            }
        } catch (Exception e) {
            log.error("Error producing messages", e);
            throw e;
        } finally {
            producer.close();
        }
        log.info("Produced total of {} messages", count);
        log.info("Producer finished");
    }

    private List<GenericRecord> convert(Schema schema, List<Map<String, Object>> values) {

        List<GenericRecord> records = new ArrayList<>();
        for (Map<String, Object> value : values) {
            GenericRecordBuilder recordBuilder = new GenericRecordBuilder(schema);
            for (Schema.Field field : schema.getFields()) {
                recordBuilder.set(field, value.get(field.name()));
            }
            records.add(recordBuilder.build());
        }
        return records;
    }

    private void encryptFields(GenericRecord record) {
        if (!encryptFields.isEmpty()) {

            Schema schema  = record.getSchema();
            for(String fieldName : encryptFields) {

                Object fieldValue = record.get(fieldName);
                Schema.Field field = schema.getField(fieldName);
                Schema fieldSchema = field.schema();
                if (Schema.Type.UNION.equals(fieldSchema.getType())) {
                    System.out.println(fieldSchema.getType());
                }
                System.out.println(field);
            }
        }
    }

    private void encodeFields(GenericRecord record) {
        if (!encodeFields.isEmpty()) {

            Schema schema  = record.getSchema();
            for(String fieldName : encodeFields) {

                Object fieldValue = record.get(fieldName);
                Schema.Field field = schema.getField(fieldName);
                System.out.println(field);
            }
        }
    }
}
