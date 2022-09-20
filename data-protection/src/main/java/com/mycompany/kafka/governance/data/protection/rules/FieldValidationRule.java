package com.mycompany.kafka.governance.data.protection.rules;

import com.mycompany.kafka.governance.data.protection.RuleViolation;
import org.apache.avro.generic.GenericRecord;

public interface FieldValidationRule {

    String getRecordName();

    String getFieldName();

    boolean test(GenericRecord genericRecord);

    RuleViolation getViolationMessage();
}
