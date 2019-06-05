package com.indeed.operators.rabbitmq.model.crd.rabbitmq;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

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
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "Operator policy 'name' cannot be empty or null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(vhost), "Operator policy 'vhost' cannot be empty or null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(pattern), "Operator policy 'pattern' cannot be empty or null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(applyTo), "Operator policy 'applyTo' cannot be empty or null");
        Preconditions.checkArgument(priority >= 0, "Operator policy 'priority' must be greater than or equal to 0");

        this.vhost = vhost;
        this.name = name;
        this.pattern = pattern;
        this.applyTo = applyTo;
        this.definition = Preconditions.checkNotNull(definition, "Operator policy 'definition' cannot be null");
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
