package com.indeed.operators.rabbitmq;

import com.google.common.base.Preconditions;
import com.indeed.operators.rabbitmq.controller.crd.RabbitMQUserResourceController;
import com.indeed.operators.rabbitmq.model.crd.user.RabbitMQUserCustomResource;
import com.indeed.operators.rabbitmq.reconciliation.ClusterReconciliationOrchestrator;
import com.indeed.operators.rabbitmq.reconciliation.RabbitMQUserReconciler;
import com.indeed.operators.rabbitmq.reconciliation.Reconciliation;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RabbitMQUserEventWatcher implements Watcher<RabbitMQUserCustomResource> {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQUserEventWatcher.class);

    private final RabbitMQUserReconciler reconciler;
    private final RabbitMQUserResourceController controller;
    private final ClusterReconciliationOrchestrator orchestrator;

    public RabbitMQUserEventWatcher(
            final RabbitMQUserReconciler reconciler,
            final RabbitMQUserResourceController controller,
            final ClusterReconciliationOrchestrator orchestrator
    ) {
        this.reconciler = Preconditions.checkNotNull(reconciler);
        this.controller = controller;
        this.orchestrator = orchestrator;
    }

    @Override
    public void eventReceived(final Action action, final RabbitMQUserCustomResource resource) {
        try {
            switch (action) {
                case ADDED:
                case MODIFIED:
                    reconcile(resource);
                    break;
                case DELETED:
                    log.info("Used {} deleted", resource.getName());
                    break;
                default:
                    log.error("Unsupported action: {}", action);
            }
        } catch (final Exception exception) {
            log.error("Exception during rabbit user processing - aborting this attempt", exception);
        }
    }

    private void reconcile(final RabbitMQUserCustomResource resource) {
        final Reconciliation rec = new Reconciliation(resource.getName(), resource.getSpec().getClusterName(), resource.getMetadata().getNamespace(), resource.getKind());

        orchestrator.queueReconciliation(rec, (reconciliation) -> {
            try {
                reconciler.reconcile(reconciliation);
            } catch (final IOException e) {
                throw new RuntimeException("Reconciling users failed", e);
            }
        });
    }

    // todo
    public void reconcileAll(final String namespace) {
        log.info("Reconciling all RabbitMQ user resources in namespace {}", namespace);
    }

    @Override
    public void onClose(final KubernetesClientException cause) {
        log.info("Closing watcher", cause);
    }
}
