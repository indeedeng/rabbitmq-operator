package com.indeed.operators.rabbitmq.reconciliation.rabbitmq;

import com.indeed.operators.rabbitmq.api.RabbitManagementApiException;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiFacade;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiProvider;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQCluster;
import com.indeed.rabbitmq.admin.pojo.OperatorPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OperatorPolicyReconciler {

    private static final Logger log = LoggerFactory.getLogger(OperatorPolicyReconciler.class);

    private final RabbitManagementApiProvider apiProvider;

    public OperatorPolicyReconciler(
            final RabbitManagementApiProvider apiProvider
    ) {
        this.apiProvider = apiProvider;
    }

    public void reconcile(final RabbitMQCluster cluster) {
        final RabbitManagementApiFacade apiClient = apiProvider.getApi(cluster);

        final Map<String, OperatorPolicy> desiredPolicies = cluster.getOperatorPolicies().stream()
                .map(operatorPolicySpec -> new OperatorPolicy()
                        .withName(operatorPolicySpec.getName())
                        .withVhost(operatorPolicySpec.getVhost())
                        .withApplyTo(OperatorPolicy.ApplyTo.fromValue(operatorPolicySpec.getApplyTo()))
                        .withOperatorPolicyDefinition(operatorPolicySpec.getDefinition().asOperatorPolicyDefinition())
                        .withPattern(Pattern.compile(operatorPolicySpec.getPattern()))
                        .withPriority(operatorPolicySpec.getPriority()))
                .collect(Collectors.toMap(OperatorPolicy::getName, policy -> policy));
        final Map<String, OperatorPolicy> existingPolicies = apiClient.listOperatorPolicies().stream()
                .collect(Collectors.toMap(OperatorPolicy::getName, policy -> policy));

        deleteObsoletePolicies(desiredPolicies, existingPolicies, apiClient);
        createMissingPolicies(desiredPolicies, existingPolicies, apiClient);
        updateExistingPolicies(desiredPolicies, existingPolicies, apiClient);
    }

    private void createMissingPolicies(final Map<String, OperatorPolicy> desiredPolicies, final Map<String, OperatorPolicy> existingPolicies, final RabbitManagementApiFacade apiClient) {
        final List<OperatorPolicy> policiesToCreate = desiredPolicies.entrySet().stream()
                .filter(desiredPolicy -> !existingPolicies.containsKey(desiredPolicy.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        for (final OperatorPolicy policy : policiesToCreate) {
            try {
                apiClient.createOperatorPolicy(policy.getVhost(), policy.getName(), policy);
            } catch (final RabbitManagementApiException e) {
                log.error(String.format("Failed to create operator policy with name %s in vhost %s", policy.getName(), policy.getVhost()), e);
            }
        }
    }

    private void updateExistingPolicies(final Map<String, OperatorPolicy> desiredPolicies, final Map<String, OperatorPolicy> existingPolicies, final RabbitManagementApiFacade apiClient) {
        final List<OperatorPolicy> policiesToUpdate = desiredPolicies.entrySet().stream()
                .filter(desiredPolicy -> existingPolicies.containsKey(desiredPolicy.getKey()) && !policiesMatch(desiredPolicy.getValue(), existingPolicies.get(desiredPolicy.getKey())))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        for (final OperatorPolicy policy : policiesToUpdate) {
            try {
                apiClient.createOperatorPolicy(policy.getVhost(), policy.getName(), policy);
            } catch (final RabbitManagementApiException e) {
                log.error(String.format("Failed to update operator policy with name %s in vhost %s", policy.getName(), policy.getVhost()), e);
            }
        }
    }

    private void deleteObsoletePolicies(final Map<String, OperatorPolicy> desiredPolicies, final Map<String, OperatorPolicy> existingPolicies, final RabbitManagementApiFacade apiClient) {
        final List<OperatorPolicy> policiesToDelete = existingPolicies.entrySet().stream()
                .filter(existingPolicy -> !desiredPolicies.containsKey(existingPolicy.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        for (final OperatorPolicy policy : policiesToDelete) {
            try {
                apiClient.deleteOperatorPolicy(policy.getVhost(), policy.getName());
            } catch (final RabbitManagementApiException e) {
                log.error(String.format("Failed to delete operator policy with name %s in vhost %s", policy.getName(), policy.getVhost()), e);
            }
        }
    }

    private boolean policiesMatch(final OperatorPolicy desired, final OperatorPolicy existing) {
        return existing != null &&
                desired.getName().equals(existing.getName()) &&
                desired.getVhost().equals(existing.getVhost()) &&
                desired.getApplyTo().equals(existing.getApplyTo()) &&
                desired.getOperatorPolicyDefinition().equals(existing.getOperatorPolicyDefinition()) &&
                desired.getPattern().pattern().equals(existing.getPattern().pattern()) &&
                desired.getPriority().equals(existing.getPriority());
    }
}
