package com.indeed.operators.rabbitmq.model.crd.rabbitmq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonPropertyOrder({"queue"})
@JsonDeserialize(using = JsonDeserializer.None.class)
public class SourceShovelSpec {

    private final String queue;

    @JsonCreator
    public SourceShovelSpec(
            @JsonProperty("queue") final String queue
    ) {
        this.queue = queue;
    }

    public String getQueue() {
        return queue;
    }
}
