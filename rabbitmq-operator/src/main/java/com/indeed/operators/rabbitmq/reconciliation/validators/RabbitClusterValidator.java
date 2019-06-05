package com.indeed.operators.rabbitmq.reconciliation.validators;

import com.indeed.operators.rabbitmq.model.crd.rabbitmq.ClusterSpec;

import java.util.List;

public interface RabbitClusterValidator {

    List<String> validate(ClusterSpec clusterSpec);
}
