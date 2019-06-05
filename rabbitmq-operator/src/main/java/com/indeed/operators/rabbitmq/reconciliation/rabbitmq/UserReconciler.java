package com.indeed.operators.rabbitmq.reconciliation.rabbitmq;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.indeed.operators.rabbitmq.Constants;
import com.indeed.operators.rabbitmq.api.RabbitMQPasswordConverter;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiException;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiFacade;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiProvider;
import com.indeed.operators.rabbitmq.controller.SecretsController;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.VhostPermissions;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQCluster;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQUser;
import com.indeed.rabbitmq.admin.pojo.Permission;
import com.indeed.rabbitmq.admin.pojo.User;
import io.fabric8.kubernetes.api.model.Secret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        final RabbitManagementApiFacade apiClient = managementApiProvider.getApi(cluster);

        final Map<String, RabbitMQUser> desiredUsers = cluster.getUsers().stream().collect(Collectors.toMap(RabbitMQUser::getUsername, user -> user));
        final Map<String, User> existingUsers = apiClient.listUsers()
                .stream()
                .filter(user -> !PERMANENT_USERS.contains(user.getName()))
                .collect(Collectors.toMap(User::getName, user -> user));

        deleteObsoleteUsers(desiredUsers, existingUsers, apiClient);
        createMissingUsers(desiredUsers, existingUsers, apiClient);
        updateExistingUser(desiredUsers, existingUsers, apiClient);

        desiredUsers.values().forEach(user -> updateVhosts(apiClient, user));
    }

    private void createMissingUsers(final Map<String, RabbitMQUser> desiredUsers, final Map<String, User> existingUsers, final RabbitManagementApiFacade apiClient) {
        final List<RabbitMQUser> usersToCreate = desiredUsers.entrySet().stream()
                .filter(desiredUser -> !existingUsers.containsKey(desiredUser.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        for (final RabbitMQUser user : usersToCreate) {
            final Secret createdSecret = secretsController.createOrUpdate(user.getUserSecret());

            createOrUpdateUser(apiClient, user, passwordConverter.convertPasswordToHash(secretsController.decodeSecretPayload(createdSecret.getData().get(Constants.Secrets.PASSWORD_KEY))));
        }
    }

    private void updateExistingUser(final Map<String, RabbitMQUser> desiredUsers, final Map<String, User> existingUsers, final RabbitManagementApiFacade apiClient) {
        final List<RabbitMQUser> usersToUpdate = desiredUsers.entrySet().stream()
                .filter(desiredUser -> existingUsers.containsKey(desiredUser.getKey()) && !usersMatch(desiredUser.getValue(), existingUsers.get(desiredUser.getKey())))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        for (final RabbitMQUser user : usersToUpdate) {
            createOrUpdateUser(apiClient, user, passwordConverter.convertPasswordToHash(secretsController.decodeSecretPayload(user.getUserSecret().getData().get(Constants.Secrets.PASSWORD_KEY))));
        }
    }

    private void deleteObsoleteUsers(final Map<String, RabbitMQUser> desiredUsers, final Map<String, User> existingUsers, final RabbitManagementApiFacade apiClient) {
        final List<User> usersToDelete = existingUsers.entrySet().stream()
                .filter(existingUser -> !desiredUsers.containsKey(existingUser.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        for (final User user : usersToDelete) {
            try {
                apiClient.deleteUser(user.getName());
            } catch (final RabbitManagementApiException ex) {
                log.error("Failed to delete user with name {}", user.getName(), ex);
            }
        }
    }

    private void createOrUpdateUser(final RabbitManagementApiFacade apiClient, final RabbitMQUser desiredUser, final String passwordHash) {
        final User user = new User()
                .withName(desiredUser.getUsername())
                .withPasswordHash(passwordHash)
                .withTags(Joiner.on(",").join(desiredUser.getTags()));

        apiClient.createUser(user.getName(), user);
    }

    private void updateVhosts(final RabbitManagementApiFacade apiClient, final RabbitMQUser user) {
        for (final VhostPermissions vhost : user.getVhostPermissions()) {
            final Optional<Permission> maybeExistingPermission;

            try {
                maybeExistingPermission = apiClient.listUserPermissions(user.getUsername()).stream()
                        .filter(permission -> permission.getVhost().equals(vhost.getVhostName()))
                        .findFirst();
            } catch (final RabbitManagementApiException ex) {
                log.error(String.format("Failed to retrieve vhost permissions for user %s in vhost %s", user.getUsername(), vhost.getVhostName()), ex);
                continue;
            }

            try {
                final Permission desiredPermissions = new Permission()
                        .withRead(Pattern.compile(vhost.getPermissions().getRead()))
                        .withWrite(Pattern.compile(vhost.getPermissions().getWrite()))
                        .withConfigure(Pattern.compile(vhost.getPermissions().getConfigure()));

                if (!maybeExistingPermission.isPresent() || !permissionsMatch(desiredPermissions, maybeExistingPermission.get())) {
                    apiClient.createPermission(vhost.getVhostName(), user.getUsername(), desiredPermissions);
                }
            } catch (final RabbitManagementApiException ex) {
                log.error(String.format("Failed to set vhost permissions for user %s in vhost %s", user.getUsername(), vhost.getVhostName()), ex);
            }
        }
    }

    private boolean usersMatch(final RabbitMQUser desired, final User existing) {
        return existing != null &&
                desired.getUsername().equals(existing.getName()) &&
                Sets.newHashSet(desired.getTags()).equals(Sets.newHashSet(existing.getTags().split(",")));
    }

    private boolean permissionsMatch(final Permission desired, final Permission existing) {
        return existing != null &&
                existing.getRead().pattern() != null &&
                existing.getWrite().pattern() != null &&
                existing.getConfigure().pattern() != null &&
                desired.getRead().pattern().equals(existing.getRead().pattern()) &&
                desired.getWrite().pattern().equals(existing.getWrite().pattern()) &&
                desired.getConfigure().pattern().equals(existing.getConfigure().pattern());
    }
}
