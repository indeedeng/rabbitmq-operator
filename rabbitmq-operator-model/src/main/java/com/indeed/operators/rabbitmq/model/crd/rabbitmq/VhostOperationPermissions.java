package com.indeed.operators.rabbitmq.model.crd.rabbitmq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Objects;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class VhostOperationPermissions {

    private final String configure;
    private final String write;
    private final String read;

    @JsonCreator
    public VhostOperationPermissions(
            @JsonProperty("configure") final String configure,
            @JsonProperty("write") final String write,
            @JsonProperty("read") final String read
    ) {
        this.configure = configure;
        this.write = write;
        this.read = read;
    }

    public String getConfigure() {
        return configure;
    }

    public String getWrite() {
        return write;
    }

    public String getRead() {
        return read;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VhostOperationPermissions that = (VhostOperationPermissions) o;
        return Objects.equal(configure, that.configure) &&
                Objects.equal(write, that.write) &&
                Objects.equal(read, that.read);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(configure, write, read);
    }

    @Override
    public String toString() {
        return String.format("{%s, %s, %s}", configure, write, read);
    }
}
