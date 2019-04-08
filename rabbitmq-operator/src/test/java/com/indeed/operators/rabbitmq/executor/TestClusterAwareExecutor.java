package com.indeed.operators.rabbitmq.executor;

import com.indeed.operators.rabbitmq.reconciliation.lock.NamedSemaphores;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestClusterAwareExecutor {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(2);
    private static final NamedSemaphores NAMED_LOCKS = new NamedSemaphores();

    // Ensures that we don't enqueue duplicate tasks.  "Enqueue" is the key term - we don't consider
    // tasks that are currently being executed.  So the behavior is that we try to enqueue ten
    // copies of the same task.  The first one we enqueue should start executing, leaving the queue
    // empty.  Another task should then get enqueued and subsequent ones should get discarded.
    @Test
    void deduplication() throws InterruptedException {
        final ClusterAwareExecutor executor = new ClusterAwareExecutor(EXECUTOR_SERVICE, NAMED_LOCKS);
        final AtomicLong executionCount = new AtomicLong();
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch completionLatch = new CountDownLatch(2);

        for (int index = 0; index < 10; index++) {
            executor.submit("cluster", "operation", () -> {
                try {
                    startLatch.await(10, TimeUnit.SECONDS);
                } catch (final InterruptedException ignored) {}
                executionCount.incrementAndGet();
                completionLatch.countDown();
            });
        }

        startLatch.countDown();
        completionLatch.await(10, TimeUnit.SECONDS);

        // Since we only wait for two tasks to complete, if there's a bug that breaks the
        // deduplication logic it's possible that this test may continue to pass.  However, it would
        // likely be unstable, because this assertion would fail if more than the two tasks managed
        // to complete by the time we got here.

        assertEquals(2, executionCount.get());
    }

    // Ensures that we don't execute multiple tasks for a given cluster at the same time.  It
    // enqueues two tasks for one cluster, then a single task for another cluster.  That single task
    // should not be blocked - we have two execution threads and only one of them should be occupied
    // with the first cluster.  The task task for the first cluster must wait.
    @Test
    void oneActiveTaskPerCluster() throws InterruptedException {
        final ClusterAwareExecutor executor = new ClusterAwareExecutor(EXECUTOR_SERVICE, NAMED_LOCKS);
        final CountDownLatch cluster1StartLatch = new CountDownLatch(1);
        final CountDownLatch cluster1CompletionLatch = new CountDownLatch(2);
        final CountDownLatch cluster2CompletionLatch = new CountDownLatch(1);

        for (int index = 0; index < 2; index++) {
            executor.submit("cluster1", String.format("operation-%d", index), () -> {
                try {
                    cluster1StartLatch.await(10, TimeUnit.SECONDS);
                } catch (final InterruptedException ignored) {}
                cluster1CompletionLatch.countDown();
            });
        }
        executor.submit("cluster2", "operation", cluster2CompletionLatch::countDown);

        // cluster1 has one task running and another waiting.  Since we have a second execution
        // thread, the cluster2 task should have already completed.
        cluster2CompletionLatch.await(10, TimeUnit.SECONDS);
        assertEquals(2, cluster1CompletionLatch.getCount());

        cluster1StartLatch.countDown();
        cluster1CompletionLatch.await(10, TimeUnit.SECONDS);
    }
}
