package com.indeed.operators.rabbitmq.model.rabbitmq.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.List;

@JsonDeserialize(builder = ShovelParameterValue.Builder.class)
public class ShovelParameterValue extends AbstractParameterValue {
    private final String sourceProtocol;
    private final String sourceUri;
    private final String sourceQueue;
    private final String destinationProtocol;
    private final List<String> destinationUris;

    private ShovelParameterValue(
            final String sourceProtocol,
            final String sourceUri,
            final String sourceQueue,
            final String destinationProtocol,
            final List<String> destinationUris
    ) {
        this.sourceProtocol = sourceProtocol;
        this.sourceUri = sourceUri;
        this.sourceQueue = sourceQueue;
        this.destinationProtocol = destinationProtocol;
        this.destinationUris = destinationUris;
    }

    @JsonProperty("src-protocol")
    public String getSourceProtocol() {
        return sourceProtocol;
    }

    @JsonProperty("src-uri")
    public String getSourceUri() {
        return sourceUri;
    }

    @JsonProperty("src-queue")
    public String getSourceQueue() {
        return sourceQueue;
    }

    @JsonProperty("dest-protocol")
    public String getDestinationProtocol() {
        return destinationProtocol;
    }

    @JsonProperty("dest-uri")
    public List<String> getDestinationUris() {
        return destinationUris;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShovelParameterValue that = (ShovelParameterValue) o;
        return Objects.equal(sourceProtocol, that.sourceProtocol) &&
                Objects.equal(sourceUri, that.sourceUri) &&
                Objects.equal(sourceQueue, that.sourceQueue) &&
                Objects.equal(destinationProtocol, that.destinationProtocol) &&
                Objects.equal(destinationUris, that.destinationUris);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sourceProtocol, sourceUri, sourceQueue, destinationProtocol, destinationUris);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    @JsonIgnore
    public String getComponent() {
        return "shovel";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        private String sourceProtocol = "amqp091";
        private String sourceUri = "amqp://";
        private String sourceQueue;
        private String destinationProtocol = "amqp091";
        private List<String> destinationUri = Lists.newArrayList();

        @JsonProperty("src-protocol")
        public Builder setSourceProtocol(final String sourceProtocol) {
            this.sourceProtocol = sourceProtocol;
            return this;
        }

        @JsonProperty("src-uri")
        public Builder setSourceUri(final String sourceUri) {
            this.sourceUri = sourceUri;
            return this;
        }

        @JsonProperty("src-queue")
        public Builder setSourceQueue(final String sourceQueue) {
            this.sourceQueue = sourceQueue;
            return this;
        }

        @JsonProperty("dest-protocol")
        public Builder setDestinationProtocol(final String destinationProtocol) {
            this.destinationProtocol = destinationProtocol;
            return this;
        }

        @JsonProperty("dest-uri")
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        public Builder setDestinationUri(final List<String> destinationUri) {
            this.destinationUri = destinationUri;
            return this;
        }

        public ShovelParameterValue build() {
            return new ShovelParameterValue(sourceProtocol, sourceUri, sourceQueue, destinationProtocol, destinationUri);
        }
    }
}