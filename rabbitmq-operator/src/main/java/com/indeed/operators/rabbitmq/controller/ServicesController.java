package com.indeed.operators.rabbitmq.controller;

import com.google.common.collect.Lists;
import io.fabric8.kubernetes.api.model.DoneableService;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.ServiceResource;

import java.util.List;
import java.util.Map;

public class ServicesController extends AbstractResourceController<Service, ServiceList, DoneableService, ServiceResource<Service, DoneableService>> {

    public ServicesController(
            final KubernetesClient client,
            final Map<String, String> labelsToWatch
    ) {
        super(client, labelsToWatch, Service.class);
    }

    @Override
    protected MixedOperation<Service, ServiceList, DoneableService, ServiceResource<Service, DoneableService>> operation() {
        return getClient().services();
    }

    // adapted from https://github.com/strimzi/strimzi-kafka-operator/blob/bfd5402733fdc50cb3ff3876bf28c455cb2fb845/operator-common/src/main/java/io/strimzi/operator/common/operator/resource/ServiceOperator.java#L54
    @Override
    public Service patch(final Service resource) {
        final Service current = get(resource.getMetadata().getName(), resource.getMetadata().getNamespace());

        if (shouldUpdateServicePorts(current, resource)) {
            final List<ServicePort> updatedPorts = getUpdatedServicePorts(current, resource);
            resource.getSpec().setPorts(updatedPorts);
        }

        return super.patch(resource);
    }

    private List<ServicePort> getUpdatedServicePorts(final Service current, final Service desired) {
        final List<ServicePort> finalPorts = Lists.newArrayList();
        for (final ServicePort desiredPort : desired.getSpec().getPorts())    {
            for (final ServicePort currentPort : current.getSpec().getPorts())    {
                if (desiredPort.getNodePort() == null && desiredPort.getName().equals(currentPort.getName()) && currentPort.getNodePort() != null) {
                    finalPorts.add(new ServicePortBuilder(desiredPort).withNodePort(currentPort.getNodePort()).build());
                }
            }
        }

        return finalPorts;
    }

    private boolean shouldUpdateServicePorts(final Service current, final Service desired) {
        return ("NodePort".equals(current.getSpec().getType()) && "NodePort".equals(desired.getSpec().getType())) ||
                ("LoadBalancer".equals(current.getSpec().getType()) && "LoadBalancer".equals(desired.getSpec().getType()));
    }
}
