package com.mycompany.kafka.governance.interceptors;

import com.mycompany.kafka.governance.interceptors.rules.RuleViolation;

public class ConsumerRuleViolation {

    public final String topic;
    public final int partition;
    public final long offset;
    public final long timestamp;
    public final String clientId;
    public final String groupId;
    public final RuleViolation violation;

    public ConsumerRuleViolation(String topic, int partition, long offset, long timestamp, String clientId, String groupId, RuleViolation violation) {
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.timestamp = timestamp;
        this.clientId = clientId;
        this.groupId = groupId;
        this.violation = violation;
    }
}
