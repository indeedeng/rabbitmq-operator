package com.indeed.operators.rabbitmq.model.rabbitmq.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = JsonDeserializer.None.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueueState {
    private final String name;
    private final int messageCount;

    @JsonCreator
    public QueueState(
            @JsonProperty("name") final String name,
            @JsonProperty("messages") final int messageCount
    ) {
        this.name = name;
        this.messageCount = messageCount;
    }

    public String getName() {
        return name;
    }

    public int getMessageCount() {
        return messageCount;
    }
}
