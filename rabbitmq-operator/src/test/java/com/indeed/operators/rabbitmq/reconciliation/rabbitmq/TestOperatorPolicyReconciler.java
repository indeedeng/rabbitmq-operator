package com.indeed.operators.rabbitmq.reconciliation.rabbitmq;

import com.google.common.collect.Lists;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiFacade;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiProvider;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.OperatorPolicyDefinitionSpec;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.OperatorPolicySpec;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQCluster;
import com.indeed.rabbitmq.admin.pojo.OperatorPolicy;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestOperatorPolicyReconciler {

    @Mock
    private RabbitManagementApiProvider apiProvider;

    @InjectMocks
    private OperatorPolicyReconciler operatorPolicyReconciler;

    @Test
    public void testReconcile_newOperatorPolicy() {
        final OperatorPolicyDefinitionSpec operatorPolicyDefinitionSpec = new OperatorPolicyDefinitionSpec(5L, null, null, null);
        final OperatorPolicySpec desiredOperatorPolicy = new OperatorPolicySpec("vhost", "name", "pattern", "queues", operatorPolicyDefinitionSpec, 1);
        final RabbitMQCluster cluster = buildCluster(Lists.newArrayList(desiredOperatorPolicy));

        final RabbitManagementApiFacade api = mock(RabbitManagementApiFacade.class);

        when(apiProvider.getApi(cluster)).thenReturn(api);
        when(api.listOperatorPolicies()).thenReturn(Collections.emptyList());

        operatorPolicyReconciler.reconcile(cluster);

        final ArgumentCaptor<OperatorPolicy> operatorPolicyCaptor = ArgumentCaptor.forClass(OperatorPolicy.class);
        verify(api).createOperatorPolicy(eq("vhost"), eq("name"), operatorPolicyCaptor.capture());

        final OperatorPolicy capturedOperatorPolicy = operatorPolicyCaptor.getValue();
        assertEquals("vhost", capturedOperatorPolicy.getVhost());
        assertEquals("name", capturedOperatorPolicy.getName());
        assertEquals("pattern", capturedOperatorPolicy.getPattern().pattern());
        assertEquals(OperatorPolicy.ApplyTo.QUEUES, capturedOperatorPolicy.getApplyTo());
        assertEquals(desiredOperatorPolicy.getDefinition().asOperatorPolicyDefinition(), capturedOperatorPolicy.getOperatorPolicyDefinition());
        assertEquals(1L, capturedOperatorPolicy.getPriority());
    }

    @Test
    public void testReconcile_updateOperatorPolicy() {
        final OperatorPolicyDefinitionSpec operatorPolicyDefinitionSpec = new OperatorPolicyDefinitionSpec(5L, null, null, null);
        final OperatorPolicySpec desiredOperatorPolicy = new OperatorPolicySpec("vhost", "name", "newpattern", "queues", operatorPolicyDefinitionSpec, 5);
        final OperatorPolicy existingOperatorPolicy = new OperatorPolicy()
                .withVhost("vhost")
                .withName("name")
                .withPattern(Pattern.compile("pattern"))
                .withApplyTo(OperatorPolicy.ApplyTo.QUEUES)
                .withOperatorPolicyDefinition(desiredOperatorPolicy.getDefinition().asOperatorPolicyDefinition())
                .withPriority(1L);
        final RabbitMQCluster cluster = buildCluster(Lists.newArrayList(desiredOperatorPolicy));

        final RabbitManagementApiFacade api = mock(RabbitManagementApiFacade.class);

        when(apiProvider.getApi(cluster)).thenReturn(api);
        when(api.listOperatorPolicies()).thenReturn(Lists.newArrayList(existingOperatorPolicy));

        operatorPolicyReconciler.reconcile(cluster);

        final ArgumentCaptor<OperatorPolicy> operatorPolicyCaptor = ArgumentCaptor.forClass(OperatorPolicy.class);
        verify(api).createOperatorPolicy(eq("vhost"), eq("name"), operatorPolicyCaptor.capture());

        final OperatorPolicy capturedOperatorPolicy = operatorPolicyCaptor.getValue();
        assertEquals("vhost", capturedOperatorPolicy.getVhost());
        assertEquals("name", capturedOperatorPolicy.getName());
        assertEquals("newpattern", capturedOperatorPolicy.getPattern().pattern());
        assertEquals(OperatorPolicy.ApplyTo.QUEUES, capturedOperatorPolicy.getApplyTo());
        assertEquals(desiredOperatorPolicy.getDefinition().asOperatorPolicyDefinition(), capturedOperatorPolicy.getOperatorPolicyDefinition());
        assertEquals(5L, capturedOperatorPolicy.getPriority());
    }

    @Test
    public void testReconcile_skipUpToDateOperatorPolicy() {
        final OperatorPolicyDefinitionSpec operatorPolicyDefinitionSpec = new OperatorPolicyDefinitionSpec(5L, null, null, null);
        final OperatorPolicySpec desiredOperatorPolicy = new OperatorPolicySpec("vhost", "name", "pattern", "queues", operatorPolicyDefinitionSpec, 5);
        final OperatorPolicy existingOperatorPolicy = new OperatorPolicy()
                .withVhost("vhost")
                .withName("name")
                .withPattern(Pattern.compile("pattern"))
                .withApplyTo(OperatorPolicy.ApplyTo.QUEUES)
                .withOperatorPolicyDefinition(desiredOperatorPolicy.getDefinition().asOperatorPolicyDefinition())
                .withPriority(5L);
        final RabbitMQCluster cluster = buildCluster(Lists.newArrayList(desiredOperatorPolicy));

        final RabbitManagementApiFacade api = mock(RabbitManagementApiFacade.class);

        when(apiProvider.getApi(cluster)).thenReturn(api);
        when(api.listOperatorPolicies()).thenReturn(Lists.newArrayList(existingOperatorPolicy));

        operatorPolicyReconciler.reconcile(cluster);

        verify(api, never()).createOperatorPolicy(any(), any(), any(OperatorPolicy.class));
    }

    @Test
    public void testRecocile_deleteUnknownOperatorPolicy() {
        final OperatorPolicyDefinitionSpec operatorPolicyDefinitionSpec = new OperatorPolicyDefinitionSpec(5L, null, null, null);
        final OperatorPolicySpec desiredOperatorPolicy = new OperatorPolicySpec("vhost", "name", "pattern", "queues", operatorPolicyDefinitionSpec, 1);
        final OperatorPolicy existingOperatorPolicy = new OperatorPolicy()
                .withVhost("vhost")
                .withName("name")
                .withPattern(Pattern.compile("pattern"))
                .withApplyTo(OperatorPolicy.ApplyTo.QUEUES)
                .withOperatorPolicyDefinition(desiredOperatorPolicy.getDefinition().asOperatorPolicyDefinition())
                .withPriority(1L);
        final OperatorPolicy unknownOperatorPolicy = new OperatorPolicy()
                .withVhost("vhost")
                .withName("unknown-operatorPolicy")
                .withPattern(Pattern.compile("pattern"))
                .withApplyTo(OperatorPolicy.ApplyTo.QUEUES)
                .withOperatorPolicyDefinition(desiredOperatorPolicy.getDefinition().asOperatorPolicyDefinition())
                .withPriority(1L);
        final RabbitMQCluster cluster = buildCluster(Lists.newArrayList(desiredOperatorPolicy));

        final RabbitManagementApiFacade api = mock(RabbitManagementApiFacade.class);

        when(apiProvider.getApi(cluster)).thenReturn(api);
        when(api.listOperatorPolicies()).thenReturn(Lists.newArrayList(existingOperatorPolicy, unknownOperatorPolicy));

        operatorPolicyReconciler.reconcile(cluster);

        verify(api, times(1)).deleteOperatorPolicy("vhost", "unknown-operatorPolicy");
        verify(api, times(1)).deleteOperatorPolicy(anyString(), anyString());
    }

    public RabbitMQCluster buildCluster(final List<OperatorPolicySpec> operatorPolicies) {
        return RabbitMQCluster.newBuilder()
                .withName("mycluster")
                .withNamespace("ns")
                .withOperatorPolicies(operatorPolicies)
                .build();
    }
}
