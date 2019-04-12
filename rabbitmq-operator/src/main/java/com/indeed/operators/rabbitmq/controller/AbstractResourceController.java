package com.indeed.operators.rabbitmq.controller;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Doneable;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.Operation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public abstract class AbstractResourceController<T extends HasMetadata, L extends KubernetesResourceList, D extends Doneable<T>, R extends Resource<T, D>> implements ResourceController<T> {
    private static final Logger log = LoggerFactory.getLogger(AbstractResourceController.class);

    private final KubernetesClient client;
    private final Map<String, String> labelsToWatch;
    private final String resourceType;

    protected AbstractResourceController(
            final KubernetesClient client,
            final Map<String, String> labelsToWatch,
            final Class<T> resourceType
    ) {
        this.client = client;
        this.labelsToWatch = ImmutableMap.copyOf(labelsToWatch);
        this.resourceType = resourceType.getSimpleName();
    }

    protected abstract Operation<T, L, D, R> operation();

    @Override
    public T createOrUpdate(final T resource) {
        final T maybeExistingResource = get(resource.getMetadata().getName(), resource.getMetadata().getNamespace());

        if (maybeExistingResource == null) {
            log.info("Creating resource of type {} with name {}", resourceType, resource.getMetadata().getName());
            return operation().inNamespace(resource.getMetadata().getNamespace()).withName(resource.getMetadata().getName()).create(resource);
        } else {
            return patch(resource);
        }
    }

    @Override
    public T get(final String name, final String namespace) {
        return operation().inNamespace(namespace).withName(name).get();
    }

    @Override
    public boolean delete(final String name, final String namespace) {
        log.info("Deleting resource of type {} with name {}", resourceType, name);
        return operation().inNamespace(namespace).withName(name).delete();
    }

    @Override
    public T patch(final T resource) {
        log.info("Patching resource of type {} with name {}", resourceType, resource.getMetadata().getName());
        return operation().inNamespace(resource.getMetadata().getNamespace()).withName(resource.getMetadata().getName()).patch(resource);
    }

    @Override
    public Watch watch(final Watcher<T> watcher, final String namespace) {
        log.info("Watching resources of type {} in namespace {}", resourceType, namespace);
        return operation().inNamespace(namespace).withLabels(labelsToWatch).watch(watcher);
    }

    @Override
    public List<T> getAll(final String namespace) {
        log.info("Getting all resources of type {} in namespace {}", resourceType, namespace);
        return operation().inNamespace(namespace).withLabels(labelsToWatch).list().getItems();
    }

    protected KubernetesClient getClient() {
        return client;
    }

    protected final String getResourceType() {
        return resourceType;
    }
}
