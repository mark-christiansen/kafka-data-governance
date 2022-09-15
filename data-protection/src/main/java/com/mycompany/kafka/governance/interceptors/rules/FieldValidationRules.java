package com.mycompany.kafka.governance.interceptors.rules;

import com.mycompany.kafka.governance.interceptors.Config;
import org.apache.avro.generic.GenericRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldValidationRules {

    private static final String RULES = "rules";
    private static final String RECORD = "record";
    private static final String FIELD = "field";
    private static final String TYPE = "type";
    private static final String PATTERN_MATCHING_TYPE = "pattern-matching";
    private static final String REGEX = "regex";

    private final Map<String, List<FieldValidationRule>> rules = new HashMap<>();

    public FieldValidationRules() {
        Map<String, Object> config = Config.get();
        java.util.List<Map<String, String>> rulesConfigs = ((java.util.List<Map<String, String>>) config.get(RULES));
        for (Map<String, String> ruleConfigs : rulesConfigs) {

            String recordName = ruleConfigs.get(RECORD);
            String type = ruleConfigs.get(TYPE);

            List<FieldValidationRule> recordRules = rules.computeIfAbsent(recordName, k -> new ArrayList<>());
            if (PATTERN_MATCHING_TYPE.equals(type)) {
                recordRules.add(new PatternMatchingRule(recordName, ruleConfigs.get(FIELD), ruleConfigs.get(REGEX)));
            } else {
                throw new UnsupportedOperationException("The rule type \"" + type + "\" is not supported");
            }
        }
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
