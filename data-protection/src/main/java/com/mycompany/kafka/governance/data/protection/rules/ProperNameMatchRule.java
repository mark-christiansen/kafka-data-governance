package com.mycompany.kafka.governance.data.protection.rules;

import com.mycompany.kafka.governance.data.protection.RuleViolation;
import com.mycompany.kafka.governance.data.protection.util.AvroUtil;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class ProperNameMatchRule extends FieldMatchingRuleImpl<String> {

    private static final String ENGLISH_NAME_MODEL_FILE = "opennlp/en-ner-person.bin";

    private static final Logger log = LoggerFactory.getLogger(ProperNameMatchRule.class);

    private NameFinderME nameFinder;

    public ProperNameMatchRule(String recordName, String fieldName) {
        super(recordName, fieldName);
        setupNameFinder();
    }

    @Override
    protected boolean test(String[] fieldValues) {
        for (String fieldValue : fieldValues) {
            // split string by spaces to detect full name separated by space
            Span[] nameSpans = this.nameFinder.find(fieldValue.split(" "));
            if (nameSpans.length > 0) {
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
        return new RuleViolation(getRecordName(), getFieldName(), "Value was recognized as a proper name");
    }

    private void setupNameFinder() {

        try (InputStream in = ProperNameMatchRule.class.getClassLoader()
                .getResourceAsStream(ENGLISH_NAME_MODEL_FILE)) {

            // load the model from file
            TokenNameFinderModel model = new TokenNameFinderModel(in);
            this.nameFinder = new NameFinderME(model);

        } catch (IOException e) {
            log.error("Error loading name model file", e);
            throw new RuntimeException("Error loading name model file", e);
        }
    }

    @Override
    public String toString() {
        return "ProperNameMatchingRule{" +
                "recordName='" + recordName + '\'' +
                ", fieldName=" + getFieldName() +
                '}';
    }
}
