package com.mycompany.kafka.governance.interceptors;

import com.mycompany.kafka.governance.interceptors.rules.RuleViolation;

public class ProducerRuleViolation {

    public final String topic;
    public final Integer partition;
    public final Long timestamp;
    public final String clientId;
    public final RuleViolation violation;

    public ProducerRuleViolation(String topic, Integer partition, Long timestamp, String clientId, RuleViolation violation) {
        this.topic = topic;
        this.partition = partition;
        this.timestamp = timestamp;
        this.clientId = clientId;
        this.violation = violation;
    }
}
