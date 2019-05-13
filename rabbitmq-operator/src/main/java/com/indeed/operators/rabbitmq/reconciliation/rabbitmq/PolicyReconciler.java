package com.indeed.operators.rabbitmq.reconciliation.rabbitmq;

import com.indeed.operators.rabbitmq.api.RabbitApiResponseConsumer;
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
                RabbitApiResponseConsumer.consumeResponse(apiClient.createPolicy(policy.getVhost(), policy.getName(), policy).execute());
            } catch (final Exception e) {
                log.error(String.format("Failed to create policy with name %s in vhost %s", policy.getName(), policy.getVhost()), e);
            }
        }
    }

    private void deleteObsoletePolicies(final RabbitMQCluster cluster, final RabbitManagementApi apiClient) {
        final List<Policy> existingPolicies;

        try {
            existingPolicies = RabbitApiResponseConsumer.consumeResponse(apiClient.listPolicies().execute());
        } catch (final Exception e) {
            throw new RuntimeException("Unable to retrieve existing policies, skipping reconciliation of policies", e);
        }

        final Map<String, Policy> existingPolicyMap = existingPolicies.stream()
                .collect(Collectors.toMap(Policy::getName, policy -> policy));
        final Map<String, PolicySpec> desiredPolicyMap = cluster.getPolicies().stream()
                .collect(Collectors.toMap(PolicySpec::getName, policy -> policy));

        for (final Map.Entry<String, Policy> existingPolicy : existingPolicyMap.entrySet()) {
            final String policyName = existingPolicy.getKey();
            if (!desiredPolicyMap.containsKey(policyName)) {
                try {
                    RabbitApiResponseConsumer.consumeResponse(apiClient.deletePolicy(existingPolicy.getValue().getVhost(), policyName).execute());
                } catch (final Exception e) {
                    log.error(String.format("Failed to delete policy with name %s in vhost %s", policyName, existingPolicy.getValue().getVhost()), e);
                }
            }
        }
    }
}
