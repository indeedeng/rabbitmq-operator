package com.indeed.operators.rabbitmq.model.rabbitmq;

import com.indeed.operators.rabbitmq.model.crd.rabbitmq.VhostPermissions;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.Secret;

import java.util.List;

public class RabbitMQUser {

    private final String username;
    private final Secret userSecret;
    private final ObjectMeta clusterMetadata;
    private final OwnerReference clusterReference;
    private final List<VhostPermissions> vhostPermissions;
    private final List<String> tags;

    public RabbitMQUser(
            final String username,
            final Secret userSecret,
            final ObjectMeta clusterMetadata,
            final OwnerReference clusterReference,
            final List<VhostPermissions> vhostPermissions,
            final List<String> tags
    ) {
        this.username = username;
        this.userSecret = userSecret;
        this.clusterMetadata = clusterMetadata;
        this.clusterReference = clusterReference;
        this.vhostPermissions = vhostPermissions;
        this.tags = tags;
    }

    public String getUsername() {
        return username;
    }

    public Secret getUserSecret() {
        return userSecret;
    }

    public ObjectMeta getClusterMetadata() {
        return clusterMetadata;
    }

    public OwnerReference getClusterReference() {
        return clusterReference;
    }

    public List<VhostPermissions> getVhostPermissions() {
        return vhostPermissions;
    }

    public List<String> getTags() {
        return tags;
    }
}
