package com.indeed.operators.rabbitmq.reconciliation.lock;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import javax.annotation.Nonnull;
import java.util.concurrent.Semaphore;

public class NamedSemaphores {

    private final LoadingCache<String, Semaphore> locks = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, Semaphore>() {
                @Override
                public Semaphore load(@Nonnull final String key) {
                    return new Semaphore(1);
                }
            });

    public Semaphore getSemaphore(final String lockName) {
        return locks.getUnchecked(lockName);
    }
}
