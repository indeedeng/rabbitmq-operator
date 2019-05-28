package com.indeed.operators.rabbitmq.reconciliation.rabbitmq;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.indeed.operators.rabbitmq.Constants;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiException;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiFacade;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiProvider;
import com.indeed.operators.rabbitmq.controller.SecretsController;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.AddressAndVhost;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQCluster;
import com.indeed.rabbitmq.admin.pojo.Shovel;
import com.indeed.rabbitmq.admin.pojo.ShovelArguments;
import io.fabric8.kubernetes.api.model.Secret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.indeed.operators.rabbitmq.Constants.Uris.AMQP_BASE;

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
        final RabbitManagementApiFacade apiClient = apiProvider.getApi(cluster);

        final Map<String, Shovel> desiredShovels = cluster.getShovels().stream()
                .map(shovelSpec -> {
                    final String destSecretName = shovelSpec.getDestination().getSecretName();
                    final String destSecretNamespace = shovelSpec.getDestination().getSecretNamespace();
                    final Secret secret = secretsController.get(destSecretName, destSecretNamespace);

                    Preconditions.checkNotNull(secret, String.format("Could not find secret with name [%s] in namespace [%s]", destSecretName, destSecretNamespace));

                    final List<String> uris = shovelSpec.getDestination().getAddresses().stream()
                            .map(addr -> buildShovelUri(secret, addr))
                            .collect(Collectors.toList());

                    final ShovelArguments shovelArguments = new ShovelArguments()
                            .withSrcUri(Lists.newArrayList(AMQP_BASE))
                            .withSrcQueue(shovelSpec.getSource().getQueue())
                            .withDestUri(uris);
                    return new Shovel().withValue(shovelArguments).withVhost(shovelSpec.getSource().getVhost()).withName(shovelSpec.getName());
                })
                .collect(Collectors.toMap(Shovel::getName, shovel -> shovel));
        final Map<String, Shovel> existingShovels = apiClient.listShovels().stream()
                .collect(Collectors.toMap(Shovel::getName, shovel -> shovel));

        deleteObsoleteShovels(desiredShovels, existingShovels, apiClient);
        createMissingShovels(desiredShovels, existingShovels, apiClient);
        updateExistingShovels(desiredShovels, existingShovels, apiClient);
    }

    private void createMissingShovels(final Map<String, Shovel> desiredShovels, final Map<String, Shovel> existingShovels, final RabbitManagementApiFacade apiClient) {
        final List<Shovel> shovelsToCreate = desiredShovels.entrySet().stream()
                .filter(desiredShovel -> !existingShovels.containsKey(desiredShovel.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        for (final Shovel shovel : shovelsToCreate) {
            try {
                apiClient.createShovel(shovel.getVhost(), shovel.getName(), shovel);
            } catch (final RabbitManagementApiException e) {
                log.error(String.format("Failed to create shovel with name %s in vhost %s", shovel.getName(), shovel.getVhost()), e);
            }
        }
    }

    private void updateExistingShovels(final Map<String, Shovel> desiredShovels, final Map<String, Shovel> existingShovels, final RabbitManagementApiFacade apiClient) {
        final List<Shovel> shovelsToUpdate = desiredShovels.entrySet().stream()
                .filter(desiredShovel -> existingShovels.containsKey(desiredShovel.getKey()) && !shovelsMatch(desiredShovel.getValue(), existingShovels.get(desiredShovel.getKey())))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        for (final Shovel shovel : shovelsToUpdate) {
            try {
                apiClient.createShovel(shovel.getVhost(), shovel.getName(), shovel);
            } catch (final RabbitManagementApiException e) {
                log.error(String.format("Failed to update shovel with name %s in vhost %s", shovel.getName(), shovel.getVhost()), e);
            }
        }
    }

    private void deleteObsoleteShovels(final Map<String, Shovel> desiredShovels, final Map<String, Shovel> existingShovels, final RabbitManagementApiFacade apiClient) {
        final List<Shovel> shovelsToDelete = existingShovels.entrySet().stream()
                .filter(existingShovel -> !desiredShovels.containsKey(existingShovel.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        for (final Shovel existingShovel : shovelsToDelete) {
            try {
                apiClient.deleteShovel(existingShovel.getVhost(), existingShovel.getName());
            } catch (final Exception e) {
                log.error(String.format("Failed to delete shovel with name %s in vhost %s", existingShovel.getName(), existingShovel.getVhost()), e);
            }
        }
    }

    private String buildShovelUri(final Secret shovelSecret, final AddressAndVhost rabbitAddress) {
        final String username = secretsController.decodeSecretPayload(shovelSecret.getData().get(Constants.Secrets.USERNAME_KEY));
        final String password = secretsController.decodeSecretPayload(shovelSecret.getData().get(Constants.Secrets.PASSWORD_KEY));

        return String.format("%s%s:%s@%s", AMQP_BASE, username, password, rabbitAddress.asRabbitUri());
    }

    private boolean shovelsMatch(final Shovel desired, final Shovel existing) {
        return existing != null &&
                desired.getValue().equals(existing.getValue()) &&
                desired.getVhost().equals(existing.getVhost()) &&
                desired.getName().equals(existing.getName());
    }
}
