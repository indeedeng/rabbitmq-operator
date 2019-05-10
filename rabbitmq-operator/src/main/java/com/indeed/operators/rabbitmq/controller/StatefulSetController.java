package com.indeed.operators.rabbitmq.controller;

import io.fabric8.kubernetes.api.model.apps.DoneableStatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class StatefulSetController extends AbstractWaitableResourceController<StatefulSet, StatefulSetList, DoneableStatefulSet, RollableScalableResource<StatefulSet, DoneableStatefulSet>> {

    private static final Logger log = LoggerFactory.getLogger(StatefulSetController.class);

    private final PodController podController;

    public StatefulSetController(
            final KubernetesClient client,
            final Map<String, String> labelsToWatch,
            final PodController podController
    ) {
        super(client, labelsToWatch, StatefulSet.class);

        this.podController = podController;
    }

    @Override
    protected MixedOperation<StatefulSet, StatefulSetList, DoneableStatefulSet, RollableScalableResource<StatefulSet, DoneableStatefulSet>> operation() {
        return getClient().apps().statefulSets();
    }

    @Override
    public StatefulSet patch(final StatefulSet resource) {
        log.info("Patching resource of type {} with name {}", getResourceType(), resource.getMetadata().getName());
        return operation().inNamespace(resource.getMetadata().getNamespace()).withName(resource.getMetadata().getName()).cascading(false).patch(resource);
    }

    @Override
    public void waitForReady(final String name, final String namespace, final long time, final TimeUnit timeUnit) throws InterruptedException {
        log.info("Waiting {} {} for StatefulSet with name {} to be ready", time, timeUnit, name);
        operation().inNamespace(namespace).withName(name).waitUntilReady(time, timeUnit);
        operation().inNamespace(namespace).withName(name).waitUntilCondition(ss -> ss.getSpec().getReplicas().equals(ss.getStatus().getCurrentReplicas()), time, timeUnit);

        log.info("StatefulSet with name {} reported as ready - checking pod statuses", name);

        final StatefulSet statefulSet = operation().inNamespace(namespace).withName(name).get();

        for (int i = statefulSet.getSpec().getReplicas() - 1; i >= 0; i--) {
            final String podName = String.format("%s-%s", statefulSet.getMetadata().getName(), i);

            podController.waitForReady(podName, statefulSet.getMetadata().getNamespace(), time, timeUnit);
        }
    }
}
