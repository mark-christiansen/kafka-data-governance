package com.mycompany.kafka.governance.data.protection.rules;

import com.mycompany.kafka.governance.data.protection.RuleViolation;
import com.mycompany.kafka.governance.data.protection.util.AvroUtil;
import com.mycompany.kafka.governance.data.protection.util.Entropy;

/**
 * @deprecated
 * The idea of this rule was to evaluate whether a string was encrypted based on how random the characters are in the
 * string by measuring the string's entropy. This is also failed to produce consistent results.
 */
@Deprecated
public class EntropyRule extends FieldMatchingRuleImpl<String> {

    private static final double MINIMUM_ENTROPY = 1.25;

    public EntropyRule(String recordName, String fieldName) {
        super(recordName, fieldName);
    }

    @Override
    protected boolean test(String[] fieldValue) {

        for (String value : fieldValue) {
            if (Entropy.calculateEntropy(value) > MINIMUM_ENTROPY) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected String[] getFieldValues(Object value) {
        return AvroUtil.getStringBasedValues(value);
    }

    @Override
    public RuleViolation getViolationMessage() {
        return new RuleViolation(getRecordName(), getFieldName(), "Value was recognized as not encrypted because of its low entropy");
    }
}
