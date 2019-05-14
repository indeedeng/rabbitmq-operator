package com.indeed.operators.rabbitmq.reconciliation;

import com.indeed.operators.rabbitmq.controller.PersistentVolumeClaimController;
import com.indeed.operators.rabbitmq.controller.PodDisruptionBudgetController;
import com.indeed.operators.rabbitmq.controller.SecretsController;
import com.indeed.operators.rabbitmq.controller.ServicesController;
import com.indeed.operators.rabbitmq.controller.StatefulSetController;
import com.indeed.operators.rabbitmq.controller.crd.RabbitMQResourceController;
import com.indeed.operators.rabbitmq.model.Labels;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.RabbitMQCustomResource;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQCluster;
import com.indeed.operators.rabbitmq.reconciliation.rabbitmq.UserReconciler;
import com.indeed.operators.rabbitmq.reconciliation.rabbitmq.OperatorPolicyReconciler;
import com.indeed.operators.rabbitmq.reconciliation.rabbitmq.PolicyReconciler;
import com.indeed.operators.rabbitmq.reconciliation.rabbitmq.RabbitMQClusterFactory;
import com.indeed.operators.rabbitmq.reconciliation.rabbitmq.ShovelReconciler;
import com.indeed.operators.rabbitmq.resources.RabbitMQServices;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.indeed.operators.rabbitmq.Constants.RABBITMQ_STORAGE_NAME;

public class RabbitMQClusterReconciler {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQClusterReconciler.class);

    private final RabbitMQClusterFactory clusterFactory;
    private final RabbitMQResourceController controller;
    private final SecretsController secretsController;
    private final ServicesController servicesController;
    private final StatefulSetController statefulSetController;
    private final PodDisruptionBudgetController podDisruptionBudgetController;
    private final PersistentVolumeClaimController persistentVolumeClaimController;
    private final ShovelReconciler shovelReconciler;
    private final UserReconciler usersReconciler;
    private final PolicyReconciler policyReconciler;
    private final OperatorPolicyReconciler operatorPolicyReconciler;

    public RabbitMQClusterReconciler(
            final RabbitMQClusterFactory clusterFactory,
            final RabbitMQResourceController controller,
            final SecretsController secretsController,
            final ServicesController servicesController,
            final StatefulSetController statefulSetController,
            final PodDisruptionBudgetController podDisruptionBudgetController,
            final PersistentVolumeClaimController persistentVolumeClaimController,
            final ShovelReconciler shovelReconciler,
            final UserReconciler usersReconciler,
            final PolicyReconciler policyReconciler,
            final OperatorPolicyReconciler operatorPolicyReconciler
    ) {
        this.clusterFactory = clusterFactory;
        this.controller = controller;
        this.secretsController = secretsController;
        this.servicesController = servicesController;
        this.statefulSetController = statefulSetController;
        this.podDisruptionBudgetController = podDisruptionBudgetController;
        this.persistentVolumeClaimController = persistentVolumeClaimController;
        this.shovelReconciler = shovelReconciler;
        this.usersReconciler = usersReconciler;
        this.policyReconciler = policyReconciler;
        this.operatorPolicyReconciler = operatorPolicyReconciler;
    }

    public void reconcile(final Reconciliation reconciliation) throws InterruptedException {
        final RabbitMQCustomResource resource = controller.get(reconciliation.getResourceName(), reconciliation.getNamespace());

        if (resource == null) {
            log.info("Not reconciling because RabbitMQCustomResource {} no longer exists", reconciliation.getResourceName());
            return;
        }

        if (shouldReconcile(resource)) {
            final RabbitMQCluster cluster = clusterFactory.fromCustomResource(resource);
            final int currentReplicaCount = determineCurrentReplicaCount(resource);

            reconcileKubernetesObjects(cluster);
            if (!resource.getSpec().isPreserveOrphanPVCs()) {
                deleteDanglingPvcs(resource, currentReplicaCount);
            }

            usersReconciler.reconcile(cluster);
            shovelReconciler.reconcile(cluster);
            policyReconciler.reconcile(cluster);
            operatorPolicyReconciler.reconcile(cluster);

            log.info("Reconciliation complete!");
        } else {
            log.info("Not reconciling cluster because it is locked");
        }
    }

    private void reconcileKubernetesObjects(final RabbitMQCluster cluster) throws InterruptedException {
        final Secret adminSecret = cluster.getAdminSecret();
        final Secret erlangCookieSecret = cluster.getErlangCookieSecret();
        secretsController.createOrUpdate(adminSecret);
        secretsController.createOrUpdate(erlangCookieSecret);

        servicesController.createOrUpdate(cluster.getMainService());
        servicesController.createOrUpdate(cluster.getDiscoveryService());

        if (cluster.getLoadBalancerService().isPresent()) {
            servicesController.createOrUpdate(cluster.getLoadBalancerService().get());
        } else {
            servicesController.delete(RabbitMQServices.getLoadBalancerServiceName(cluster.getName()), cluster.getNamespace());
        }

        statefulSetController.createOrUpdate(cluster.getStatefulSet());
        statefulSetController.waitForReady(cluster.getStatefulSet().getMetadata().getName(), cluster.getStatefulSet().getMetadata().getNamespace(), 5, TimeUnit.MINUTES);

        podDisruptionBudgetController.createOrUpdate(cluster.getPodDisruptionBudget());
    }

    /**
     * Delete any persistent volume claims that were orphaned by the process of scaling down a
     * cluster.
     *
     * This is not ideal.  In a perfect world we would set the owner reference of each persistent
     * volume claim to the pod that owns it, and then things would be cleaned up automatically as
     * the cluster scales down.  But we don't actually have those IDs, so instead we set the owner
     * to the stateful set.  They would eventually be cleaned up when the stateful set is deleted,
     * but to conserve resources we manually delete them now.
     *
     * @param resource the RabbitMQ cluster being scaled.
     * @param currentReplicaCount the number of replicas in the cluster prior to the scaling
     *                            operation.
     */
    private void deleteDanglingPvcs(final RabbitMQCustomResource resource, final int currentReplicaCount) {
        final String clusterName = resource.getMetadata().getName();
        if (resource.getSpec().getReplicas() < currentReplicaCount) {
            log.info("Deleting dangling PersistentVolumeClaims");
            for (int index = resource.getSpec().getReplicas(); index < currentReplicaCount; index++) {
                final String name = RABBITMQ_STORAGE_NAME + "-" + clusterName + "-" + index;
                persistentVolumeClaimController.delete(name, resource.getMetadata().getNamespace());
            }
        }
    }

    private int determineCurrentReplicaCount(final RabbitMQCustomResource resource) {
        final StatefulSet existing = statefulSetController.get(resource.getMetadata().getName(), resource.getMetadata().getNamespace());
        if (null != existing) {
            return existing.getSpec().getReplicas();
        }

        return 0;
    }

    private boolean shouldReconcile(final RabbitMQCustomResource resource) {
        final Map<String, String> labels = resource.getMetadata().getLabels();
        final boolean hasLabels = resource.getMetadata().getLabels() != null;

        return !hasLabels || !labels.containsKey(Labels.Indeed.LOCKED_BY);
    }
}
