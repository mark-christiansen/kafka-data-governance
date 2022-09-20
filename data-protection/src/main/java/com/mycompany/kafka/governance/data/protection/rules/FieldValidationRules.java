package com.mycompany.kafka.governance.data.protection.rules;

import com.mycompany.kafka.governance.data.protection.Config;
import com.mycompany.kafka.governance.data.protection.RuleViolation;
import org.apache.avro.generic.GenericRecord;

import java.util.*;

public class FieldValidationRules {

    private final Map<String, List<FieldValidationRule>> rules = Collections.synchronizedMap(new HashMap<>());

    public FieldValidationRules() {
    }

    public FieldValidationRules(List<FieldValidationRule> rules) {

        for (FieldValidationRule rule : rules) {
            List<FieldValidationRule> recordRules = this.rules.computeIfAbsent(rule.getRecordName(), k -> new ArrayList<>());
            recordRules.add(rule);
        }
    }

    public void add(FieldValidationRule rule) {

        String recordName  = rule.getRecordName();
        List<FieldValidationRule> recordRules = rules.computeIfAbsent(recordName, k -> new ArrayList<>());
        Iterator<FieldValidationRule> iter = recordRules.iterator();
        while (iter.hasNext()) {
            FieldValidationRule next = iter.next();
            if (next.getFieldName().equals(rule.getFieldName())) {
                iter.remove();
            }
        }
        recordRules.add(rule);
    }

    public List<RuleViolation> test(GenericRecord record) {

        List<RuleViolation> ruleViolations = new ArrayList<>();

        // check if there are any field validation rules for this record
        List<FieldValidationRule> recordRules = rules.get(record.getSchema().getFullName());
        if (recordRules == null) {
            return null;
        }

        // if there are field validation rules for this record, test each against the record and return rule
        // violation for any that return true.
        for (FieldValidationRule rule : recordRules) {
            if (rule.test(record)) {
                ruleViolations.add(rule.getViolationMessage());
            }
        }
        return ruleViolations;
    }
}
