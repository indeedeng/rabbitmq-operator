package com.indeed.operators.rabbitmq.reconciliation.rabbitmq;

import com.google.common.base.Preconditions;
import com.indeed.operators.rabbitmq.Constants;
import com.indeed.operators.rabbitmq.api.RabbitMQApiClient;
import com.indeed.operators.rabbitmq.controller.SecretsController;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.ShovelSpec;
import com.indeed.operators.rabbitmq.model.rabbitmq.api.BaseParameter;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQConnectionInfo;
import com.indeed.operators.rabbitmq.model.rabbitmq.api.ShovelParameterValue;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQCluster;
import com.indeed.operators.rabbitmq.resources.RabbitMQServices;
import io.fabric8.kubernetes.api.model.Secret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShovelReconciler {
    private static final Logger log = LoggerFactory.getLogger(ShovelReconciler.class);

    private final RabbitMQApiClient apiClient;
    private final SecretsController secretsController;

    public ShovelReconciler(
            final RabbitMQApiClient apiClient,
            final SecretsController secretsController
    ) {
        this.apiClient = apiClient;
        this.secretsController = secretsController;
    }

    public void reconcile(final RabbitMQCluster cluster) {
        deleteObsoleteShovels(cluster);

        final RabbitMQConnectionInfo connectionInfo = new RabbitMQConnectionInfo(cluster.getName(), cluster.getNamespace(), RabbitMQServices.getDiscoveryServiceName(cluster.getName()));

        for (final ShovelSpec shovel : cluster.getShovels()) {
            final String destSecretName = shovel.getDestination().getSecretName();
            final String destSecretNamespace = shovel.getDestination().getSecretNamespace();
            final Secret secret = secretsController.get(destSecretName, destSecretNamespace);

            Preconditions.checkNotNull(secret, String.format("Could not find secret with name [%s] in namespace [%s]", destSecretName, destSecretNamespace));

            final String username = secretsController.decodeSecretPayload(secret.getData().get(Constants.Secrets.USERNAME_KEY));
            final String password = secretsController.decodeSecretPayload(secret.getData().get(Constants.Secrets.PASSWORD_KEY));

            final List<String> uris = shovel.getDestination().getAddresses().stream()
                    .map(addr -> String.format("amqp://%s:%s@%s", username, password, addr.asRabbitUri()))
                    .collect(Collectors.toList());

            final BaseParameter<ShovelParameterValue> shovelParameter = new BaseParameter<>(
                    ShovelParameterValue.newBuilder()
                            .setSourceQueue(shovel.getSource().getQueue())
                            .setDestinationUri(uris)
                            .build(),
                    shovel.getSource().getVhost(),
                    shovel.getName()
            );

            try {
                apiClient.createOrUpdateShovel(connectionInfo, shovelParameter);
            } catch (final IOException e) {
                log.error(String.format("Failed to create shovel with name %s for cluster %s in namespace %s", shovel.getName(), cluster.getName(), cluster.getNamespace()), e);
            }
        }
    }

    private void deleteObsoleteShovels(final RabbitMQCluster cluster) {
        final String clusterName = cluster.getName();
        final String clusterNamespace = cluster.getNamespace();
        final RabbitMQConnectionInfo connectionInfo = new RabbitMQConnectionInfo(clusterName, clusterNamespace, RabbitMQServices.getDiscoveryServiceName(clusterName));

        final List<BaseParameter<ShovelParameterValue>> existingShovels;

        try {
            existingShovels = apiClient.getShovels(connectionInfo, "/");
        } catch (final IOException e) {
            throw new RuntimeException(String.format("Unable to retrieve existing shovels for cluster %s in namespace %s, skipping reconciliation of shovels", clusterName, clusterNamespace), e);
        }

        final Map<String, BaseParameter<ShovelParameterValue>> existingShovelMap = existingShovels.stream()
                .collect(Collectors.toMap(BaseParameter::getName, shovel -> shovel));
        final Map<String, ShovelSpec> desiredShovelMap = cluster.getShovels().stream()
                .collect(Collectors.toMap(ShovelSpec::getName, shovel -> shovel));

        for (final Map.Entry<String, BaseParameter<ShovelParameterValue>> existingShovel : existingShovelMap.entrySet()) {
            final String shovelName = existingShovel.getKey();
            if (!desiredShovelMap.containsKey(shovelName)) {
                try {
                    apiClient.deleteShovel(connectionInfo, existingShovel.getValue().getVhost(), shovelName);
                } catch (final IOException e) {
                    log.error(String.format("Failed to delete shovel with name %s with %s vhost in cluster %s in namespace %s", shovelName, existingShovel.getValue().getVhost(), clusterName, clusterNamespace), e);
                }
            }
        }
    }
}
