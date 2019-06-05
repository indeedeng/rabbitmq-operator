package com.indeed.operators.rabbitmq.reconciliation.validators;

import com.indeed.operators.rabbitmq.model.crd.rabbitmq.ClusterSpec;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.OperatorPolicySpec;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class OperatorPolicyValidator implements RabbitClusterValidator {
    @Override
    public List<String> validate(final ClusterSpec clusterSpec) {
        final List<OperatorPolicySpec> policies = clusterSpec.getOperatorPolicies();

        final List<String> errors = new ArrayList<>();

        policies.forEach(policy -> {
            try {
                Pattern.compile(policy.getPattern());
            } catch (final PatternSyntaxException ex) {
                errors.add(String.format("Invalid pattern for policy %s: %s", policy.getName(), ex.getMessage()));
            }

            if (!policy.getApplyTo().equals("queues")) {
                errors.add(String.format("Operator policy applyTo value must be 'queues', but operator policy %s had applyTo: %s", policy.getName(), policy.getApplyTo()));
            }
        });

        return errors;
    }
}
