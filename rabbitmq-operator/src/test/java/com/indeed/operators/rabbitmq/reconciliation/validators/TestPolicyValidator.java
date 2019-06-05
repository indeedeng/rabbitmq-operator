package com.indeed.operators.rabbitmq.reconciliation.validators;

import com.google.common.collect.Lists;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.ClusterSpec;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.PolicyDefinitionSpec;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.PolicySpec;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPolicyValidator {
    private static final PolicyDefinitionSpec POLICY_DEFINITION = new PolicyDefinitionSpec(null, null, null, null, null, null, null, null, null, null, null, null, null);

    private final PolicyValidator validator = new PolicyValidator();

    @Test
    public void testValidate_validSpec() {
        final List<PolicySpec> policies = Lists.newArrayList(
                new PolicySpec("vhost1", "name1", "pattern", "queues", POLICY_DEFINITION, 1L),
                new PolicySpec("////", "name2", "[a-z]", "queues", POLICY_DEFINITION, 10L),
                new PolicySpec("////", "name2", "[a-z]", "exchanges", POLICY_DEFINITION, 10L)
        );

        final List<String> errors = validator.validate(buildClusterSpec(policies));

        assertTrue(errors.isEmpty());
    }

    @Test
    public void testValidate_invalidPattern() {
        final List<PolicySpec> policies = Lists.newArrayList(
                new PolicySpec("vhost1", "name1", "[[[[[[", "queues", POLICY_DEFINITION, 1L)
        );

        final List<String> errors = validator.validate(buildClusterSpec(policies));

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("pattern"));
    }

    @Test
    public void testValidate_invalidApplyTo() {
        final List<PolicySpec> policies = Lists.newArrayList(
                new PolicySpec("vhost1", "name1", "pattern", "blah", POLICY_DEFINITION, 1L)
        );

        final List<String> errors = validator.validate(buildClusterSpec(policies));

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("applyTo"));
    }

    @Test
    public void testValidate_multipleInvalid() {
        final List<PolicySpec> policies = Lists.newArrayList(
                new PolicySpec("vhost1", "name1", "[[[[[[", "queues", POLICY_DEFINITION, 1L),
                new PolicySpec("vhost1", "name1", "pattern", "blah", POLICY_DEFINITION, 1L)
        );

        final List<String> errors = validator.validate(buildClusterSpec(policies));

        assertEquals(2, errors.size());
    }

    private ClusterSpec buildClusterSpec(final List<PolicySpec> policies) {
        return new ClusterSpec(0.0, Collections.emptyList(), Collections.emptyList(), policies, Collections.emptyList());
    }
}
