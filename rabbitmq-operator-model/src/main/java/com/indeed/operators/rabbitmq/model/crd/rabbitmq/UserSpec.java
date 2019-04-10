package com.indeed.operators.rabbitmq.model.crd.rabbitmq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import io.sundr.builder.annotations.Buildable;

import java.util.List;

/**
 * See https://github.com/fabric8io/kubernetes-client/tree/master/kubernetes-examples/src/main/java/io/fabric8/kubernetes/examples/crds
 */
@Buildable(
        builderPackage = "io.fabric8.kubernetes.api.builder",
        editableEnabled = false
)
@JsonPropertyOrder({"username", "vhosts", "tags"})
@JsonDeserialize(using = JsonDeserializer.None.class)
public class UserSpec {
    private final String username;
    private final List<VhostPermissions> vhosts;
    private final List<String> tags;

    @JsonCreator
    public UserSpec(
            @JsonProperty("username") final String username,
            @JsonProperty("vhosts") final List<VhostPermissions> vhosts,
            @JsonProperty("tags") final List<String> tags
    ) {
        this.username = username;
        this.vhosts = ImmutableList.copyOf(vhosts);
        this.tags = ImmutableList.copyOf(tags);
    }

    public String getUsername() {
        return username;
    }

    public List<VhostPermissions> getVhosts() {
        return vhosts;
    }

    public List<String> getTags() {
        return tags;
    }
}
