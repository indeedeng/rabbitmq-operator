package com.indeed.operators.rabbitmq.model.crd.rabbitmq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"address", "clusterName"})
public class DestinationShovelSpec {

    private final String address;
    private final String clusterName;
    private final String clusterNamespace;

    @JsonCreator
    public DestinationShovelSpec(
            @JsonProperty("address") final String address,
            @JsonProperty("clusterName") final String clusterName,
            @JsonProperty("clusterNamespace") final String clusterNamespace
    ) {
        this.address = address;
        this.clusterName = clusterName;
        this.clusterNamespace = clusterNamespace;
    }

    public String getAddress() {
        return address;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getClusterNamespace() {
        return clusterNamespace;
    }
}
