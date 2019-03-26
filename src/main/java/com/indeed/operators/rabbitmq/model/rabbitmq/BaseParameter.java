package com.indeed.operators.rabbitmq.model.rabbitmq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Objects;

public class BaseParameter<T extends AbstractParameterValue> {

    private final T value;
    private final String vhost;
    private final String component;
    private final String name;

    public BaseParameter(
            final T value,
            final String vhost,
            final String name
    ) {
        this(value, vhost, value.getComponent(), name);
    }

    @JsonCreator
    private BaseParameter(
            @JsonProperty("value")
            @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "component")
            @JsonSubTypes(value = {
                    @JsonSubTypes.Type(value = ShovelParameterValue.class, name = "shovel")
            }) final T value,
            @JsonProperty("vhost") final String vhost,
            @JsonProperty("component") final String component,
            @JsonProperty("name") final String name
    ) {
        this.value = value;
        this.vhost = vhost;
        this.component = component;
        this.name = name;
    }

    public T getValue() {
        return value;
    }

    public String getVhost() {
        return vhost;
    }

    public String getComponent() {
        return component;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseParameter<?> that = (BaseParameter<?>) o;
        return Objects.equal(value, that.value) &&
                Objects.equal(vhost, that.vhost) &&
                Objects.equal(component, that.component) &&
                Objects.equal(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value, vhost, component, name);
    }
}
