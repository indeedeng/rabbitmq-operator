package com.indeed.operators.rabbitmq.model.crd.rabbitmq;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.indeed.rabbitmq.admin.pojo.Definition;

public class PolicyDefinitionSpec {

    private final String alternateExchange;
    private final String deadLetterExchange;
    private final String deadLetterRoutingKey;
    private final Long expires;
    private final String haMode;
    private final Long haParams;
    private final String haPromoteOnShutdown;
    private final Long haSyncBatchSize;
    private final String haSyncMode;
    private final Long maxLength;
    private final Long maxLengthBytes;
    private final Long messageTtl;
    private final String queueMasterLocator;

    public PolicyDefinitionSpec(
            @JsonProperty("alternate-exchange") final String alternateExchange,
            @JsonProperty("dead-letter-exchange") final String deadLetterExchange,
            @JsonProperty("dead-letter-routing-key") final String deadLetterRoutingKey,
            @JsonProperty("expires") final Long expires,
            @JsonProperty("ha-mode") final String haMode,
            @JsonProperty("ha-params") final Long haParams,
            @JsonProperty("ha-promote-on-shutdown") final String haPromoteOnShutdown,
            @JsonProperty("ha-sync-batch-size") final Long haSyncBatchSize,
            @JsonProperty("ha-sync-mode") final String haSyncMode,
            @JsonProperty("max-length") final Long maxLength,
            @JsonProperty("max-length-bytes") final Long maxLengthBytes,
            @JsonProperty("message-ttl") final Long messageTtl,
            @JsonProperty("queue-master-locator") final String queueMasterLocator
    ) {
        this.alternateExchange = alternateExchange;
        this.deadLetterExchange = deadLetterExchange;
        this.deadLetterRoutingKey = deadLetterRoutingKey;
        this.expires = expires;
        this.haMode = haMode;
        this.haParams = haParams;
        this.haPromoteOnShutdown = haPromoteOnShutdown;
        this.haSyncBatchSize = haSyncBatchSize;
        this.haSyncMode = haSyncMode;
        this.maxLength = maxLength;
        this.maxLengthBytes = maxLengthBytes;
        this.messageTtl = messageTtl;
        this.queueMasterLocator = queueMasterLocator;
    }

    public String getAlternateExchange() {
        return alternateExchange;
    }

    public String getDeadLetterExchange() {
        return deadLetterExchange;
    }

    public String getDeadLetterRoutingKey() {
        return deadLetterRoutingKey;
    }

    public Long getExpires() {
        return expires;
    }

    public String getHaMode() {
        return haMode;
    }

    public Long getHaParams() {
        return haParams;
    }

    public String getHaPromoteOnShutdown() {
        return haPromoteOnShutdown;
    }

    public Long getHaSyncBatchSize() {
        return haSyncBatchSize;
    }

    public String getHaSyncMode() {
        return haSyncMode;
    }

    public Long getMaxLength() {
        return maxLength;
    }

    public Long getMaxLengthBytes() {
        return maxLengthBytes;
    }

    public Long getMessageTtl() {
        return messageTtl;
    }

    public String getQueueMasterLocator() {
        return queueMasterLocator;
    }

    @JsonIgnore
    public Definition asDefinition() {
        Definition definition = new Definition();

        if (getAlternateExchange() != null) {
            definition = definition.withAlternateExchange(getAlternateExchange());
        }

        if (getDeadLetterExchange() != null) {
            definition = definition.withAlternateExchange(getDeadLetterExchange());
        }

        if (getDeadLetterRoutingKey() != null) {
            definition = definition.withAlternateExchange(getDeadLetterRoutingKey());
        }

        if (getExpires() != null) {
            definition = definition.withExpires(getExpires());
        }

        if (getHaMode() != null) {
            definition = definition.withHaMode(Definition.HaMode.fromValue(getHaMode()));
        }

        if (getHaParams() != null) {
            definition = definition.withHaParams(getHaParams());
        }

        if (getHaPromoteOnShutdown() != null) {
            definition = definition.withHaPromoteOnShutdown(Definition.HaPromoteOnShutdown.fromValue(getHaPromoteOnShutdown()));
        }

        if (getHaSyncBatchSize() != null) {
            definition = definition.withHaSyncBatchSize(getHaSyncBatchSize());
        }

        if (getHaSyncMode() != null) {
            definition = definition.withHaSyncMode(Definition.HaSyncMode.fromValue(getHaSyncMode()));
        }

        if (getMaxLength() != null) {
            definition = definition.withMaxLength(getMaxLength());
        }

        if (getMaxLengthBytes() != null) {
            definition = definition.withMaxLengthBytes(getMaxLengthBytes());
        }

        if (getMessageTtl() != null) {
            definition = definition.withMessageTtl(getMessageTtl());
        }

        if (getQueueMasterLocator() != null) {
            definition = definition.withQueueMasterLocator(Definition.QueueMasterLocator.fromValue(getQueueMasterLocator()));
        }

        return definition;
    }
}
