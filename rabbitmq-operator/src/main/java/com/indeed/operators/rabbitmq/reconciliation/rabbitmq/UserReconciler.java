package com.indeed.operators.rabbitmq.reconciliation.rabbitmq;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.indeed.operators.rabbitmq.Constants;
import com.indeed.operators.rabbitmq.api.RabbitMQPasswordConverter;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiException;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiFacade;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiProvider;
import com.indeed.operators.rabbitmq.controller.SecretsController;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.VhostPermissions;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQCluster;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQUser;
import com.indeed.operators.rabbitmq.resources.RabbitMQSecrets;
import com.indeed.rabbitmq.admin.pojo.Permission;
import com.indeed.rabbitmq.admin.pojo.User;
import io.fabric8.kubernetes.api.model.Secret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UserReconciler {
    private static final Set<String> PERMANENT_USERS = ImmutableSet.of("rabbit", "monitoring");
    private static final Logger log = LoggerFactory.getLogger(UserReconciler.class);

    private final SecretsController secretsController;
    private final RabbitManagementApiProvider managementApiProvider;
    private final RabbitMQPasswordConverter passwordConverter;

    public UserReconciler(
            final SecretsController secretsController,
            final RabbitManagementApiProvider managementApiProvider,
            final RabbitMQPasswordConverter passwordConverter
    ) {
        this.secretsController = secretsController;
        this.managementApiProvider = managementApiProvider;
        this.passwordConverter = passwordConverter;
    }

    public void reconcile(final RabbitMQCluster cluster) {
        final Map<String, RabbitMQUser> expectedUsers = cluster.getUsers().stream().collect(Collectors.toMap(RabbitMQUser::getUsername, user -> user));
        final RabbitManagementApiFacade apiClient = managementApiProvider.getApi(cluster);

        final Map<String, User> existingUsers = apiClient.listUsers()
                .stream()
                .filter(user -> !PERMANENT_USERS.contains(user.getName()))
                .collect(Collectors.toMap(User::getName, user -> user));


        for (final Map.Entry<String, User> existingUser : existingUsers.entrySet()) {
            if (!expectedUsers.containsKey(existingUser.getKey())) {
                try {
                    apiClient.deleteUser(existingUser.getKey());
                } catch (final RabbitManagementApiException e) {
                    log.error(String.format("Failed to delete user %s", existingUser), e);
                }
            }
        }

        for (final Map.Entry<String, RabbitMQUser> user : expectedUsers.entrySet()) {
            if (!existingUsers.containsKey(user.getKey())) {
                createUser(apiClient, user.getValue());
            } else if (!PERMANENT_USERS.contains(user.getKey())) {
                updateExistingUser(apiClient, user.getValue());
            }
        }
    }

    private void createUser(final RabbitManagementApiFacade apiClient, final RabbitMQUser desiredUser) {
        final Secret createdSecret = secretsController.createOrUpdate(desiredUser.getUserSecret());

        createOrUpdateUser(apiClient, desiredUser, passwordConverter.convertPasswordToHash(secretsController.decodeSecretPayload(createdSecret.getData().get(Constants.Secrets.PASSWORD_KEY))));
    }

    private void updateExistingUser(final RabbitManagementApiFacade apiClient, final RabbitMQUser desiredUser) {
        final Secret userSecret = secretsController.get(RabbitMQSecrets.getUserSecretName(desiredUser.getUsername(), desiredUser.getClusterMetadata().getName()), desiredUser.getClusterMetadata().getNamespace());

        createOrUpdateUser(apiClient, desiredUser, passwordConverter.convertPasswordToHash(secretsController.decodeSecretPayload(userSecret.getData().get(Constants.Secrets.PASSWORD_KEY))));
    }

    private void createOrUpdateUser(final RabbitManagementApiFacade apiClient, final RabbitMQUser desiredUser, final String passwordHash) {
        try {
            final User user = new User()
                    .withName(desiredUser.getUsername())
                    .withPasswordHash(passwordHash)
                    .withTags(Joiner.on(",").join(desiredUser.getTags()));
            apiClient.createUser(user.getName(), user);
        } catch (final RabbitManagementApiException e) {
            log.error(String.format("Failed to create/update user %s", desiredUser.getUsername()), e);
        }

        updateVhosts(apiClient, desiredUser);
    }

    private void updateVhosts(final RabbitManagementApiFacade apiClient, final RabbitMQUser user) {
        for (final VhostPermissions vhost : user.getVhostPermissions()) {
            final Permission existingPermissions;

            try {
                existingPermissions = apiClient.getPermission(vhost.getVhostName(), user.getUsername());
            } catch (final Exception ex) {
                log.error(String.format("Failed to retrieve vhost permissions for user %s in vhost %s", user.getUsername(), vhost.getVhostName()), ex);
                continue;
            }

            try {
                final Permission desiredPermissions = new Permission()
                        .withRead(Pattern.compile(vhost.getPermissions().getRead()))
                        .withWrite(Pattern.compile(vhost.getPermissions().getWrite()))
                        .withConfigure(Pattern.compile(vhost.getPermissions().getConfigure()));

                if (!permissionsMatch(desiredPermissions, existingPermissions)) {
                    apiClient.createPermission(vhost.getVhostName(), user.getUsername(), desiredPermissions);
                }
            } catch (final RabbitManagementApiException ex) {
                log.error(String.format("Failed to set vhost permissions for user %s in vhost %s", user.getUsername(), vhost.getVhostName()), ex);
            }
        }
    }

    private boolean permissionsMatch(final Permission desired, final Permission existing) {
        return existing != null &&
                desired.getRead().pattern().equals(existing.getRead().pattern()) &&
                desired.getWrite().pattern().equals(existing.getWrite().pattern()) &&
                desired.getConfigure().pattern().equals(existing.getConfigure().pattern());
    }
}
