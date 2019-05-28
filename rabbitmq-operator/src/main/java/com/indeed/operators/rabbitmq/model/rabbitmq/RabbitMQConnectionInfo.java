package com.indeed.operators.rabbitmq.model.rabbitmq;

import com.google.common.base.Objects;

import java.util.Optional;

public class RabbitMQConnectionInfo {

    private final String clusterName;
    private final String namespace;
    private final String serviceName;
    private final Optional<String> nodeName;

    public RabbitMQConnectionInfo(
            final String clusterName,
            final String namespace,
            final String serviceName
    ) {
        this(clusterName, namespace, serviceName, null);
    }

    public RabbitMQConnectionInfo(
            final String clusterName,
            final String namespace,
            final String serviceName,
            final String nodeName
    ) {
        this.clusterName = clusterName;
        this.namespace = namespace;
        this.serviceName = serviceName;
        this.nodeName = Optional.ofNullable(nodeName);
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Optional<String> getNodeName() {
        return nodeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RabbitMQConnectionInfo that = (RabbitMQConnectionInfo) o;
        return Objects.equal(clusterName, that.clusterName) &&
                Objects.equal(namespace, that.namespace) &&
                Objects.equal(serviceName, that.serviceName) &&
                Objects.equal(nodeName, that.nodeName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(clusterName, namespace, serviceName, nodeName);
    }

    public static RabbitMQConnectionInfo fromCluster(final RabbitMQCluster cluster) {
        return new RabbitMQConnectionInfo(cluster.getName(), cluster.getNamespace(), cluster.getDiscoveryService().getMetadata().getName());
    }
}
