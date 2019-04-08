package com.indeed.operators.rabbitmq.model.rabbitmq;

import com.indeed.operators.rabbitmq.model.crd.rabbitmq.ShovelSpec;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.policy.PodDisruptionBudget;

import java.util.List;
import java.util.Optional;

public class RabbitMQCluster {

    private final String name;
    private final String namespace;
    private final Secret adminSecret;
    private final Secret erlangCookieSecret;
    private final Service mainService;
    private final Service discoveryService;
    private final Optional<Service> loadBalancerService;
    private final StatefulSet statefulSet;
    private final PodDisruptionBudget podDisruptionBudget;
    private final List<ShovelSpec> shovels;
    private final List<RabbitMQUser> users;

    public RabbitMQCluster(
            final String name,
            final String namespace,
            final Secret adminSecret,
            final Secret erlangCookieSecret,
            final Service mainService,
            final Service discoveryService,
            final Optional<Service> loadBalancerService,
            final StatefulSet statefulSet,
            final PodDisruptionBudget podDisruptionBudget,
            final List<ShovelSpec> shovels,
            final List<RabbitMQUser> users
    ) {
        this.name = name;
        this.namespace = namespace;
        this.adminSecret = adminSecret;
        this.erlangCookieSecret = erlangCookieSecret;
        this.mainService = mainService;
        this.discoveryService = discoveryService;
        this.loadBalancerService = loadBalancerService;
        this.statefulSet = statefulSet;
        this.podDisruptionBudget = podDisruptionBudget;
        this.shovels = shovels;
        this.users = users;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public Secret getAdminSecret() {
        return adminSecret;
    }

    public Secret getErlangCookieSecret() {
        return erlangCookieSecret;
    }

    public Service getMainService() {
        return mainService;
    }

    public Service getDiscoveryService() {
        return discoveryService;
    }

    public Optional<Service> getLoadBalancerService() {
        return loadBalancerService;
    }

    public StatefulSet getStatefulSet() {
        return statefulSet;
    }

    public PodDisruptionBudget getPodDisruptionBudget() {
        return podDisruptionBudget;
    }

    public List<ShovelSpec> getShovels() {
        return shovels;
    }

    public List<RabbitMQUser> getUsers() {
        return users;
    }
}
