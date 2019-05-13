package com.indeed.operators.rabbitmq.reconciliation.rabbitmq;

import com.indeed.operators.rabbitmq.api.RabbitApiResponseConsumer;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiProvider;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.OperatorPolicySpec;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQCluster;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQConnectionInfo;
import com.indeed.operators.rabbitmq.resources.RabbitMQServices;
import com.indeed.rabbitmq.admin.RabbitManagementApi;
import com.indeed.rabbitmq.admin.pojo.OperatorPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OperatorPolicyReconciler {

    private static final Logger log = LoggerFactory.getLogger(PolicyReconciler.class);

    private final RabbitManagementApiProvider apiProvider;

    public OperatorPolicyReconciler(
            final RabbitManagementApiProvider apiProvider
    ) {
        this.apiProvider = apiProvider;
    }

    public void reconcile(final RabbitMQCluster cluster) {
        final RabbitMQConnectionInfo connectionInfo = new RabbitMQConnectionInfo(cluster.getName(), cluster.getNamespace(), RabbitMQServices.getDiscoveryServiceName(cluster.getName()));
        final RabbitManagementApi apiClient = apiProvider.getApi(connectionInfo);

        deleteObsoleteOperatorPolicies(cluster, apiClient);

        for (final OperatorPolicySpec desiredPolicy : cluster.getOperatorPolicies()) {
            final OperatorPolicy policy = new OperatorPolicy()
                    .withName(desiredPolicy.getName())
                    .withVhost(desiredPolicy.getVhost())
                    .withApplyTo(OperatorPolicy.ApplyTo.fromValue(desiredPolicy.getApplyTo()))
                    .withOperatorPolicyDefinition(desiredPolicy.getDefinition().asOperatorPolicyDefinition())
                    .withPattern(Pattern.compile(desiredPolicy.getPattern()))
                    .withPriority(desiredPolicy.getPriority());

            try {
                RabbitApiResponseConsumer.consumeResponse(apiClient.createOperatorPolicy(policy.getVhost(), policy.getName(), policy).execute());
            } catch (final Exception e) {
                log.error(String.format("Failed to create operator policy with name %s in vhost %s", policy.getName(), policy.getVhost()), e);
            }
        }
    }

    private void deleteObsoleteOperatorPolicies(final RabbitMQCluster cluster, final RabbitManagementApi apiClient) {
        final List<OperatorPolicy> existingPolicies;

        try {
            existingPolicies = RabbitApiResponseConsumer.consumeResponse(apiClient.listOperatorPolicies().execute());
        } catch (final Exception e) {
            throw new RuntimeException("Unable to retrieve existing operator policies, skipping reconciliation of operator policies", e);
        }

        final Map<String, OperatorPolicy> existingPolicyMap = existingPolicies.stream()
                .collect(Collectors.toMap(OperatorPolicy::getName, policy -> policy));
        final Map<String, OperatorPolicySpec> desiredPolicyMap = cluster.getOperatorPolicies().stream()
                .collect(Collectors.toMap(OperatorPolicySpec::getName, policy -> policy));

        for (final Map.Entry<String, OperatorPolicy> existingPolicy : existingPolicyMap.entrySet()) {
            final String policyName = existingPolicy.getKey();
            if (!desiredPolicyMap.containsKey(policyName)) {
                try {
                    RabbitApiResponseConsumer.consumeResponse(apiClient.deleteOperatorPolicy(existingPolicy.getValue().getVhost(), policyName).execute());
                } catch (final Exception e) {
                    log.error(String.format("Failed to delete operator policy with name %s in vhost %s", policyName, existingPolicy.getValue().getVhost()), e);
                }
            }
        }
    }
}
