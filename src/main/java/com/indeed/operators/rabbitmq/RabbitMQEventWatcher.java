package com.indeed.operators.rabbitmq;

import com.google.common.base.Preconditions;
import com.indeed.operators.rabbitmq.controller.crd.RabbitMQResourceController;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.RabbitMQCustomResource;
import com.indeed.operators.rabbitmq.reconciliation.ClusterReconciliationOrchestrator;
import com.indeed.operators.rabbitmq.reconciliation.RabbitMQClusterReconciler;
import com.indeed.operators.rabbitmq.reconciliation.Reconciliation;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class RabbitMQEventWatcher implements Watcher<RabbitMQCustomResource> {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQEventWatcher.class);

    private final RabbitMQClusterReconciler reconciler;
    private final RabbitMQResourceController controller;
    private final ClusterReconciliationOrchestrator orchestrator;

    public RabbitMQEventWatcher(
            final RabbitMQClusterReconciler reconciler,
            final RabbitMQResourceController controller,
            final ClusterReconciliationOrchestrator orchestrator
    ) {
        this.reconciler = Preconditions.checkNotNull(reconciler);
        this.controller = controller;
        this.orchestrator = orchestrator;
    }

    @Override
    public void eventReceived(final Action action, final RabbitMQCustomResource resource) {
        try {
            switch (action) {
                case ADDED:
                case MODIFIED:
                    reconcile(resource);
                    break;
                case DELETED:
                    log.info("rabbit {} deleted", resource.getName());
                    break;
                default:
                    log.error("Unsupported action: {}", action);
            }
        } catch (final Exception exception) {
            log.error("Exception during rabbit cluster processing - aborting this attempt", exception);
        }
    }

    private void reconcile(final RabbitMQCustomResource resource) {
        final Reconciliation rec = new Reconciliation(resource.getName(), resource.getName(), resource.getMetadata().getNamespace(), resource.getKind());

        orchestrator.queueReconciliation(rec, (reconciliation) -> {
            try {
                reconciler.reconcile(reconciliation);
            } catch (final InterruptedException e) {
                log.error("Interrupted during reconciliation", e);
            }
        });
    }

    public void reconcileAll(final String namespace) {
        log.info("Reconciling all RabbitMQ cluster resources in namespace {}", namespace);
        final List<RabbitMQCustomResource> allResources = controller.getAll(namespace);

        allResources.forEach(this::reconcile);
    }

    @Override
    public void onClose(final KubernetesClientException cause) {
        log.info("Closing watcher", cause);
    }
}
