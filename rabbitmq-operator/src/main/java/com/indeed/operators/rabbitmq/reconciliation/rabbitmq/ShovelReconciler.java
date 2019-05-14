package com.indeed.operators.rabbitmq.reconciliation.rabbitmq;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.indeed.operators.rabbitmq.Constants;
import com.indeed.operators.rabbitmq.api.RabbitApiResponseConsumer;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiProvider;
import com.indeed.operators.rabbitmq.controller.SecretsController;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.ShovelSpec;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQCluster;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQConnectionInfo;
import com.indeed.operators.rabbitmq.resources.RabbitMQServices;
import com.indeed.rabbitmq.admin.RabbitManagementApi;
import com.indeed.rabbitmq.admin.pojo.Shovel;
import com.indeed.rabbitmq.admin.pojo.ShovelArguments;
import io.fabric8.kubernetes.api.model.Secret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShovelReconciler {
    private static final Logger log = LoggerFactory.getLogger(ShovelReconciler.class);

    private final RabbitManagementApiProvider apiProvider;
    private final SecretsController secretsController;

    public ShovelReconciler(
            final RabbitManagementApiProvider apiProvider,
            final SecretsController secretsController
    ) {
        this.apiProvider = apiProvider;
        this.secretsController = secretsController;
    }

    public void reconcile(final RabbitMQCluster cluster) {
        final RabbitMQConnectionInfo connectionInfo = new RabbitMQConnectionInfo(cluster.getName(), cluster.getNamespace(), RabbitMQServices.getDiscoveryServiceName(cluster.getName()));
        final RabbitManagementApi apiClient = apiProvider.getApi(connectionInfo);

        deleteObsoleteShovels(cluster, apiClient);

        for (final ShovelSpec desiredShovel : cluster.getShovels()) {
            final String destSecretName = desiredShovel.getDestination().getSecretName();
            final String destSecretNamespace = desiredShovel.getDestination().getSecretNamespace();
            final Secret secret = secretsController.get(destSecretName, destSecretNamespace);

            Preconditions.checkNotNull(secret, String.format("Could not find secret with name [%s] in namespace [%s]", destSecretName, destSecretNamespace));

            final String username = secretsController.decodeSecretPayload(secret.getData().get(Constants.Secrets.USERNAME_KEY));
            final String password = secretsController.decodeSecretPayload(secret.getData().get(Constants.Secrets.PASSWORD_KEY));

            final List<String> uris = desiredShovel.getDestination().getAddresses().stream()
                    .map(addr -> String.format("amqp://%s:%s@%s", username, password, addr.asRabbitUri()))
                    .collect(Collectors.toList());

            final ShovelArguments shovelArguments = new ShovelArguments()
                    .withSrcUri(Lists.newArrayList("amqp://"))
                    .withSrcQueue(desiredShovel.getSource().getQueue())
                    .withDestUri(uris);
            final Shovel shovel = new Shovel().withValue(shovelArguments).withVhost(desiredShovel.getSource().getVhost()).withName(desiredShovel.getName());

            try {
                RabbitApiResponseConsumer.consumeResponse(apiClient.createShovel(shovel.getVhost(), shovel.getName(), shovel).execute());
            } catch (final Exception e) {
                log.error(String.format("Failed to create shovel with name %s in vhost %s", shovel.getName(), shovel.getVhost()), e);
            }
        }
    }

    private void deleteObsoleteShovels(final RabbitMQCluster cluster, final RabbitManagementApi apiClient) {
        final List<Shovel> existingShovels;

        try {
            existingShovels = RabbitApiResponseConsumer.consumeResponse(apiClient.listShovels().execute());
        } catch (final Exception e) {
            throw new RuntimeException("Unable to retrieve existing shovels, skipping reconciliation of shovels", e);
        }

        final Map<String, Shovel> existingShovelMap = existingShovels.stream()
                .collect(Collectors.toMap(Shovel::getName, shovel -> shovel));
        final Map<String, ShovelSpec> desiredShovelMap = cluster.getShovels().stream()
                .collect(Collectors.toMap(ShovelSpec::getName, shovel -> shovel));

        for (final Map.Entry<String, Shovel> existingShovel : existingShovelMap.entrySet()) {
            final String shovelName = existingShovel.getKey();
            if (!desiredShovelMap.containsKey(shovelName)) {
                try {
                    RabbitApiResponseConsumer.consumeResponse(apiClient.deleteShovel(existingShovel.getValue().getVhost(), shovelName).execute());
                } catch (final Exception e) {
                    log.error(String.format("Failed to delete shovel with name %s in vhost %s", shovelName, existingShovel.getValue().getVhost()), e);
                }
            }
        }
    }
}
