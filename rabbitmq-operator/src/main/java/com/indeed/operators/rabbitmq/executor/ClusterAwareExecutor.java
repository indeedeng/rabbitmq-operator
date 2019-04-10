package com.indeed.operators.rabbitmq.executor;

import com.google.common.base.Objects;
import com.indeed.operators.rabbitmq.reconciliation.lock.NamedSemaphores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

public class ClusterAwareExecutor {
    private static final Logger log = LoggerFactory.getLogger(ClusterAwareExecutor.class);

    private final Map<ClusterOperation, Runnable> taskQueue = new LinkedHashMap<>();

    private final ExecutorService executorService;
    private final NamedSemaphores namedSemaphores;

    public ClusterAwareExecutor(@Nonnull final ExecutorService executorService, @Nonnull final NamedSemaphores namedSemaphores) {
        this.executorService = executorService;
        this.namedSemaphores = namedSemaphores;
    }

    public synchronized void submit(
            @Nonnull final String clusterName,
            @Nonnull final String operation,
            @Nonnull final Runnable runnable) {
        // Only call dispatch if this task wasn't already present - if it was, then dispatch would
        // be a no-op anyway.
        if (null == taskQueue.putIfAbsent(new ClusterOperation(clusterName, operation), runnable)) {
            dispatch();
        }
    }

    private synchronized void dispatch() {
        for (final Map.Entry<ClusterOperation, Runnable> task : taskQueue.entrySet()) {
            final ClusterOperation clusterOperation = task.getKey();
            final String clusterName = clusterOperation.getClusterName();
            final String operation = clusterOperation.getOperation();

            // We want to prevent multiple tasks for the same cluster from attempting to run
            // simultaneously - if they did they could easily conflict with one another.  To rule
            // this out we use a semaphore per cluster as a lock.
            final Semaphore semaphore = namedSemaphores.getSemaphore(clusterName);
            log.debug("Attempting to acquire semaphore for cluster {}", clusterName);
            if (semaphore.tryAcquire()) {
                log.info("Acquired semaphore for cluster {}, running operation {}", clusterName, operation);

                executorService.submit(() -> {
                    try {
                        task.getValue().run();
                    } finally {
                        log.info("Finished operation {} for cluster {}, releasing semaphore", operation, clusterName);

                        // Important - release the semaphore before dispatching the next task,
                        // otherwise we may inadvertently make a task for this cluster wait longer
                        // than is strictly necessary.
                        semaphore.release();

                        // Check for additional work, unless we were interrupted (which usually
                        // indicates that we're being shut down).
                        if (!Thread.currentThread().isInterrupted()) {
                            dispatch();
                        }
                    }
                });
                taskQueue.remove(clusterOperation);
                return;
            } else {
                log.debug("Semaphore for cluster {} was unacquirable", clusterName);
            }
        }
    }

    private static class ClusterOperation {

        private final String clusterName;
        private final String operation;

        ClusterOperation(@Nonnull final String clusterName, @Nonnull final String operation) {
            this.clusterName = clusterName;
            this.operation = operation;
        }

        String getClusterName() {
            return clusterName;
        }

        String getOperation() {
            return operation;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final ClusterOperation that = (ClusterOperation) o;
            return Objects.equal(clusterName, that.clusterName) &&
                    Objects.equal(operation, that.operation);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(clusterName, operation);
        }
    }
}
