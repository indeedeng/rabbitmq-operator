package com.indeed.operators.rabbitmq.reconciliation.rabbitmq;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.indeed.operators.rabbitmq.Constants;
import com.indeed.operators.rabbitmq.api.RabbitMQPasswordConverter;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiProvider;
import com.indeed.operators.rabbitmq.controller.SecretsController;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.VhostPermissions;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQCluster;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQConnectionInfo;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQUser;
import com.indeed.operators.rabbitmq.resources.RabbitMQSecrets;
import com.indeed.operators.rabbitmq.resources.RabbitMQServices;
import com.indeed.rabbitmq.admin.RabbitManagementApi;
import com.indeed.rabbitmq.admin.pojo.Permission;
import com.indeed.rabbitmq.admin.pojo.User;
import io.fabric8.kubernetes.api.model.Secret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClusterUsersReconciler {
    private static final Set<String> READ_ONLY_USERS = ImmutableSet.of("rabbit", "monitoring");
    private static final Logger log = LoggerFactory.getLogger(ClusterUsersReconciler.class);

    private final SecretsController secretsController;
    private final RabbitManagementApiProvider managementApiProvider;
    private final RabbitMQPasswordConverter passwordConverter;

    public ClusterUsersReconciler(
            final SecretsController secretsController,
            final RabbitManagementApiProvider managementApiProvider,
            final RabbitMQPasswordConverter passwordConverter
    ) {
        this.secretsController = secretsController;
        this.managementApiProvider = managementApiProvider;
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
        final RabbitManagementApi apiClient = managementApiProvider.getApi(connectionInfo);
        try {
            existingUsers = apiClient.listUsers().stream().collect(Collectors.toMap(User::getName, user -> user));
        } catch (final Exception e) {
            throw new RuntimeException(String.format("Failed to retrieve existing users from cluster %s in namespace %s, so skipping user reconciliation for this cluster", cluster.getName(), cluster.getNamespace()), e);
        }

        for (final Map.Entry<String, User> existingUser : existingUsers.entrySet()) {
            if (!expectedUsers.containsKey(existingUser.getKey()) && !READ_ONLY_USERS.contains(existingUser.getKey())) {
                try {
                    apiClient.deleteUser(existingUser.getKey());
                } catch (final Exception e) {
                    log.error(String.format("Failed to delete user %s in cluster %s in namespace %s", existingUser, cluster.getName(), cluster.getNamespace()), e);
                }
            }
        }

        for (final Map.Entry<String, RabbitMQUser> user : expectedUsers.entrySet()) {
            if (!existingUsers.containsKey(user.getKey()) && !READ_ONLY_USERS.contains(user.getKey())) {
                createUser(apiClient, user.getValue());
            } else if (!READ_ONLY_USERS.contains(user.getKey())) {
                updateExistingUser(apiClient, user.getValue());
            }
        }
    }

    private void createUser(final RabbitManagementApi apiClient, final RabbitMQUser desiredUser) {
        final Secret userSecret = desiredUser.getUserSecret();
        secretsController.createOrUpdate(userSecret);

        createOrUpdateUser(apiClient, desiredUser, passwordConverter.convertPasswordToHash(userSecret.getStringData().get(Constants.Secrets.PASSWORD_KEY)));
    }

    private void updateExistingUser(final RabbitManagementApi apiClient, final RabbitMQUser desiredUser) {
        final String username = desiredUser.getUsername();
        final Secret userSecret = secretsController.get(RabbitMQSecrets.getUserSecretName(username, desiredUser.getClusterMetadata().getName()), desiredUser.getClusterMetadata().getNamespace());

        createOrUpdateUser(apiClient, desiredUser, passwordConverter.convertPasswordToHash(secretsController.decodeSecretPayload(userSecret.getData().get(Constants.Secrets.PASSWORD_KEY))));
    }

    private void createOrUpdateUser(final RabbitManagementApi apiClient, final RabbitMQUser desiredUser, final String password) {
        try {
            final User user = new User().withName(desiredUser.getUsername()).withPassword(password).withTags(Joiner.on(",").join(desiredUser.getTags()));
            apiClient.createUser(user.getName(), user);
        } catch (final Exception e) {
            log.error(String.format("Failed to create/update user %s in cluster %s in namespace %s", desiredUser.getUsername(), desiredUser.getClusterMetadata().getName(), desiredUser.getClusterMetadata().getNamespace()), e);
        }

        // todo: only update vhosts if we need to
        updateVhosts(apiClient, desiredUser);
    }

    private void updateVhosts(final RabbitManagementApi apiClient, final RabbitMQUser user) {
        for (final VhostPermissions vhost : user.getVhostPermissions()) {
            try {
                final Permission permissions = new Permission()
                        .withRead(Pattern.compile(vhost.getPermissions().getRead()))
                        .withWrite(Pattern.compile(vhost.getPermissions().getWrite()))
                        .withConfigure(Pattern.compile(vhost.getPermissions().getConfigure()));

                apiClient.createPermission(vhost.getVhostName(), user.getUsername(), permissions);
            } catch (final Exception ex) {
                log.error(String.format("Failed to set vhost permissions for user %s in vhost %s in cluster %s in namespace %s", user.getUsername(), vhost.getVhostName(), user.getClusterMetadata().getName(), user.getClusterMetadata().getNamespace()), ex);
            }
        }
    }
}
