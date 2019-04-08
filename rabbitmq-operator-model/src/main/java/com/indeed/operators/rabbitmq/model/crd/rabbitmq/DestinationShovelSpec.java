package com.indeed.operators.rabbitmq.model.crd.rabbitmq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"addresses", "clusterName"})
public class DestinationShovelSpec {

    private final List<String> addresses;
    private final String secretName;
    private final String secretNamespace;

    @JsonCreator
    public DestinationShovelSpec(
            @JsonProperty("addresses") final List<String> addresses,
            @JsonProperty("secretName") final String secretName,
            @JsonProperty("secretNamespace") final String secretNamespace
    ) {
        this.addresses = addresses;
        this.secretName = secretName;
        this.secretNamespace = secretNamespace;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public String getSecretName() {
        return secretName;
    }

    public String getSecretNamespace() {
        return secretNamespace;
    }
}
