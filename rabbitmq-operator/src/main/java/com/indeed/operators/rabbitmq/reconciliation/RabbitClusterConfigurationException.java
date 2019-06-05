package com.indeed.operators.rabbitmq.reconciliation;

import com.google.common.base.Joiner;

import java.util.List;

public class RabbitClusterConfigurationException extends Exception {

    public RabbitClusterConfigurationException(final List<String> errors) {
        super(Joiner.on("; ").join(errors));
    }
}
