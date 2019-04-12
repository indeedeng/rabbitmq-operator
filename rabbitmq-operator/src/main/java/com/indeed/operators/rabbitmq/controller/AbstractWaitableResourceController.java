package com.indeed.operators.rabbitmq.controller;

import io.fabric8.kubernetes.api.model.Doneable;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public abstract class AbstractWaitableResourceController<T extends HasMetadata, L extends KubernetesResourceList, D extends Doneable<T>, R extends Resource<T, D>> extends AbstractResourceController<T, L, D, R> implements WaitableResourceController {

    private static final Logger log = LoggerFactory.getLogger(AbstractWaitableResourceController.class);

    protected AbstractWaitableResourceController(
            final KubernetesClient client,
            final Map<String, String> labelsToWatch,
            final Class<T> resourceType
    ) {
        super(client, labelsToWatch, resourceType);
    }

    @Override
    public void waitForReady(final String name, final String namespace, final long time, final TimeUnit timeUnit) throws InterruptedException {
        log.info("Waiting {} {} for resource of type {} with name {} to be ready", time, timeUnit, getResourceType(), name);
        operation().inNamespace(namespace).withName(name).waitUntilReady(time, timeUnit);
    }

    @Override
    public void waitForDeletion(final String name, final String namespace, final long time, final TimeUnit timeUnit) throws InterruptedException {
        log.info("Waiting {} {} for resource of type {} with name {} to be deleted", time, timeUnit, getResourceType(), name);
        operation().inNamespace(namespace).withName(name).waitUntilCondition(Objects::isNull, time, timeUnit);
    }
}
