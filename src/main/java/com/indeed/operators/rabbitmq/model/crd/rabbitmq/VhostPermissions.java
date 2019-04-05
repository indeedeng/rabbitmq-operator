package com.indeed.operators.rabbitmq.model.crd.rabbitmq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Objects;
import com.indeed.operators.rabbitmq.model.rabbitmq.VhostOperationPermissions;
import io.sundr.builder.annotations.Buildable;

@Buildable(
        builderPackage = "io.fabric8.kubernetes.api.builder",
        editableEnabled = false
)
@JsonPropertyOrder({"vhostName", "permissions"})
@JsonDeserialize(using = JsonDeserializer.None.class)
public class VhostPermissions {

    private final String vhostName;
    private final VhostOperationPermissions permissions;

    @JsonCreator
    public VhostPermissions(
            @JsonProperty("vhostName") final String vhostName,
            @JsonProperty("permissions") final VhostOperationPermissions permissions
    ) {
        this.vhostName = vhostName;
        this.permissions = permissions;
    }

    public String getVhostName() {
        return vhostName;
    }

    public VhostOperationPermissions getPermissions() {
        return permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VhostPermissions that = (VhostPermissions) o;
        return Objects.equal(vhostName, that.vhostName) &&
                Objects.equal(permissions, that.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(vhostName, permissions);
    }
}
