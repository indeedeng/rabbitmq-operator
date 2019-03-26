package com.indeed.operators.rabbitmq.resources;

import com.indeed.operators.rabbitmq.model.Labels;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EmptyDirVolumeSource;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.WeightedPodAffinityTermBuilder;

public class RabbitMQPods {

    public PodSpec buildPodSpec(
            final String rabbitName,
            final String initContainerImage,
            final Container container
    ) {
        return new PodSpecBuilder()
                .withServiceAccountName("rabbitmq-user")
                .addNewInitContainer()
                .withName("copy-rabbitmq-config")
                .withImage(initContainerImage)
                .withCommand(new String[]{"sh", "-c", "cp /configmap/* /etc/rabbitmq"})
                .addNewVolumeMount().withName("config").withMountPath("/etc/rabbitmq").endVolumeMount()
                .addNewVolumeMount().withName("configmap").withMountPath("/configmap").endVolumeMount()
                .endInitContainer()
                .withContainers(container)
                .addNewVolume().withName("config").withEmptyDir(new EmptyDirVolumeSource()).endVolume()
                .addNewVolume().withName("configmap").withConfigMap(
                        new ConfigMapVolumeSourceBuilder()
                                .withName("rabbitmq-config")
                                .addNewItem("rabbitmq.conf", 0644, "rabbitmq.conf")
                                .addNewItem("enabled_plugins", 0644, "enabled_plugins")
                                .build()
                ).endVolume()
                .addNewVolume().withName("probes").withConfigMap(
                        new ConfigMapVolumeSourceBuilder()
                                .withName("rabbitmq-probes")
                                .addNewItem("readiness.sh", 0755, "readiness.sh")
                                .build()
                ).endVolume()
                .addNewVolume().withName("startup-scripts").withConfigMap(
                        new ConfigMapVolumeSourceBuilder()
                                .withName("rabbitmq-startup-scripts")
                                .addNewItem("users.sh", 0755, "users.sh")
                                .build()
                ).endVolume()
                .withNewAffinity().withNewPodAntiAffinity().withPreferredDuringSchedulingIgnoredDuringExecution(
                        new WeightedPodAffinityTermBuilder()
                                .withNewWeight(1)
                                .withNewPodAffinityTerm()
                                        .withNewLabelSelector()
                                                .addToMatchLabels(Labels.Kubernetes.INSTANCE, rabbitName)
                                        .endLabelSelector()
                                        .withTopologyKey("kubernetes.io/hostname")
                                .endPodAffinityTerm()
                                .build()
                ).endPodAntiAffinity().endAffinity()
                .build();
    }
}
