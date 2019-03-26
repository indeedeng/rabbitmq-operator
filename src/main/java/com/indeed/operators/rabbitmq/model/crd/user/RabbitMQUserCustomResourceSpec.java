package com.indeed.operators.rabbitmq.model.crd.user;

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
@JsonPropertyOrder({"clusterName", "vHosts", "tags"})
@JsonDeserialize(using = JsonDeserializer.None.class)
public class RabbitMQUserCustomResourceSpec {
    final String clusterName;
    final List<RabbitMQVHostUser> vHosts;
    final List<String> tags;

    @JsonCreator
    public RabbitMQUserCustomResourceSpec(
        @JsonProperty("clusterName") final String clusterName,
        @JsonProperty("vHosts") final List<RabbitMQVHostUser> vHosts,
        @JsonProperty("tags") final List<String> tags
    ) {
        this.clusterName = clusterName;
        this.vHosts = ImmutableList.copyOf(vHosts);
        this.tags = ImmutableList.copyOf(tags);
    }

    public String getClusterName() {
        return clusterName;
    }

    public List<RabbitMQVHostUser> getVHosts() {
        return vHosts;
    }

    public List<String> getTags() {
        return tags;
    }
}
