package com.indeed.operators.rabbitmq.model.crd.partition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.sundr.builder.annotations.Buildable;

import java.util.List;
import java.util.Set;

/**
 * See https://github.com/fabric8io/kubernetes-client/tree/master/kubernetes-examples/src/main/java/io/fabric8/kubernetes/examples/crds
 */
@Buildable(
        builderPackage = "io.fabric8.kubernetes.api.builder",
        editableEnabled = false
)
@JsonPropertyOrder({"clusterName", "partitions", "drained", "serviceName"})
@JsonDeserialize(using = JsonDeserializer.None.class)
public class RabbitMQNetworkPartitionCustomResourceSpec {
    private final String clusterName;
    private final List<Set<String>> partitions;
    private final List<Set<String>> drained;
    private final String serviceName;

    @JsonCreator
    public RabbitMQNetworkPartitionCustomResourceSpec(
            @JsonProperty("clusterName") final String clusterName,
            @JsonProperty("partitions") final List<Set<String>> partitions,
            @JsonProperty("drained") final List<Set<String>> drained,
            @JsonProperty("serviceName") final String serviceName
    ) {
        this.clusterName = clusterName;
        this.partitions = partitions;
        this.drained = drained;
        this.serviceName = serviceName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public List<Set<String>> getPartitions() {
        return partitions;
    }

    public List<Set<String>> getDrained() {
        return drained;
    }

    public String getServiceName() {
        return serviceName;
    }
}
