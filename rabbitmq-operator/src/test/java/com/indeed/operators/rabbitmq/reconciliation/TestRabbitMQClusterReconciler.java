package com.indeed.operators.rabbitmq.reconciliation;

import com.google.common.collect.Lists;
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
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.RabbitMQCustomResourceSpecBuilder;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQCluster;
import com.indeed.operators.rabbitmq.reconciliation.rabbitmq.UserReconciler;
import com.indeed.operators.rabbitmq.reconciliation.rabbitmq.OperatorPolicyReconciler;
import com.indeed.operators.rabbitmq.reconciliation.rabbitmq.PolicyReconciler;
import com.indeed.operators.rabbitmq.reconciliation.rabbitmq.RabbitMQClusterFactory;
import com.indeed.operators.rabbitmq.reconciliation.rabbitmq.ShovelReconciler;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetSpecBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static com.indeed.operators.rabbitmq.Constants.RABBITMQ_STORAGE_NAME;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestRabbitMQClusterReconciler {

    private final String NAME = "name";
    private final String NAMESPACE = "namespace";

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
    private UserReconciler usersReconciler;

    @Mock
    private PolicyReconciler policyReconciler;

    @Mock
    private OperatorPolicyReconciler operatorPolicyReconciler;

    private RabbitMQClusterReconciler reconciler;

    @BeforeEach
    void setup() {
        reconciler = new RabbitMQClusterReconciler(clusterFactory, controller, secretsController, servicesController, statefulSetController, podDisruptionBudgetController, persistentVolumeClaimController, shovelReconciler, usersReconciler, policyReconciler, operatorPolicyReconciler);
    }

    @Test
    void shouldSkipReconciliation() throws InterruptedException {
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
    void shouldSkipReconciliationNullResource() throws InterruptedException {
        final Reconciliation rec = new Reconciliation("resourceName", "clusterName", "namespace", "type");

        when(controller.get(rec.getResourceName(), rec.getNamespace())).thenReturn(null);

        reconciler.reconcile(rec);

        verifyZeroInteractions(secretsController);
        verifyZeroInteractions(statefulSetController);
        verifyZeroInteractions(podDisruptionBudgetController);
        verifyZeroInteractions(persistentVolumeClaimController);
    }

    @Test
    void scalingDownDeletesOrphanPVCs() throws InterruptedException {
        final Reconciliation rec = new Reconciliation(NAME, NAME, NAMESPACE, "type");

        final StatefulSet originalStatefulSet = new StatefulSet(
                "apps/v1",
                "StatefulSet",
                new ObjectMetaBuilder().build(),
                new StatefulSetSpecBuilder().withReplicas(4).build(),
                null
        );

        final RabbitMQCustomResource scaledResource = new RabbitMQCustomResourceBuilder()
                .withMetadata(
                        new ObjectMetaBuilder()
                                .withName(NAME)
                                .withNamespace(NAMESPACE)
                                .build()
                )
                .withSpec(
                        new RabbitMQCustomResourceSpecBuilder()
                                .withReplicas(3)
                                .build()
                )
                .build();

        when(controller.get(rec.getResourceName(), rec.getNamespace())).thenReturn(scaledResource);

        when(clusterFactory.fromCustomResource(scaledResource)).thenReturn(
                RabbitMQCluster.newBuilder()
                        .withName(NAME)
                        .withNamespace(NAMESPACE)
                        .withAdminSecret(null)
                        .withErlangCookieSecret(null)
                        .withMainService(null)
                        .withDiscoveryService(null)
                        .withLoadBalancerService(Optional.empty())
                        .withStatefulSet(originalStatefulSet)
                        .withPodDisruptionBudget(null)
                        .withShovels(Lists.newArrayList())
                        .withUsers(Lists.newArrayList())
                        .withPolicies(Lists.newArrayList())
                        .withOperatorPolicies(Lists.newArrayList())
                        .build()
        );

        // This call will happen twice.  In both cases it will occur before the StatefulSet has been patched, hence it
        // will reflect the origin unscaled replica count.
        when(statefulSetController.get(NAME, NAMESPACE)).thenReturn(originalStatefulSet);

        reconciler.reconcile(rec);

        verify(persistentVolumeClaimController).delete(RABBITMQ_STORAGE_NAME + "-" + NAME + "-3", NAMESPACE);
        verifyNoMoreInteractions(persistentVolumeClaimController);
    }

    @Test
    void scalingDownPreservesOrphanPVCs() throws InterruptedException {
        final Reconciliation rec = new Reconciliation(NAME, NAME, NAMESPACE, "type");

        final StatefulSet originalStatefulSet = new StatefulSet(
                "apps/v1",
                "StatefulSet",
                new ObjectMetaBuilder().build(),
                new StatefulSetSpecBuilder().withReplicas(4).build(),
                null
        );

        final RabbitMQCustomResource scaledResource = new RabbitMQCustomResourceBuilder()
                .withMetadata(
                        new ObjectMetaBuilder()
                                .withName(NAME)
                                .withNamespace(NAMESPACE)
                                .build()
                )
                .withSpec(
                        new RabbitMQCustomResourceSpecBuilder()
                                .withReplicas(3)
                                .withPreserveOrphanPVCs(true)
                                .build()
                )
                .build();

        when(controller.get(rec.getResourceName(), rec.getNamespace())).thenReturn(scaledResource);

        when(clusterFactory.fromCustomResource(scaledResource)).thenReturn(
                RabbitMQCluster.newBuilder()
                        .withName(NAME)
                        .withNamespace(NAMESPACE)
                        .withAdminSecret(null)
                        .withErlangCookieSecret(null)
                        .withMainService(null)
                        .withDiscoveryService(null)
                        .withLoadBalancerService(Optional.empty())
                        .withStatefulSet(originalStatefulSet)
                        .withPodDisruptionBudget(null)
                        .withShovels(Lists.newArrayList())
                        .withUsers(Lists.newArrayList())
                        .withPolicies(Lists.newArrayList())
                        .withOperatorPolicies(Lists.newArrayList())
                        .build()
        );

        // This call will happen twice.  In both cases it will occur before the StatefulSet has been patched, hence it
        // will reflect the origin unscaled replica count.
        when(statefulSetController.get(NAME, NAMESPACE)).thenReturn(originalStatefulSet);

        reconciler.reconcile(rec);

        verifyZeroInteractions(persistentVolumeClaimController);
        verifyNoMoreInteractions(persistentVolumeClaimController);
    }
}
