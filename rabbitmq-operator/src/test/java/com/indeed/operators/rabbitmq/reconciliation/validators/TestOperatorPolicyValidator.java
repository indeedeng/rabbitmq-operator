package com.indeed.operators.rabbitmq.reconciliation.validators;

import com.google.common.collect.Lists;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.ClusterSpec;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.OperatorPolicyDefinitionSpec;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.OperatorPolicySpec;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestOperatorPolicyValidator {
    private static final OperatorPolicyDefinitionSpec POLICY_DEFINITION = new OperatorPolicyDefinitionSpec(0L, 0L, 0L, 0L);

    private final OperatorPolicyValidator validator = new OperatorPolicyValidator();

    @Test
    public void testValidate_validSpec() {
        final List<OperatorPolicySpec> policies = Lists.newArrayList(
                new OperatorPolicySpec("vhost1", "name1", "pattern", "queues", POLICY_DEFINITION, 1L),
                new OperatorPolicySpec("////", "name2", "[a-z]", "queues", POLICY_DEFINITION, 10L)
        );

        final ClusterSpec clusterSpec = buildClusterSpec(policies);

        assertTrue(validator.validate(clusterSpec).isEmpty());
    }

    @Test
    public void testValidate_invalidPattern() {
        final List<OperatorPolicySpec> policies = Lists.newArrayList(
                new OperatorPolicySpec("vhost1", "name1", "[[[[[[", "queues", POLICY_DEFINITION, 1L)
        );

        final ClusterSpec clusterSpec = buildClusterSpec(policies);

        final List<String> errors = validator.validate(clusterSpec);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("pattern"));
    }

    @Test
    public void testValidate_invalidApplyTo() {
        final List<OperatorPolicySpec> policies = Lists.newArrayList(
                new OperatorPolicySpec("vhost1", "name1", "pattern", "exchanges", POLICY_DEFINITION, 1L)
        );

        final ClusterSpec clusterSpec = buildClusterSpec(policies);

        final List<String> errors = validator.validate(clusterSpec);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("applyTo"));
    }

    @Test
    public void testValidate_multipleInvalid() {
        final List<OperatorPolicySpec> policies = Lists.newArrayList(
                new OperatorPolicySpec("vhost1", "name1", "[[[[[[", "queues", POLICY_DEFINITION, 1L),
                new OperatorPolicySpec("vhost1", "name1", "pattern", "exchanges", POLICY_DEFINITION, 1L)
        );

        final ClusterSpec clusterSpec = buildClusterSpec(policies);

        final List<String> errors = validator.validate(clusterSpec);

        assertEquals(2, errors.size());
    }

    private ClusterSpec buildClusterSpec(final List<OperatorPolicySpec> policies) {
        return new ClusterSpec(0.0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), policies);
    }
}
