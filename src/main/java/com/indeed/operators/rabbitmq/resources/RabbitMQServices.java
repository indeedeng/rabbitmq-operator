package com.indeed.operators.rabbitmq.resources;

import com.google.common.collect.ImmutableList;
import com.indeed.operators.rabbitmq.model.Labels;
import com.indeed.operators.rabbitmq.model.ModelFieldLookups;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.RabbitMQCustomResource;
import io.fabric8.kubernetes.api.model.*;

import java.util.List;

import static com.indeed.operators.rabbitmq.Constants.Ports.AMQP;
import static com.indeed.operators.rabbitmq.Constants.Ports.AMQP_PORT;
import static com.indeed.operators.rabbitmq.Constants.Ports.EPMD;
import static com.indeed.operators.rabbitmq.Constants.Ports.EPMD_PORT;
import static com.indeed.operators.rabbitmq.Constants.Ports.MANAGEMENT;
import static com.indeed.operators.rabbitmq.Constants.Ports.MANAGEMENT_PORT;

public class RabbitMQServices {
    private static final String TCP_PROTOCOL = "TCP";
    private static final String CLUSTER_IP = "ClusterIP";
    private static final String LOADBALANCER = "LoadBalancer";

    public Service buildService(final String namespace, final RabbitMQCustomResource rabbit) {
        final String clusterName = ModelFieldLookups.getName(rabbit);

        return new ServiceBuilder()
                .withNewMetadata()
                .withName(getPublicServiceName(clusterName))
                .withNamespace(namespace)
                .addToLabels(Labels.Indeed.getIndeedLabels(rabbit))
                .addToLabels(Labels.Kubernetes.INSTANCE, clusterName)
                .addToLabels(Labels.Kubernetes.MANAGED_BY, Labels.Values.RABBITMQ_OPERATOR)
                .addToLabels(Labels.Kubernetes.PART_OF, Labels.Values.RABBITMQ)
                .withAnnotations(rabbit.getMetadata().getAnnotations())
                .withOwnerReferences(
                        new OwnerReference(
                                rabbit.getApiVersion(),
                                true,
                                true,
                                rabbit.getKind(),
                                rabbit.getName(),
                                rabbit.getMetadata().getUid()
                        )
                )
                .endMetadata()
                .withNewSpec()
                .withPorts(constructServicePorts())
                .withType(CLUSTER_IP)
                .addToSelector(Labels.Kubernetes.INSTANCE, clusterName)
                .endSpec()
                .build();
    }

    public Service buildDiscoveryService(final String namespace, final RabbitMQCustomResource rabbit) {
        final String clusterName = ModelFieldLookups.getName(rabbit);

        return new ServiceBuilder()
                .withNewMetadata()
                .withName(getDiscoveryServiceName(clusterName))
                .withNamespace(namespace)
                .addToLabels(Labels.Indeed.getIndeedLabels(rabbit))
                .addToLabels(Labels.Kubernetes.INSTANCE, clusterName)
                .addToLabels(Labels.Kubernetes.MANAGED_BY, Labels.Values.RABBITMQ_OPERATOR)
                .addToLabels(Labels.Kubernetes.PART_OF, Labels.Values.RABBITMQ)
                .withAnnotations(rabbit.getMetadata().getAnnotations())
                .withOwnerReferences(
                        new OwnerReference(
                                rabbit.getApiVersion(),
                                true,
                                true,
                                rabbit.getKind(),
                                rabbit.getName(),
                                rabbit.getMetadata().getUid()
                        )
                )
                .endMetadata()
                .withNewSpec()
                .withPorts(constructServicePorts())
                .withType(CLUSTER_IP)
                .withClusterIP("None")
                .addToSelector(Labels.Kubernetes.INSTANCE, clusterName)
                .withPublishNotReadyAddresses(true)
                .endSpec()
                .build();
    }

    public Service buildLoadBalancerService(final String namespace, final RabbitMQCustomResource rabbit) {
        final String clusterName = ModelFieldLookups.getName(rabbit);

        return new ServiceBuilder()
                .withNewMetadata()
                .withName(getLoadBalancerServiceName(clusterName))
                .withNamespace(namespace)
                .addToLabels(Labels.Indeed.getIndeedLabels(rabbit))
                .addToLabels(Labels.Kubernetes.INSTANCE, clusterName)
                .addToLabels(Labels.Kubernetes.MANAGED_BY, Labels.Values.RABBITMQ_OPERATOR)
                .addToLabels(Labels.Kubernetes.PART_OF, Labels.Values.RABBITMQ)
                .withAnnotations(rabbit.getMetadata().getAnnotations())
                .withOwnerReferences(
                        new OwnerReference(
                                rabbit.getApiVersion(),
                                true,
                                true,
                                rabbit.getKind(),
                                rabbit.getName(),
                                rabbit.getMetadata().getUid()
                        )
                )
                .endMetadata()
                .withNewSpec()
                .withPorts(constructServicePorts())
                .withType(LOADBALANCER)
                .addToSelector(Labels.Kubernetes.INSTANCE, clusterName)
                .endSpec()
                .build();
    }

    private List<ServicePort> constructServicePorts() {
        return ImmutableList.of(
                new ServicePortBuilder().withName(EPMD).withProtocol(TCP_PROTOCOL).withPort(EPMD_PORT).withNewTargetPort(EPMD_PORT).build(),
                new ServicePortBuilder().withName(AMQP).withProtocol(TCP_PROTOCOL).withPort(AMQP_PORT).withNewTargetPort(AMQP_PORT).build(),
                new ServicePortBuilder().withName(MANAGEMENT).withProtocol(TCP_PROTOCOL).withPort(MANAGEMENT_PORT).withNewTargetPort(MANAGEMENT_PORT).build()
        );
    }

    public static String getDiscoveryServiceName(final String rabbitName) {
        return String.format("%s-svc-discovery", rabbitName);
    }

    public static String getPublicServiceName(final String rabbitName) {
        return String.format("%s-svc", rabbitName);
    }

    public static String getLoadBalancerServiceName(final String rabbitName) {
        return String.format("%s-lb", getPublicServiceName(rabbitName));
    }
}
