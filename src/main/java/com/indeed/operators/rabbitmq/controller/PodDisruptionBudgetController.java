package com.indeed.operators.rabbitmq.controller;

import io.fabric8.kubernetes.api.model.policy.DoneablePodDisruptionBudget;
import io.fabric8.kubernetes.api.model.policy.PodDisruptionBudget;
import io.fabric8.kubernetes.api.model.policy.PodDisruptionBudgetList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

import java.util.Map;

public class PodDisruptionBudgetController extends AbstractResourceController<PodDisruptionBudget, PodDisruptionBudgetList, DoneablePodDisruptionBudget, Resource<PodDisruptionBudget, DoneablePodDisruptionBudget>> {

    public PodDisruptionBudgetController(
            final KubernetesClient client,
            final Map<String, String> labelsToWatch
    ) {
        super(client, labelsToWatch, PodDisruptionBudget.class);
    }

    @Override
    protected MixedOperation<PodDisruptionBudget, PodDisruptionBudgetList, DoneablePodDisruptionBudget, Resource<PodDisruptionBudget, DoneablePodDisruptionBudget>> operation() {
        return getClient().policy().podDisruptionBudget();
    }
}
