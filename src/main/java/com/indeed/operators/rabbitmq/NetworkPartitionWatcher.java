package com.indeed.operators.rabbitmq;

import com.google.common.base.Preconditions;
import com.indeed.operators.rabbitmq.controller.crd.NetworkPartitionResourceController;
import com.indeed.operators.rabbitmq.model.crd.partition.RabbitMQNetworkPartitionCustomResource;
import com.indeed.operators.rabbitmq.reconciliation.ClusterReconciliationOrchestrator;
import com.indeed.operators.rabbitmq.reconciliation.NetworkPartitionReconciler;
import com.indeed.operators.rabbitmq.reconciliation.Reconciliation;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NetworkPartitionWatcher implements Watcher<RabbitMQNetworkPartitionCustomResource> {
    private static final Logger log = LoggerFactory.getLogger(NetworkPartitionWatcher.class);

    private final NetworkPartitionReconciler reconciler;
    private final NetworkPartitionResourceController controller;
    private final ClusterReconciliationOrchestrator orchestrator;

    public NetworkPartitionWatcher(
            final NetworkPartitionReconciler reconciler,
            final NetworkPartitionResourceController controller,
            final ClusterReconciliationOrchestrator orchestrator
    ) {
        this.reconciler = Preconditions.checkNotNull(reconciler);
        this.controller = controller;
        this.orchestrator = orchestrator;
    }

    @Override
    public void eventReceived(final Action action, final RabbitMQNetworkPartitionCustomResource resource) {
        try {
            switch (action) {
                case ADDED:
                    reconcile(resource);
                    break;
                case MODIFIED:
                case DELETED:
                    break;
                default:
                    log.error("Unsupported action: {}", action);
            }
        } catch (final Exception ex) {
            log.error("Exception during network partition processing - aborting this attempt", ex);
        }
    }

    private void reconcile(final RabbitMQNetworkPartitionCustomResource resource) {
        final Reconciliation rec = new Reconciliation(resource.getName(), resource.getSpec().getClusterName(), resource.getMetadata().getNamespace(), resource.getKind());

        orchestrator.queueReconciliation(rec, (reconciliation) -> {
            try {
                reconciler.reconcile(reconciliation);
            } catch (final InterruptedException e) {
                log.error("Interrupted during reconciliation", e);
            }
        });
    }

    public void reconcileAll(final String namespace) {
        log.info("Reconciling all NetworkPartition resources in namespace {}", namespace);
        final List<RabbitMQNetworkPartitionCustomResource> allResources = controller.getAll(namespace);

        allResources.forEach(this::reconcile);
    }

    @Override
    public void onClose(final KubernetesClientException cause) {
        log.info("closing watcher");
    }
}
