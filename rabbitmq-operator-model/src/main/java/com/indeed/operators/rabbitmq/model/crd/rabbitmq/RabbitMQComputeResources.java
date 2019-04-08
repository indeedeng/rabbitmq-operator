package com.indeed.operators.rabbitmq.model.crd.rabbitmq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Quantity;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;

import java.util.Map;

@Buildable(
        builderPackage = "io.fabric8.kubernetes.api.builder",
        editableEnabled = false
)
@JsonPropertyOrder({"cpuRequest", "cpuLimit", "memory"})
@JsonDeserialize(using = JsonDeserializer.None.class)
public class RabbitMQComputeResources {
    private final Quantity cpuRequest;
    private final Quantity cpuLimit;
    private final Quantity memory;

    @JsonCreator
    public RabbitMQComputeResources(
            @JsonProperty("cpuRequest") final Quantity cpuRequest,
            @JsonProperty("cpuLimit") final Quantity cpuLimit,
            @JsonProperty("memory") final Quantity memory) {

        if (cpuRequest == null && cpuLimit == null) {
            throw new RuntimeException("Must specify one of 'cpuRequest' or 'cpuLimit'");
        }

        this.cpuRequest = cpuRequest;
        this.cpuLimit = cpuLimit;
        this.memory = memory;
    }

    public Quantity getCpuRequest() {
        return cpuRequest;
    }

    public Quantity getCpuLimit() {
        return cpuLimit;
    }

    public Quantity getMemory() {
        return memory;
    }

    public Map<String, Quantity> asResourceRequestQuantityMap() {

        final Quantity cpu = cpuRequest == null ? cpuLimit : cpuRequest;

        return ImmutableMap.<String, Quantity>builder()
                .put("cpu", cpu)
                .put("memory", memory)
                .build();
    }

    public Map<String, Quantity> asResourceLimitQuantityMap() {
        final ImmutableMap.Builder<String, Quantity> limitsBuilder = ImmutableMap.builder();
        if (cpuLimit != null) {
            limitsBuilder.put("cpu", cpuLimit);
        }
        limitsBuilder.put("memory", memory);

        return limitsBuilder.build();
    }
}

