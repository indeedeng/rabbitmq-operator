package com.indeed.operators.rabbitmq.model.rabbitmq;

import com.indeed.operators.rabbitmq.model.crd.rabbitmq.OperatorPolicySpec;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.PolicySpec;
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
    private final List<PolicySpec> policies;
    private final List<OperatorPolicySpec> operatorPolicies;

    private RabbitMQCluster(
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
            final List<RabbitMQUser> users,
            final List<PolicySpec> policies,
            final List<OperatorPolicySpec> operatorPolicies
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
        this.policies = policies;
        this.operatorPolicies = operatorPolicies;
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

    public List<PolicySpec> getPolicies() {
        return policies;
    }

    public List<OperatorPolicySpec> getOperatorPolicies() {
        return operatorPolicies;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String namespace;
        private Secret adminSecret;
        private Secret erlangCookieSecret;
        private Service mainService;
        private Service discoveryService;
        private Optional<Service> loadBalancerService;
        private StatefulSet statefulSet;
        private PodDisruptionBudget podDisruptionBudget;
        private List<ShovelSpec> shovels;
        private List<RabbitMQUser> users;
        private List<PolicySpec> policies;
        private List<OperatorPolicySpec> operatorPolicies;

        public Builder withName(final String name) {
            this.name = name;
            return this;
        }

        public Builder withNamespace(final String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Builder withAdminSecret(final Secret adminSecret) {
            this.adminSecret = adminSecret;
            return this;
        }

        public Builder withErlangCookieSecret(final Secret erlangCookieSecret) {
            this.erlangCookieSecret = erlangCookieSecret;
            return this;
        }

        public Builder withMainService(final Service mainService) {
            this.mainService = mainService;
            return this;
        }

        public Builder withDiscoveryService(final Service discoveryService) {
            this.discoveryService = discoveryService;
            return this;
        }

        public Builder withLoadBalancerService(final Optional<Service> loadBalancerService) {
            this.loadBalancerService = loadBalancerService;
            return this;
        }

        public Builder withStatefulSet(final StatefulSet statefulSet) {
            this.statefulSet = statefulSet;
            return this;
        }

        public Builder withPodDisruptionBudget(final PodDisruptionBudget podDisruptionBudget) {
            this.podDisruptionBudget = podDisruptionBudget;
            return this;
        }

        public Builder withShovels(final List<ShovelSpec> shovels) {
            this.shovels = shovels;
            return this;
        }

        public Builder withUsers(final List<RabbitMQUser> users) {
            this.users = users;
            return this;
        }

        public Builder withPolicies(final List<PolicySpec> policies) {
            this.policies = policies;
            return this;
        }

        public Builder withOperatorPolicies(final List<OperatorPolicySpec> operatorPolicies) {
            this.operatorPolicies = operatorPolicies;
            return this;
        }

        public RabbitMQCluster build() {
            return new RabbitMQCluster(
                    name,
                    namespace,
                    adminSecret,
                    erlangCookieSecret,
                    mainService,
                    discoveryService,
                    loadBalancerService,
                    statefulSet,
                    podDisruptionBudget,
                    shovels,
                    users,
                    policies,
                    operatorPolicies
            );
        }
    }
}
