package com.indeed.operators.rabbitmq.reconciliation;

import com.indeed.operators.rabbitmq.api.RabbitMQApiClient;
import com.indeed.operators.rabbitmq.api.RabbitMQPasswordConverter;
import com.indeed.operators.rabbitmq.controller.SecretsController;
import com.indeed.operators.rabbitmq.controller.crd.RabbitMQUserResourceController;
import com.indeed.operators.rabbitmq.model.crd.user.RabbitMQUserCustomResource;
import com.indeed.operators.rabbitmq.model.crd.user.RabbitMQVHostUser;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQConnectionInfo;
import com.indeed.operators.rabbitmq.model.rabbitmq.User;
import com.indeed.operators.rabbitmq.resources.RabbitMQSecrets;
import com.indeed.operators.rabbitmq.resources.RabbitMQServices;
import io.fabric8.kubernetes.api.model.Secret;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class RabbitMQUserReconciler implements Reconciler {

    private final RabbitMQSecrets rabbitMQSecrets;
    private final RabbitMQUserResourceController controller;
    private final SecretsController secretsController;
    private final RabbitMQApiClient apiClient;
    private final RabbitMQPasswordConverter passwordConverter;

    public RabbitMQUserReconciler(
            final RabbitMQSecrets rabbitMQSecrets,
            final RabbitMQUserResourceController controller,
            final SecretsController secretsController,
            final RabbitMQApiClient apiClient,
            final RabbitMQPasswordConverter passwordConverter
    ) {
        this.rabbitMQSecrets = rabbitMQSecrets;
        this.controller = controller;
        this.secretsController = secretsController;
        this.apiClient = apiClient;
        this.passwordConverter = passwordConverter;
    }

    @Override
    public void reconcile(final Reconciliation rec) throws IOException {
        final RabbitMQUserCustomResource resource = controller.get(rec.getResourceName(), rec.getNamespace());

        final RabbitMQConnectionInfo connectionInfo = new RabbitMQConnectionInfo(
                rec.getClusterName(),
                rec.getNamespace(),
                RabbitMQServices.getDiscoveryServiceName(rec.getClusterName())
        );

        final List<User> allUsers = apiClient.getUsers(connectionInfo);

        final Optional<User> maybeExistingUser = allUsers.stream().filter(user -> user.getUsername().equals(resource.getName())).findFirst();

        if (!maybeExistingUser.isPresent()) {
            createUser(resource, connectionInfo);
        } else {
            updateExistingUser(resource, maybeExistingUser.get(), connectionInfo);
        }
    }

    private void createUser(final RabbitMQUserCustomResource resource, final RabbitMQConnectionInfo connectionInfo) throws IOException {
        final Secret userSecret = rabbitMQSecrets.createUserSecret(resource);
        secretsController.createOrUpdate(userSecret);

        // We use getStringData() here because we just constructed the secret. If the secret had been retrieved from the k8s api, we would use getData() instead
        apiClient.createOrUpdateUser(connectionInfo, resource.getName(), passwordConverter.convertPasswordToHash(userSecret.getStringData().get("password")), resource.getSpec().getTags());

        for (final RabbitMQVHostUser vhost : resource.getSpec().getVHosts()) {
            apiClient.updateVHostPermissions(connectionInfo, vhost.getName(), resource.getName(), vhost.getPermissions());
        }
    }

    // todo: make this less chatty
    private void updateExistingUser(final RabbitMQUserCustomResource resource, final User existingUser, final RabbitMQConnectionInfo connectionInfo) throws IOException {
        final Secret userSecret = secretsController.get(RabbitMQSecrets.getUserSecretName(existingUser.getUsername(), resource.getSpec().getClusterName()), resource.getMetadata().getNamespace());

        apiClient.createOrUpdateUser(connectionInfo, resource.getName(), passwordConverter.convertPasswordToHash(secretsController.decodeSecretPayload(userSecret.getData().get("password"))), resource.getSpec().getTags());

        for (final RabbitMQVHostUser vhost : resource.getSpec().getVHosts()) {
            apiClient.updateVHostPermissions(connectionInfo, vhost.getName(), resource.getName(), vhost.getPermissions());
        }
    }
}
