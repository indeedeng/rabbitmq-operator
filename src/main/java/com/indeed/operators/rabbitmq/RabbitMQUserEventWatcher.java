package com.indeed.operators.rabbitmq;

import com.google.common.base.Preconditions;
import com.indeed.operators.rabbitmq.api.RabbitMQApiClient;
import com.indeed.operators.rabbitmq.controller.crd.RabbitMQResourceController;
import com.indeed.operators.rabbitmq.controller.crd.RabbitMQUserResourceController;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.RabbitMQCustomResource;
import com.indeed.operators.rabbitmq.model.crd.user.RabbitMQUserCustomResource;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQConnectionInfo;
import com.indeed.operators.rabbitmq.model.rabbitmq.User;
import com.indeed.operators.rabbitmq.reconciliation.ClusterReconciliationOrchestrator;
import com.indeed.operators.rabbitmq.reconciliation.RabbitMQUserReconciler;
import com.indeed.operators.rabbitmq.reconciliation.Reconciliation;
import com.indeed.operators.rabbitmq.resources.RabbitMQServices;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class RabbitMQUserEventWatcher implements Watcher<RabbitMQUserCustomResource> {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQUserEventWatcher.class);

    private final RabbitMQUserReconciler reconciler;
    private final RabbitMQUserResourceController userController;
    private final RabbitMQResourceController clusterController;
    private final ClusterReconciliationOrchestrator orchestrator;
    private final RabbitMQApiClient apiClient;

    public RabbitMQUserEventWatcher(
            final RabbitMQUserReconciler reconciler,
            final RabbitMQUserResourceController userController,
            final RabbitMQResourceController clusterController,
            final ClusterReconciliationOrchestrator orchestrator,
            final RabbitMQApiClient apiClient
    ) {
        this.reconciler = Preconditions.checkNotNull(reconciler);
        this.userController = userController;
        this.clusterController = clusterController;
        this.orchestrator = orchestrator;
        this.apiClient = apiClient;
    }

    @Override
    public void eventReceived(final Action action, final RabbitMQUserCustomResource resource) {
        final Reconciliation rec = new Reconciliation(resource.getName(), resource.getSpec().getClusterName(), resource.getMetadata().getNamespace(), resource.getKind());

        try {
            switch (action) {
                case ADDED:
                case MODIFIED:
                case DELETED:
                    reconcile(rec);
                    break;
                default:
                    log.error("Unsupported action: {}", action);
            }
        } catch (final Exception exception) {
            log.error("Exception during rabbit user processing - aborting this attempt", exception);
        }
    }

    private void reconcile(final Reconciliation rec) {
        if (rec.getResourceName().equals("rabbit") || rec.getResourceName().equals("monitoring")) {
            log.info("Attempted to reconcile the 'rabbit' or 'monitoring' user - skipping");
            return;
        }

        orchestrator.queueReconciliation(rec, (reconciliation) -> {
            try {
                reconciler.reconcile(reconciliation);
            } catch (final IOException e) {
                throw new RuntimeException("Reconciling users failed", e);
            }
        });
    }

    public void reconcileAll(final String namespace) throws IOException {
        final List<RabbitMQUserCustomResource> expectedUsers = userController.getAll(namespace);

        final List<RabbitMQCustomResource> clusters = clusterController.getAll(namespace);

        final List<Reconciliation> reconciliations = expectedUsers.stream()
                .map(user -> new Reconciliation(user.getName(), user.getSpec().getClusterName(), user.getMetadata().getNamespace(), RabbitMQUserCustomResource.class.getSimpleName()))
                .collect(Collectors.toList());

        for (final RabbitMQCustomResource cluster : clusters) {
            final RabbitMQConnectionInfo connectionInfo = new RabbitMQConnectionInfo(cluster.getName(), cluster.getMetadata().getNamespace(), RabbitMQServices.getDiscoveryServiceName(cluster.getName()));
            final List<User> users = apiClient.getUsers(connectionInfo);

            users.forEach(user -> reconciliations.add(new Reconciliation(user.getUsername(), cluster.getName(), cluster.getMetadata().getNamespace(), RabbitMQUserCustomResource.class.getSimpleName())));
        }

        reconciliations.forEach(this::reconcile);
    }

    @Override
    public void onClose(final KubernetesClientException cause) {
        log.info("Closing watcher", cause);
    }
}
