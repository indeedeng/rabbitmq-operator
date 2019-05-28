package com.indeed.operators.rabbitmq.reconciliation.rabbitmq;

import com.google.common.collect.Lists;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiFacade;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiProvider;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.PolicyDefinitionSpec;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.PolicySpec;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQCluster;
import com.indeed.rabbitmq.admin.pojo.Policy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestPolicyReconciler {

    @Mock
    private RabbitManagementApiProvider apiProvider;

    @InjectMocks
    private PolicyReconciler policyReconciler;

    @Test
    public void testReconcile_newPolicy() {
        final PolicyDefinitionSpec policyDefinitionSpec = new PolicyDefinitionSpec("alt exchange", null, null, null, null, null, null, null, null, null, null, null, null);
        final PolicySpec desiredPolicy = new PolicySpec("vhost", "name", "pattern", "queues", policyDefinitionSpec, 1);
        final RabbitMQCluster cluster = buildCluster(Lists.newArrayList(desiredPolicy));

        final RabbitManagementApiFacade api = mock(RabbitManagementApiFacade.class);

        when(apiProvider.getApi(cluster)).thenReturn(api);
        when(api.listPolicies()).thenReturn(Collections.emptyList());

        policyReconciler.reconcile(cluster);

        final ArgumentCaptor<Policy> policyCaptor = ArgumentCaptor.forClass(Policy.class);
        verify(api).createPolicy(eq("vhost"), eq("name"), policyCaptor.capture());

        final Policy capturedPolicy = policyCaptor.getValue();
        assertEquals("vhost", capturedPolicy.getVhost());
        assertEquals("name", capturedPolicy.getName());
        assertEquals("pattern", capturedPolicy.getPattern().pattern());
        assertEquals(Policy.ApplyTo.QUEUES, capturedPolicy.getApplyTo());
        assertEquals(desiredPolicy.getDefinition().asDefinition(), capturedPolicy.getDefinition());
        assertEquals(1L, capturedPolicy.getPriority());
    }

    @Test
    public void testReconcile_updatePolicy() {
        final PolicyDefinitionSpec policyDefinitionSpec = new PolicyDefinitionSpec("new alt exchange", null, null, null, null, null, null, null, null, null, null, null, null);
        final PolicySpec desiredPolicy = new PolicySpec("vhost", "name", "newpattern", "exchanges", policyDefinitionSpec, 5);
        final Policy existingPolicy = new Policy()
                .withVhost("vhost")
                .withName("name")
                .withPattern(Pattern.compile("pattern"))
                .withApplyTo(Policy.ApplyTo.QUEUES)
                .withDefinition(desiredPolicy.getDefinition().asDefinition())
                .withPriority(1L);
        final RabbitMQCluster cluster = buildCluster(Lists.newArrayList(desiredPolicy));

        final RabbitManagementApiFacade api = mock(RabbitManagementApiFacade.class);

        when(apiProvider.getApi(cluster)).thenReturn(api);
        when(api.listPolicies()).thenReturn(Lists.newArrayList(existingPolicy));

        policyReconciler.reconcile(cluster);

        final ArgumentCaptor<Policy> policyCaptor = ArgumentCaptor.forClass(Policy.class);
        verify(api).createPolicy(eq("vhost"), eq("name"), policyCaptor.capture());

        final Policy capturedPolicy = policyCaptor.getValue();
        assertEquals("vhost", capturedPolicy.getVhost());
        assertEquals("name", capturedPolicy.getName());
        assertEquals("newpattern", capturedPolicy.getPattern().pattern());
        assertEquals(Policy.ApplyTo.EXCHANGES, capturedPolicy.getApplyTo());
        assertEquals(desiredPolicy.getDefinition().asDefinition(), capturedPolicy.getDefinition());
        assertEquals(5L, capturedPolicy.getPriority());
    }

    @Test
    public void testRecocile_deleteUnknownPolicy() {
        final PolicyDefinitionSpec policyDefinitionSpec = new PolicyDefinitionSpec("alt exchange", null, null, null, null, null, null, null, null, null, null, null, null);
        final PolicySpec desiredPolicy = new PolicySpec("vhost", "name", "pattern", "queues", policyDefinitionSpec, 1);
        final Policy existingPolicy = new Policy()
                .withVhost("vhost")
                .withName("name")
                .withPattern(Pattern.compile("pattern"))
                .withApplyTo(Policy.ApplyTo.QUEUES)
                .withDefinition(desiredPolicy.getDefinition().asDefinition())
                .withPriority(1L);
        final Policy unknownPolicy = new Policy()
                .withVhost("vhost")
                .withName("unknown-policy")
                .withPattern(Pattern.compile("pattern"))
                .withApplyTo(Policy.ApplyTo.QUEUES)
                .withDefinition(desiredPolicy.getDefinition().asDefinition())
                .withPriority(1L);
        final RabbitMQCluster cluster = buildCluster(Lists.newArrayList(desiredPolicy));

        final RabbitManagementApiFacade api = mock(RabbitManagementApiFacade.class);

        when(apiProvider.getApi(cluster)).thenReturn(api);
        when(api.listPolicies()).thenReturn(Lists.newArrayList(existingPolicy, unknownPolicy));

        policyReconciler.reconcile(cluster);

        verify(api, times(1)).deletePolicy("vhost", "unknown-policy");
        verify(api, times(1)).deletePolicy(anyString(), anyString());
    }

    public RabbitMQCluster buildCluster(final List<PolicySpec> policies) {
        return RabbitMQCluster.newBuilder()
                .withName("mycluster")
                .withNamespace("ns")
                .withPolicies(policies)
                .build();
    }
}
