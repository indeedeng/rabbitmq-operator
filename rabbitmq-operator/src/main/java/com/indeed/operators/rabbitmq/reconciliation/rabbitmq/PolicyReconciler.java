package com.indeed.operators.rabbitmq.reconciliation.rabbitmq;

import com.indeed.operators.rabbitmq.api.RabbitManagementApiProvider;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.PolicySpec;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQCluster;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQConnectionInfo;
import com.indeed.operators.rabbitmq.resources.RabbitMQServices;
import com.indeed.rabbitmq.admin.RabbitManagementApi;
import com.indeed.rabbitmq.admin.pojo.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PolicyReconciler {

    private static final Logger log = LoggerFactory.getLogger(PolicyReconciler.class);

    private final RabbitManagementApiProvider apiProvider;

    public PolicyReconciler(
            final RabbitManagementApiProvider apiProvider
    ) {
        this.apiProvider = apiProvider;
    }

    public void reconcile(final RabbitMQCluster cluster) {
        final RabbitMQConnectionInfo connectionInfo = new RabbitMQConnectionInfo(cluster.getName(), cluster.getNamespace(), RabbitMQServices.getDiscoveryServiceName(cluster.getName()));
        final RabbitManagementApi apiClient = apiProvider.getApi(connectionInfo);

        deleteObsoletePolicies(cluster, apiClient);

        for (final PolicySpec desiredPolicy : cluster.getPolicies()) {
            final Policy policy = new Policy()
                    .withName(desiredPolicy.getName())
                    .withVhost(desiredPolicy.getVhost())
                    .withApplyTo(Policy.ApplyTo.fromValue(desiredPolicy.getApplyTo()))
                    .withDefinition(desiredPolicy.getDefinition().asDefinition())
                    .withPattern(Pattern.compile(desiredPolicy.getPattern()))
                    .withPriority(desiredPolicy.getPriority());

            try {
                apiClient.createPolicy(policy.getVhost(), policy.getName(), policy);
            } catch (final Exception e) {
                log.error(String.format("Failed to create policy with name %s for cluster %s in namespace %s", policy.getName(), cluster.getName(), cluster.getNamespace()), e);
            }
        }
    }

    private void deleteObsoletePolicies(final RabbitMQCluster cluster, final RabbitManagementApi apiClient) {
        final String clusterName = cluster.getName();
        final String clusterNamespace = cluster.getNamespace();

        final List<Policy> existingPolicies;

        try {
            existingPolicies = apiClient.listPolicies();
        } catch (final Exception e) {
            throw new RuntimeException(String.format("Unable to retrieve existing policies for cluster %s in namespace %s, skipping reconciliation of policies", clusterName, clusterNamespace), e);
        }

        final Map<String, Policy> existingPolicyMap = existingPolicies.stream()
                .collect(Collectors.toMap(Policy::getName, policy -> policy));
        final Map<String, PolicySpec> desiredPolicyMap = cluster.getPolicies().stream()
                .collect(Collectors.toMap(PolicySpec::getName, policy -> policy));

        for (final Map.Entry<String, Policy> existingPolicy : existingPolicyMap.entrySet()) {
            final String policyName = existingPolicy.getKey();
            if (!desiredPolicyMap.containsKey(policyName)) {
                try {
                    apiClient.deletePolicy(existingPolicy.getValue().getVhost(), policyName);
                } catch (final Exception e) {
                    log.error(String.format("Failed to delete policy with name %s with %s vhost in cluster %s in namespace %s", policyName, existingPolicy.getValue().getVhost(), clusterName, clusterNamespace), e);
                }
            }
        }
    }
}
