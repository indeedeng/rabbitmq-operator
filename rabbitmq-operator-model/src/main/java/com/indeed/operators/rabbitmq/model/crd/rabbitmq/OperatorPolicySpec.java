package com.indeed.operators.rabbitmq.model.crd.rabbitmq;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OperatorPolicySpec {

    private String vhost;
    private String name;
    private String pattern;
    private String applyTo;
    private OperatorPolicyDefinitionSpec definition;
    private long priority;

    public OperatorPolicySpec(
            @JsonProperty("vhost") final String vhost,
            @JsonProperty("name") final String name,
            @JsonProperty("pattern") final String pattern,
            @JsonProperty("applyTo") final String applyTo,
            @JsonProperty("definition") final OperatorPolicyDefinitionSpec definition,
            @JsonProperty("priority") final long priority
    ) {
        this.vhost = vhost;
        this.name = name;
        this.pattern = pattern;
        this.applyTo = applyTo;
        this.definition = definition;
        this.priority = priority;
    }

    public String getVhost() {
        return vhost;
    }

    public String getName() {
        return name;
    }

    public String getPattern() {
        return pattern;
    }

    public String getApplyTo() {
        return applyTo;
    }

    public OperatorPolicyDefinitionSpec getDefinition() {
        return definition;
    }

    public long getPriority() {
        return priority;
    }
}
