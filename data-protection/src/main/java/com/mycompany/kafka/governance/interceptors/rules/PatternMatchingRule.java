package com.mycompany.kafka.governance.interceptors.rules;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class PatternMatchingRule implements FieldValidationRule {

    private static final Logger log = LoggerFactory.getLogger(PatternMatchingRule.class);

    private final String recordName;
    private final String[] fieldNames;
    private final Pattern pattern;

    public PatternMatchingRule(String recordName, String fieldName, String pattern) {
        this.recordName = recordName;
        this.fieldNames = fieldName.split("\\.");
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public String getRecordName() {
        return recordName;
    }

    @Override
    public String getFieldName() {
        return String.join(".", fieldNames);
    }

    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public boolean test(GenericRecord record) {
        Schema schema = record.getSchema();
        if (this.recordName.equals(schema.getFullName())) {

            String[] fieldValues = getFieldValues(record);
            if (fieldValues == null) {
                return false;
            }

            for (String fieldValue : fieldValues) {
                if (this.pattern.matcher(fieldValue).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public RuleViolation getViolationMessage() {
        return new RuleViolation(getRecordName(), getFieldName(),
                "Value matches pattern \"" + getPattern().pattern().replace("\"", "\\\"") + "\"");
    }

    private String[] getFieldValues(GenericRecord record) {

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
                return toStringArray(fieldValue);
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

    private String[] toStringArray(Object value) {

        if (value instanceof String[]) {
            return (String[]) value;
        } else if (value instanceof String) {
            return new String[] {(String) value};
        } else if (value instanceof Utf8) {
            return new String[] { new String(((Utf8) value).getBytes()) };
        } else if (value instanceof Utf8[]) {

            Utf8[] utf8Array = (Utf8[]) value;
            String[] stringArray = new String[utf8Array.length];
            for (int j = 0; j < utf8Array.length; j++) {
                stringArray[j] = new String(utf8Array[j].getBytes());
            }
            return stringArray;

        } else if (value instanceof byte[][]) {

            byte[][] bytesArray = (byte[][]) value;
            String[] stringArray = new String[bytesArray.length];
            for (int j = 0; j < bytesArray.length; j++) {
                stringArray[j] = new String(bytesArray[j]);
            }
            return stringArray;

        } else if (value instanceof byte[]) {
            return new String[] {new String((byte[]) value)};
        }
        return null;
    }

    @Override
    public String toString() {
        return "FieldValidationRule{" +
                "recordName='" + recordName + '\'' +
                ", fieldName=" + getFieldName() +
                ", pattern=" + pattern +
                '}';
    }
}
