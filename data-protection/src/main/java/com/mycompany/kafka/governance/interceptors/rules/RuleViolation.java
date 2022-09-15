package com.mycompany.kafka.governance.interceptors.rules;

public class RuleViolation {

    public final String record;
    public final String field;
    public final String message;

    public RuleViolation(String record, String field, String message) {
        this.record = record;
        this.field = field;
        this.message = message;
    }
}
