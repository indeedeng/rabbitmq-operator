package com.indeed.operators.rabbitmq.model.crd.rabbitmq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.sundr.builder.annotations.Buildable;

@Buildable(
        builderPackage = "io.fabric8.kubernetes.api.builder",
        editableEnabled = false
)
@JsonPropertyOrder({"highWatermarkFraction", "shovelSpec"})
@JsonDeserialize(using = JsonDeserializer.None.class)
public class ClusterSpec {

    private final double highWatermarkFraction;
    private final ShovelSpec shovelSpec;

    @JsonCreator
    public ClusterSpec(
            @JsonProperty("highWatermarkFraction") final double highWatermarkFraction,
            @JsonProperty("shovelSpec") final ShovelSpec shovelSpec
    ) {
        this.highWatermarkFraction = highWatermarkFraction;
        this.shovelSpec = shovelSpec;
    }

    public double getHighWatermarkFraction() {
        return highWatermarkFraction;
    }

    public ShovelSpec getShovelSpec() {
        return shovelSpec;
    }
}
