package com.indeed.operators.rabbitmq.model.crd.rabbitmq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Lists;
import io.sundr.builder.annotations.Buildable;

import java.util.List;

@Buildable(
        builderPackage = "io.fabric8.kubernetes.api.builder",
        editableEnabled = false
)
@JsonPropertyOrder({"highWatermarkFraction", "users", "shovels", "policies"})
@JsonDeserialize(using = JsonDeserializer.None.class)
public class ClusterSpec {

    private final double highWatermarkFraction;
    private final List<UserSpec> users;
    private final List<ShovelSpec> shovels;
    private final List<PolicySpec> policies;

    @JsonCreator
    public ClusterSpec(
            @JsonProperty("highWatermarkFraction") final double highWatermarkFraction,
            @JsonProperty("users") final List<UserSpec> users,
            @JsonProperty("shovels") final List<ShovelSpec> shovels,
            @JsonProperty("policies") final List<PolicySpec> policies
    ) {
        this.highWatermarkFraction = highWatermarkFraction;
        this.users = (users == null ? Lists.newArrayList() : users);
        this.shovels = (shovels == null ? Lists.newArrayList() : shovels);
        this.policies = (policies == null ? Lists.newArrayList() : policies);
    }

    public double getHighWatermarkFraction() {
        return highWatermarkFraction;
    }

    public List<UserSpec> getUsers() {
        return users;
    }

    public List<ShovelSpec> getShovels() {
        return shovels;
    }

    public List<PolicySpec> getPolicies() {
        return policies;
    }
}
