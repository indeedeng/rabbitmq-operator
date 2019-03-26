package com.indeed.operators.rabbitmq.model.crd.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Objects;
import com.indeed.operators.rabbitmq.model.rabbitmq.VHostPermissions;
import io.sundr.builder.annotations.Buildable;

@Buildable(
        builderPackage = "io.fabric8.kubernetes.api.builder",
        editableEnabled = false
)
@JsonPropertyOrder({"name", "permissions"})
@JsonDeserialize(using = JsonDeserializer.None.class)
public class RabbitMQVHostUser {

    private final String name;
    private final VHostPermissions permissions;

    @JsonCreator
    public RabbitMQVHostUser(
            @JsonProperty("name") final String name,
            @JsonProperty("permissions") final VHostPermissions permissions
    ) {
        this.name = name;
        this.permissions = permissions;
    }

    public String getName() {
        return name;
    }

    public VHostPermissions getPermissions() {
        return permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RabbitMQVHostUser that = (RabbitMQVHostUser) o;
        return Objects.equal(name, that.name) &&
                Objects.equal(permissions, that.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, permissions);
    }
}
