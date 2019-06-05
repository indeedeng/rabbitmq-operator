package com.indeed.operators.rabbitmq.reconciliation.validators;

import com.google.common.collect.Lists;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.ClusterSpec;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.UserSpec;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.VhostOperationPermissions;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.VhostPermissions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUserValidator {

    private final UserValidator validator = new UserValidator();

    @Test
    public void testValidate_validUser() {
        final VhostPermissions permissions = new VhostPermissions("vhostname", new VhostOperationPermissions(".*", ".*", ".*"));
        final UserSpec userSpec = new UserSpec("username", Lists.newArrayList(permissions), Lists.newArrayList("administrator"));

        final ClusterSpec clusterSpec = buildClusterSpec(Lists.newArrayList(userSpec));

        assertTrue(validator.validate(clusterSpec).isEmpty());
    }

    @Test
    public void testValidate_invalidTag() {
        final VhostPermissions permissions = new VhostPermissions("vhostname", new VhostOperationPermissions(".*", ".*", ".*"));
        final UserSpec userSpec = new UserSpec("username", Lists.newArrayList(permissions), Lists.newArrayList("a random tag"));

        final ClusterSpec clusterSpec = buildClusterSpec(Lists.newArrayList(userSpec));

        final List<String> errors = validator.validate(clusterSpec);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("tag"));
    }

    @Test
    public void testValidate_invalidConfigure() {
        final VhostPermissions permissions = new VhostPermissions("vhostname", new VhostOperationPermissions("[[[", ".*", ".*"));
        final UserSpec userSpec = new UserSpec("username", Lists.newArrayList(permissions), Lists.newArrayList("administrator"));

        final ClusterSpec clusterSpec = buildClusterSpec(Lists.newArrayList(userSpec));

        final List<String> errors = validator.validate(clusterSpec);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("configure"));
    }

    @Test
    public void testValidate_invalidWrite() {
        final VhostPermissions permissions = new VhostPermissions("vhostname", new VhostOperationPermissions(".*", "[[[", ".*"));
        final UserSpec userSpec = new UserSpec("username", Lists.newArrayList(permissions), Lists.newArrayList("administrator"));

        final ClusterSpec clusterSpec = buildClusterSpec(Lists.newArrayList(userSpec));

        final List<String> errors = validator.validate(clusterSpec);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("write"));
    }

    @Test
    public void testValidate_invalidRead() {
        final VhostPermissions permissions = new VhostPermissions("vhostname", new VhostOperationPermissions(".*", ".*", "[[["));
        final UserSpec userSpec = new UserSpec("username", Lists.newArrayList(permissions), Lists.newArrayList("administrator"));

        final ClusterSpec clusterSpec = buildClusterSpec(Lists.newArrayList(userSpec));

        final List<String> errors = validator.validate(clusterSpec);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("read"));
    }

    private ClusterSpec buildClusterSpec(final List<UserSpec> users) {
        return new ClusterSpec(0.0, users, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }
}
