package com.indeed.operators.rabbitmq.reconciliation;

import com.indeed.operators.rabbitmq.executor.ClusterAwareExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.function.Consumer;

public class ClusterReconciliationOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(ClusterReconciliationOrchestrator.class);

    private final ClusterAwareExecutor executor;

    public ClusterReconciliationOrchestrator(
            final ClusterAwareExecutor executor
    ) {
        this.executor = executor;
    }

    public void queueReconciliation(final Reconciliation reconciliation, final Consumer<Reconciliation> runner) {
        log.info("Queueing reconciliation {}", reconciliation);
        executor.submit(reconciliation.getClusterName(), reconciliation.getType(), () -> {
            MDC.put("cluster", reconciliation.getClusterName());

            try {
                runner.accept(reconciliation);
            } catch (final Throwable t) {
                log.error("There was an error during reconciliation that the reconciler didn't handle", t);
            } finally {
                MDC.remove("cluster");
            }
        });

        log.info("Reconciliation {} successfully queued", reconciliation);
    }
}
