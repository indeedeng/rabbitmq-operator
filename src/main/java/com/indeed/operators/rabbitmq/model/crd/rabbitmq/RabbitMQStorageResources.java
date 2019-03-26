package com.indeed.operators.rabbitmq.model.crd.rabbitmq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.Quantity;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;

@Buildable(
        builderPackage = "io.fabric8.kubernetes.api.builder",
        editableEnabled = false
)
@JsonPropertyOrder({"storageClassName", "storage"})
@JsonDeserialize(using = JsonDeserializer.None.class)
public class RabbitMQStorageResources {
    private final String storageClassName;
    private final Quantity storage;

    @JsonCreator
    public RabbitMQStorageResources(
            @JsonProperty("storageClassName") final String storageClassName,
            @JsonProperty("limit") final Quantity storage
    ) {

        this.storageClassName = storageClassName;
        this.storage = storage;
    }

    public String getStorageClassName() {
        return storageClassName;
    }

    public Quantity getStorage() {
        return storage;
    }
}
