package com.indeed.operators.rabbitmq.reconciliation.validators;

import com.indeed.operators.rabbitmq.model.crd.rabbitmq.ClusterSpec;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.PolicySpec;
import com.indeed.rabbitmq.admin.pojo.Policy;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PolicyValidator implements RabbitClusterValidator {
    @Override
    public List<String> validate(final ClusterSpec clusterSpec) {
        final List<PolicySpec> policies = clusterSpec.getPolicies();

        final List<String> errors = new ArrayList<>();

        policies.forEach(policy -> {
            try {
                Pattern.compile(policy.getPattern());
            } catch (final PatternSyntaxException ex) {
                errors.add(String.format("Invalid pattern for policy %s: %s", policy.getName(), ex.getMessage()));
            }

            try {
                Policy.ApplyTo.fromValue(policy.getApplyTo());
            } catch (final IllegalArgumentException ex) {
                errors.add(String.format("Invalid applyTo for policy %s: %s", policy.getName(), ex.getMessage()));
            }
        });

        return errors;
    }
}
