package com.mycompany.kafka.governance.interceptors.rules;

import org.apache.avro.generic.GenericRecord;

public interface FieldValidationRule {

    String getRecordName();

    String getFieldName();

    boolean test(GenericRecord genericRecord);

    RuleViolation getViolationMessage();
}
