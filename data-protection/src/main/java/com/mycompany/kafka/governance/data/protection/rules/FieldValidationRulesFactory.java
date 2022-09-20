package com.mycompany.kafka.governance.data.protection.rules;

import com.mycompany.kafka.governance.data.protection.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FieldValidationRulesFactory {

    private static final String RULES = "rules";
    private static final String RECORD = "record";
    private static final String FIELD = "field";
    private static final String TYPE = "type";
    private static final String PATTERN_MATCH_TYPE = "pattern-match";
    private static final String PROPER_NAME_MATCH_TYPE = "proper-name-match";
    private static final String REGEX = "regex";

    public static FieldValidationRules create() {

        List<FieldValidationRule> rules = new ArrayList<>();

        Map<String, Object> config = Config.get();
        java.util.List<Map<String, String>> rulesConfigs = ((java.util.List<Map<String, String>>) config.get(RULES));
        for (Map<String, String> ruleConfigs : rulesConfigs) {
            rules.add(createRule(ruleConfigs));
        }

        return new FieldValidationRules(rules);
    }

    public static FieldValidationRule createRule(Map<String, String> props) {

        String recordName = props.get(RECORD);
        String type = props.get(TYPE);

        if (PATTERN_MATCH_TYPE.equals(type)) {
            return new PatternMatchRule(recordName, props.get(FIELD), props.get(REGEX));
        } else if (PROPER_NAME_MATCH_TYPE.equals(type)) {
            return new ProperNameMatchRule(recordName, props.get(FIELD));
        } else {
            throw new UnsupportedOperationException("The rule type \"" + type + "\" is not supported");
        }
    }
}
