package com.indeed.operators.rabbitmq.model.crd.rabbitmq;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.indeed.rabbitmq.admin.pojo.OperatorPolicyDefinition;

public class OperatorPolicyDefinitionSpec {

    private final Long expires;
    private final Long maxLength;
    private final Long maxLengthBytes;
    private final Long messageTtl;

    public OperatorPolicyDefinitionSpec(
            @JsonProperty("expires") final Long expires,
            @JsonProperty("max-length") final Long maxLength,
            @JsonProperty("max-length-bytes") final Long maxLengthBytes,
            @JsonProperty("message-ttl") final Long messageTtl
    ) {
        this.expires = expires;
        this.maxLength = maxLength;
        this.maxLengthBytes = maxLengthBytes;
        this.messageTtl = messageTtl;
    }

    public Long getExpires() {
        return expires;
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

    @JsonIgnore
    public OperatorPolicyDefinition asOperatorPolicyDefinition() {
        OperatorPolicyDefinition definition = new OperatorPolicyDefinition();

        if (expires != null) {
            definition = definition.withExpires(getExpires());
        }

        if (maxLength != null) {
            definition = definition.withExpires(getMaxLength());
        }

        if (maxLengthBytes != null) {
            definition = definition.withExpires(getMaxLengthBytes());
        }

        if (messageTtl != null) {
            definition = definition.withExpires(getMessageTtl());
        }

        return definition;
    }
}
