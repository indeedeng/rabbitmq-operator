package com.indeed.operators.rabbitmq.controller;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;

import java.util.List;

public interface ResourceController<T extends HasMetadata> {

    T createOrUpdate(T resource);

    T get(String name, String namespace);

    boolean delete(String name, String namespace);

    T patch(T resource);

    Watch watch(Watcher<T> watcher, String namespace);

    List<T> getAll(String namespace);
}
