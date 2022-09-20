package com.mycompany.kafka.schema.monitor;

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class SchemaRegistryState {

    private final Map<String, SchemaMetadata> schemas = new HashMap<>();
    private final SchemaRegistryClient schemaClient;

    @Autowired
    public SchemaRegistryState(SchemaRegistryClient schemaClient) {
        this.schemaClient = schemaClient;
    }

    @PostConstruct
    public void load() throws RestClientException, IOException {

        Collection<String> subjects = schemaClient.getAllSubjects(false);
        for(String subject : subjects) {
            SchemaMetadata schemaMetadata = schemaClient.getLatestSchemaMetadata(subject);
            schemas.put(subject, schemaMetadata);
        }
    }

    public SchemaMetadata getSchema(String subject) {
        return schemas.get(subject);
    }

    public void addSchema(String subject, int id, int version, String schema) {
        schemas.put(subject, new SchemaMetadata(id, version, schema));
    }
}
