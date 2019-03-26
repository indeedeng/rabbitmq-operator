package com.indeed.operators.rabbitmq.reconciliation;

public interface Reconciler {

    void reconcile(final Reconciliation rec) throws Exception;
}
