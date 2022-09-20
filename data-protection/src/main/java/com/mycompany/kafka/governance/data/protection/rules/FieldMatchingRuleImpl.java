package com.mycompany.kafka.governance.data.protection.rules;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FieldMatchingRuleImpl<V> implements FieldValidationRule {

    private static final Logger log = LoggerFactory.getLogger(FieldMatchingRuleImpl.class);

    protected final String recordName;
    protected final String[] fieldNames;

    public FieldMatchingRuleImpl(String recordName, String fieldName) {
        this.recordName = recordName;
        this.fieldNames = fieldName.split("\\.");
    }

    @Override
    public String getRecordName() {
        return recordName;
    }

    @Override
    public String getFieldName() {
        return String.join(".", fieldNames);
    }

    @Override
    public boolean test(GenericRecord record) {

        Schema schema = record.getSchema();
        if (this.recordName.equals(schema.getFullName())) {
            V[] fieldValues = getFieldValues(record);
            return fieldValues != null && test(fieldValues);
        }
        return false;
    }

    protected abstract boolean test(V[] fieldValue);

    protected abstract V[] getFieldValues(Object fieldValue);

    private V[] getFieldValues(GenericRecord record) {

        for (int i = 0; i < this.fieldNames.length; i++) {

            String fieldName = this.fieldNames[i];
            if (!record.hasField(fieldName)) {
                log.warn("Attempted to retrieve value for unknown field name \"{}\" for record \"{}\"", fieldName, this.recordName);
                return null;
            }

            Object fieldValue = record.get(fieldName);
            if (fieldValue == null) {
                return null;
            }

            if (i == this.fieldNames.length - 1) {
                return getFieldValues(fieldValue);
            } else {
                if (fieldValue instanceof GenericRecord) {
                    record = (GenericRecord) fieldValue;
                } else {
                    log.warn("Attempted to retrieve value for nested field name \"{}\" on non-record field \"{}\" for record \"{}\"",
                            fieldNames[i + 1], fieldName, this.recordName);
                    return null;
                }
            }
        }
        return null;
    }
}
