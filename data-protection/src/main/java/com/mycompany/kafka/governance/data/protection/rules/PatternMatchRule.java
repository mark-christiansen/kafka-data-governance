package com.mycompany.kafka.governance.data.protection.rules;

import com.mycompany.kafka.governance.data.protection.RuleViolation;
import com.mycompany.kafka.governance.data.protection.util.AvroUtil;

import java.util.regex.Pattern;

public class PatternMatchRule extends FieldMatchingRuleImpl<String> {

    private final Pattern pattern;

    public PatternMatchRule(String recordName, String fieldName, String pattern) {
        super(recordName, fieldName);
        this.pattern = Pattern.compile(pattern);
    }

    public Pattern getPattern() {
        return pattern;
    }

    @Override
    protected boolean test(String[] fieldValues) {
        for (String fieldValue : fieldValues) {
            if (this.pattern.matcher(fieldValue).matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public RuleViolation getViolationMessage() {
        return new RuleViolation(getRecordName(), getFieldName(),
                "Value matches pattern \"" + getPattern().pattern().replace("\"", "\\\"") + "\"");
    }

    @Override
    protected String[] getFieldValues(Object value) {
        return AvroUtil.getStringBasedValues(value);
    }

    @Override
    public String toString() {
        return "PatternMatchingRule{" +
                "recordName='" + recordName + '\'' +
                ", fieldName=" + getFieldName() +
                ", pattern=" + pattern +
                '}';
    }
}
