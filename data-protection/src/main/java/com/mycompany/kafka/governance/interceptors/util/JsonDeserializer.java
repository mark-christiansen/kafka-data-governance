package com.mycompany.kafka.governance.interceptors.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class JsonDeserializer<V> implements Deserializer<V> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Class<V> jsonClass;

    public JsonDeserializer(Class<V> jsonClass) {
        this.jsonClass = jsonClass;
    }

    @Override
    public void configure(Map<String, ?> props, boolean isKey) {
    }

    @Override
    public V deserialize(String topic, byte[] bytes) {

        if (bytes == null) {
            return null;
        }

        V data;
        try {
            data = objectMapper.readValue(bytes, jsonClass);
        } catch (Exception e) {
            throw new SerializationException(e);
        }

        return data;
    }

    @Override
    public void close() {
    }
}