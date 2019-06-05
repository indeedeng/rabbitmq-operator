package com.indeed.operators.rabbitmq.model.crd.rabbitmq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.sundr.builder.annotations.Buildable;

@Buildable(
        builderPackage = "io.fabric8.kubernetes.api.builder",
        editableEnabled = false
)
@JsonPropertyOrder({"name", "source", "destination"})
@JsonDeserialize(using = JsonDeserializer.None.class)
public class ShovelSpec {

    private final String name;
    private final SourceShovelSpec source;
    private final DestinationShovelSpec destination;

    @JsonCreator
    public ShovelSpec(
            @JsonProperty("name") final String name,
            @JsonProperty("source") final SourceShovelSpec source,
            @JsonProperty("destination") final DestinationShovelSpec destination
    ) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "Shovel 'name' cannot be empty or null");

        this.name = name;
        this.source = Preconditions.checkNotNull(source, "Shovel 'source' cannot be null");
        this.destination = Preconditions.checkNotNull(destination, "Shovel 'destination' cannot be null");
    }

    public String getName() {
        return name;
    }

    public SourceShovelSpec getSource() {
        return source;
    }

    public DestinationShovelSpec getDestination() {
        return destination;
    }
}