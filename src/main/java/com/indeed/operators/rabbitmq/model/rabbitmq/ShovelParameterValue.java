package com.indeed.operators.rabbitmq.model.rabbitmq;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Objects;

@JsonDeserialize(builder = ShovelParameterValue.Builder.class)
public class ShovelParameterValue extends AbstractParameterValue {
    private final String sourceProtocol;
    private final String sourceUri;
    private final String sourceQueue;
    private final String destinationProtocol;
    private final String destinationUri;

    private ShovelParameterValue(
            final String sourceProtocol,
            final String sourceUri,
            final String sourceQueue,
            final String destinationProtocol,
            final String destinationUri
    ) {
        this.sourceProtocol = sourceProtocol;
        this.sourceUri = sourceUri;
        this.sourceQueue = sourceQueue;
        this.destinationProtocol = destinationProtocol;
        this.destinationUri = destinationUri;
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
    public String getDestinationUri() {
        return destinationUri;
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
                Objects.equal(destinationUri, that.destinationUri);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sourceProtocol, sourceUri, sourceQueue, destinationProtocol, destinationUri);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String getComponent() {
        return "shovel";
    }


    public static class Builder {
        private String sourceProtocol;
        private String sourceUri;
        private String sourceQueue;
        private String destinationProtocol;
        private String destinationUri;

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
        public Builder setDestinationUri(final String destinationUri) {
            this.destinationUri = destinationUri;
            return this;
        }

        public ShovelParameterValue build() {
            return new ShovelParameterValue(sourceProtocol, sourceUri, sourceQueue, destinationProtocol, destinationUri);
        }
    }
}