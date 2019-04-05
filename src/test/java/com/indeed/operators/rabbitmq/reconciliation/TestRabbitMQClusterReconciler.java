package com.indeed.operators.rabbitmq.reconciliation;

import com.google.common.collect.Maps;
import com.indeed.operators.rabbitmq.controller.PersistentVolumeClaimController;
import com.indeed.operators.rabbitmq.controller.PodDisruptionBudgetController;
import com.indeed.operators.rabbitmq.controller.SecretsController;
import com.indeed.operators.rabbitmq.controller.ServicesController;
import com.indeed.operators.rabbitmq.controller.StatefulSetController;
import com.indeed.operators.rabbitmq.controller.crd.RabbitMQResourceController;
import com.indeed.operators.rabbitmq.model.Labels;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.RabbitMQCustomResource;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.RabbitMQCustomResourceBuilder;
import com.indeed.operators.rabbitmq.reconciliation.rabbitmq.ClusterUsersReconciler;
import com.indeed.operators.rabbitmq.reconciliation.rabbitmq.ShovelReconciler;
import com.indeed.operators.rabbitmq.reconciliation.rabbitmq.RabbitMQClusterFactory;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestRabbitMQClusterReconciler {

    @Mock
    private RabbitMQClusterFactory clusterFactory;

    @Mock
    private RabbitMQResourceController controller;

    @Mock
    private SecretsController secretsController;

    @Mock
    private ServicesController servicesController;

    @Mock
    private StatefulSetController statefulSetController;

    @Mock
    private PodDisruptionBudgetController podDisruptionBudgetController;

    @Mock
    private PersistentVolumeClaimController persistentVolumeClaimController;

    @Mock
    private ShovelReconciler shovelReconciler;

    @Mock
    private ClusterUsersReconciler usersReconciler;

    private RabbitMQClusterReconciler reconciler;

    @BeforeEach
    public void setup() {
        reconciler = new RabbitMQClusterReconciler(clusterFactory, controller, secretsController, servicesController, statefulSetController, podDisruptionBudgetController, persistentVolumeClaimController, shovelReconciler, usersReconciler);
    }

    @Test
    public void shouldSkipReonciliation() throws InterruptedException {
        final Reconciliation rec = new Reconciliation("resourceName", "clusterName", "namespace", "type");
        final Map<String, String> labels = Maps.newHashMap();
        labels.put(Labels.Indeed.LOCKED_BY, "somevalue");

        final RabbitMQCustomResource resource = new RabbitMQCustomResourceBuilder()
                .withMetadata(
                        new ObjectMetaBuilder()
                                .withLabels(labels)
                                .build()
                )
                .build();

        when(controller.get(rec.getResourceName(), rec.getNamespace())).thenReturn(resource);

        reconciler.reconcile(rec);

        verifyZeroInteractions(secretsController);
        verifyZeroInteractions(statefulSetController);
        verifyZeroInteractions(podDisruptionBudgetController);
        verifyZeroInteractions(persistentVolumeClaimController);
    }

    @Test
    public void shouldSkipReonciliationNullResource() throws InterruptedException {
        final Reconciliation rec = new Reconciliation("resourceName", "clusterName", "namespace", "type");
        final Map<String, String> labels = Maps.newHashMap();
        labels.put("locked", "somevalue");

        when(controller.get(rec.getResourceName(), rec.getNamespace())).thenReturn(null);

        reconciler.reconcile(rec);

        verifyZeroInteractions(secretsController);
        verifyZeroInteractions(statefulSetController);
        verifyZeroInteractions(podDisruptionBudgetController);
        verifyZeroInteractions(persistentVolumeClaimController);
    }
}
