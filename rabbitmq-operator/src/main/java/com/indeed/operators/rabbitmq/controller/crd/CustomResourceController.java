package com.indeed.operators.rabbitmq.controller.crd;

import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;

import java.util.List;

public interface CustomResourceController<T extends CustomResource> {

    T get(String name, String namespace);

    boolean delete(T resource);

    void patch(T resource);

    Watch watch(Watcher<T> watcher, String namespace);

    List<T> getAll(String namespace);
}
