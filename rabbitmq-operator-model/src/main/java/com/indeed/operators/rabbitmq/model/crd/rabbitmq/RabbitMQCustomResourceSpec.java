package com.indeed.operators.rabbitmq.model.crd.rabbitmq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.sundr.builder.annotations.Buildable;

/**
 * See https://github.com/fabric8io/kubernetes-client/tree/master/kubernetes-examples/src/main/java/io/fabric8/kubernetes/examples/crds
 */
@Buildable(
        builderPackage = "io.fabric8.kubernetes.api.builder",
        editableEnabled = false
)
@JsonPropertyOrder({"rabbitMQImage", "initContainerImage", "createLoadBalancer", "replicas", "compute", "storage", "highWatermarkFraction"})
@JsonDeserialize(using = JsonDeserializer.None.class)
public class RabbitMQCustomResourceSpec {
    private final String rabbitMQImage;
    private final String initContainerImage;
    private final boolean createLoadBalancer;
    private final int replicas;
    private final RabbitMQComputeResources computeResources;
    private final RabbitMQStorageResources storageResources;
    private final ClusterSpec clusterSpec;
    private final boolean preserveOrphanPVCs;

    @JsonCreator
    public RabbitMQCustomResourceSpec(
            @JsonProperty("rabbitMQImage") final String rabbitMQImage,
            @JsonProperty("initContainerImage") final String initContainerImage,
            @JsonProperty("createLoadBalancer") final boolean createLoadBalancer,
            @JsonProperty("replicas") final int replicas,
            @JsonProperty("compute") final RabbitMQComputeResources computeResources,
            @JsonProperty("storage") final RabbitMQStorageResources storageResources,
            @JsonProperty("clusterSpec") final ClusterSpec clusterSpec,
            @JsonProperty(value = "preserveOrphanPVCs", defaultValue = "false") final boolean preserveOrphanPVCs
    ) {
        this.rabbitMQImage = rabbitMQImage;
        this.initContainerImage = initContainerImage;
        this.createLoadBalancer = createLoadBalancer;
        this.replicas = replicas;
        this.computeResources = computeResources;
        this.clusterSpec = clusterSpec;
        this.storageResources = storageResources;
        this.preserveOrphanPVCs = preserveOrphanPVCs;
    }

    public String getRabbitMQImage() {
        return rabbitMQImage;
    }

    public String getInitContainerImage() {
        return initContainerImage;
    }

    public boolean isCreateLoadBalancer() {
        return createLoadBalancer;
    }

    public int getReplicas() {
        return replicas;
    }

    public RabbitMQComputeResources getComputeResources() {
        return computeResources;
    }

    public RabbitMQStorageResources getStorageResources() {
        return storageResources;
    }

    public ClusterSpec getClusterSpec() {
        return clusterSpec;
    }

    public boolean isPreserveOrphanPVCs() {
        return preserveOrphanPVCs;
    }
}
