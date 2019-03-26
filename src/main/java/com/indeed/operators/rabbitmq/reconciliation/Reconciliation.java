package com.indeed.operators.rabbitmq.reconciliation;

import com.google.common.base.Objects;

public class Reconciliation {

    private final String resourceName;
    private final String clusterName;
    private final String namespace;
    private final String type;

    public Reconciliation(final String resourceName, final String clusterName, final String namespace, final String type) {
        this.resourceName = resourceName;
        this.clusterName = clusterName;
        this.namespace = namespace;
        this.type = type;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("{ resourceName: [%s], clusterName: [%s], namespace: [%s], type: [%s]", resourceName, clusterName, namespace, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reconciliation that = (Reconciliation) o;
        return Objects.equal(resourceName, that.resourceName) &&
                Objects.equal(clusterName, that.clusterName) &&
                Objects.equal(namespace, that.namespace) &&
                Objects.equal(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(resourceName, clusterName, namespace, type);
    }
}
