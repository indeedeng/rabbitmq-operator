package com.indeed.operators.rabbitmq.reconciliation.rabbitmq;

import com.google.common.collect.ImmutableSet;
import com.indeed.operators.rabbitmq.Constants;
import com.indeed.operators.rabbitmq.api.RabbitMQApiClient;
import com.indeed.operators.rabbitmq.api.RabbitMQPasswordConverter;
import com.indeed.operators.rabbitmq.controller.SecretsController;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.VhostPermissions;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQCluster;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQConnectionInfo;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQUser;
import com.indeed.operators.rabbitmq.model.rabbitmq.api.User;
import com.indeed.operators.rabbitmq.resources.RabbitMQSecrets;
import com.indeed.operators.rabbitmq.resources.RabbitMQServices;
import io.fabric8.kubernetes.api.model.Secret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ClusterUsersReconciler {
    private static final Set<String> READ_ONLY_USERS = ImmutableSet.of("rabbit", "monitoring");
    private static final Logger log = LoggerFactory.getLogger(ClusterUsersReconciler.class);

    private final SecretsController secretsController;
    private final RabbitMQApiClient apiClient;
    private final RabbitMQPasswordConverter passwordConverter;

    public ClusterUsersReconciler(
            final SecretsController secretsController,
            final RabbitMQApiClient apiClient,
            final RabbitMQPasswordConverter passwordConverter
    ) {
        this.secretsController = secretsController;
        this.apiClient = apiClient;
        this.passwordConverter = passwordConverter;
    }

    public void reconcile(final RabbitMQCluster cluster) {
        final RabbitMQConnectionInfo connectionInfo = new RabbitMQConnectionInfo(
                cluster.getName(),
                cluster.getNamespace(),
                RabbitMQServices.getDiscoveryServiceName(cluster.getName())
        );

        final Map<String, RabbitMQUser> expectedUsers = cluster.getUsers().stream().collect(Collectors.toMap(RabbitMQUser::getUsername, user -> user));
        final Map<String, User> existingUsers;
        try {
            existingUsers = apiClient.getUsers(connectionInfo).stream().collect(Collectors.toMap(User::getUsername, user -> user));
        } catch (final IOException e) {
            throw new RuntimeException(String.format("Failed to retrieve existing users from cluster %s in namespace %s, so skipping user reconciliation for this cluster", cluster.getName(), cluster.getNamespace()), e);
        }

        for (final Map.Entry<String, User> existingUser : existingUsers.entrySet()) {
            if (!expectedUsers.containsKey(existingUser.getKey()) && !READ_ONLY_USERS.contains(existingUser.getKey())) {
                try {
                    apiClient.deleteUser(connectionInfo, existingUser.getKey());
                } catch (final IOException e) {
                    log.error(String.format("Failed to delete user %s in cluster %s in namespace %s", existingUser, cluster.getName(), cluster.getNamespace()), e);
                }
            }
        }

        for (final Map.Entry<String, RabbitMQUser> user : expectedUsers.entrySet()) {
            if (!existingUsers.containsKey(user.getKey()) && !READ_ONLY_USERS.contains(user.getKey())) {
                createUser(user.getValue(), connectionInfo);
            } else if (!READ_ONLY_USERS.contains(user.getKey())) {
                updateExistingUser(user.getValue(), connectionInfo);
            }
        }
    }

    private void createUser(final RabbitMQUser desiredUser, final RabbitMQConnectionInfo connectionInfo) {
        final Secret userSecret = desiredUser.getUserSecret();
        secretsController.createOrUpdate(userSecret);

        createOrUpdateUser(connectionInfo, desiredUser, passwordConverter.convertPasswordToHash(userSecret.getStringData().get(Constants.Secrets.PASSWORD_KEY)));
    }

    private void updateExistingUser(final RabbitMQUser desiredUser, final RabbitMQConnectionInfo connectionInfo) {
        final String username = desiredUser.getUsername();
        final Secret userSecret = secretsController.get(RabbitMQSecrets.getUserSecretName(username, desiredUser.getClusterMetadata().getName()), desiredUser.getClusterMetadata().getNamespace());

        createOrUpdateUser(connectionInfo, desiredUser, passwordConverter.convertPasswordToHash(userSecret.getStringData().get(Constants.Secrets.PASSWORD_KEY)));
    }

    private void createOrUpdateUser(final RabbitMQConnectionInfo connectionInfo, final RabbitMQUser user, final String password) {
        try {
            // We use getStringData() here because we just constructed the secret. If the secret had been retrieved from the k8s api, we would use getData() instead
            apiClient.createOrUpdateUser(connectionInfo, user.getUsername(), password, user.getTags());
        } catch (final IOException e) {
            log.error(String.format("Failed to create/update user %s in cluster %s in namespace %s", user.getUsername(), user.getClusterMetadata().getName(), user.getClusterMetadata().getNamespace()), e);
        }

        // todo: only update vhosts if we need to
        updateVhosts(connectionInfo, user);
    }

    private void updateVhosts(final RabbitMQConnectionInfo connectionInfo, final RabbitMQUser user) {
        for (final VhostPermissions vhost : user.getVhostPermissions()) {
            try {
                apiClient.updateVHostPermissions(connectionInfo, vhost.getVhostName(), user.getUsername(), vhost.getPermissions());
            } catch (final IOException ex) {
                log.error(String.format("Failed to set vhost permissions for user %s in vhost %s in cluster %s in namespace %s", user.getUsername(), vhost.getVhostName(), user.getClusterMetadata().getName(), user.getClusterMetadata().getNamespace()), ex);
            }
        }
    }
}
