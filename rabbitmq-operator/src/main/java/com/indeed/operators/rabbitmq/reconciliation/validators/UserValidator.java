package com.indeed.operators.rabbitmq.reconciliation.validators;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.ClusterSpec;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.UserSpec;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.VhostOperationPermissions;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class UserValidator implements RabbitClusterValidator {

    private static final Set<String> VALID_USER_TAGS = Sets.newHashSet("administrator", "monitoring", "policymaker", "management", "impersonator");

    @Override
    public List<String> validate(final ClusterSpec clusterSpec) {
        final List<UserSpec> users = clusterSpec.getUsers();

        final List<String> errors = Lists.newArrayList();

        users.forEach(user -> {
            if (user.getVhosts() != null && !user.getVhosts().isEmpty()) {
                user.getVhosts().forEach(vhost -> {
                    final VhostOperationPermissions permissions = vhost.getPermissions();

                    try {
                        Pattern.compile(permissions.getRead());
                    } catch (final PatternSyntaxException ex) {
                        errors.add(String.format("vhost 'read' permissions for user [%s] on vhost [%s] were invalid: %s", user.getUsername(), vhost.getVhostName(), ex.getMessage()));
                    }

                    try {
                        Pattern.compile(permissions.getWrite());
                    } catch (final PatternSyntaxException ex) {
                        errors.add(String.format("vhost 'write' permissions for user [%s] on vhost [%s] were invalid: %s", user.getUsername(), vhost.getVhostName(), ex.getMessage()));
                    }

                    try {
                        Pattern.compile(permissions.getConfigure());
                    } catch (final PatternSyntaxException ex) {
                        errors.add(String.format("vhost 'configure' permissions for user [%s] on vhost [%s] were invalid: %s", user.getUsername(), vhost.getVhostName(), ex.getMessage()));
                    }
                });
            }

            if (user.getTags() != null && !user.getTags().isEmpty()) {
                user.getTags().stream()
                        .filter(tag -> !VALID_USER_TAGS.contains(tag))
                        .forEach(tag -> errors.add(String.format("Tag [%s] for user [%s] is invalid", tag, user.getUsername())));
            }
        });

        return errors;
    }
}
