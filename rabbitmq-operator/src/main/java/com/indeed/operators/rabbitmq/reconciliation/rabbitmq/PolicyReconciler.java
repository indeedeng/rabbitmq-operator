package com.indeed.operators.rabbitmq.reconciliation.rabbitmq;

import com.indeed.operators.rabbitmq.api.RabbitManagementApiException;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiFacade;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiProvider;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQCluster;
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
        final RabbitManagementApiFacade apiClient = apiProvider.getApi(cluster);

        final Map<String, Policy> desiredPolicies = cluster.getPolicies().stream()
                .map(policySpec -> new Policy()
                        .withName(policySpec.getName())
                        .withVhost(policySpec.getVhost())
                        .withApplyTo(Policy.ApplyTo.fromValue(policySpec.getApplyTo()))
                        .withDefinition(policySpec.getDefinition().asDefinition())
                        .withPattern(Pattern.compile(policySpec.getPattern()))
                        .withPriority(policySpec.getPriority()))
                .collect(Collectors.toMap(Policy::getName, policy -> policy));
        final Map<String, Policy> existingPolicies = apiClient.listPolicies().stream()
                .collect(Collectors.toMap(Policy::getName, policy -> policy));

        deleteObsoletePolicies(desiredPolicies, existingPolicies, apiClient);
        createMissingPolicies(desiredPolicies, existingPolicies, apiClient);
        updateExistingPolicies(desiredPolicies, existingPolicies, apiClient);
    }

    private void createMissingPolicies(final Map<String, Policy> desiredPolicies, final Map<String, Policy> existingPolicies, final RabbitManagementApiFacade apiClient) {
        final List<Policy> policiesToCreate = desiredPolicies.entrySet().stream()
                .filter(desiredPolicy -> !existingPolicies.containsKey(desiredPolicy.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        for (final Policy policy : policiesToCreate) {
            try {
                apiClient.createPolicy(policy.getVhost(), policy.getName(), policy);
            } catch (final RabbitManagementApiException e) {
                log.error(String.format("Failed to create policy with name %s in vhost %s", policy.getName(), policy.getVhost()), e);
            }
        }
    }

    private void updateExistingPolicies(final Map<String, Policy> desiredPolicies, final Map<String, Policy> existingPolicies, final RabbitManagementApiFacade apiClient) {
        final List<Policy> policiesToUpdate = desiredPolicies.entrySet().stream()
                .filter(desiredPolicy -> existingPolicies.containsKey(desiredPolicy.getKey()) && !policiesMatch(desiredPolicy.getValue(), existingPolicies.get(desiredPolicy.getKey())))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        for (final Policy policy : policiesToUpdate) {
            try {
                apiClient.createPolicy(policy.getVhost(), policy.getName(), policy);
            } catch (final RabbitManagementApiException e) {
                log.error(String.format("Failed to update policy with name %s in vhost %s", policy.getName(), policy.getVhost()), e);
            }
        }
    }

    private void deleteObsoletePolicies(final Map<String, Policy> desiredPolicies, final Map<String, Policy> existingPolicies, final RabbitManagementApiFacade apiClient) {
        final List<Policy> policiesToDelete = existingPolicies.entrySet().stream()
                .filter(existingPolicy -> !desiredPolicies.containsKey(existingPolicy.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        for (final Policy policy : policiesToDelete) {
            try {
                apiClient.deletePolicy(policy.getVhost(), policy.getName());
            } catch (final RabbitManagementApiException e) {
                log.error(String.format("Failed to delete policy with name %s in vhost %s", policy.getName(), policy.getVhost()), e);
            }
        }
    }

    private boolean policiesMatch(final Policy desired, final Policy existing) {
        return existing != null &&
                desired.getName().equals(existing.getName()) &&
                desired.getVhost().equals(existing.getVhost()) &&
                desired.getApplyTo().equals(existing.getApplyTo()) &&
                desired.getDefinition().equals(existing.getDefinition()) &&
                desired.getPattern().pattern().equals(existing.getPattern().pattern()) &&
                desired.getPriority().equals(existing.getPriority());
    }
}
