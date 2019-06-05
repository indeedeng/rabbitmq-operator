package com.indeed.operators.rabbitmq.model.crd.rabbitmq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

@JsonPropertyOrder({"queue", "vhost"})
@JsonDeserialize(using = JsonDeserializer.None.class)
public class SourceShovelSpec {

    private final String queue;
    private final String vhost;

    @JsonCreator
    public SourceShovelSpec(
            @JsonProperty("queue") final String queue,
            @JsonProperty("vhost") final String vhost
    ) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(queue), "Shovel source 'queue' must not be empty or null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(queue), "Shovel source 'vhost' must not be empty or null");
        this.queue = queue;
        this.vhost = vhost;
    }

    public String getQueue() {
        return queue;
    }

    public String getVhost() {
        return vhost;
    }
}
