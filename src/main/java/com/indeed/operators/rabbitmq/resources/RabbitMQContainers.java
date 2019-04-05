package com.indeed.operators.rabbitmq.resources;

import com.indeed.operators.rabbitmq.model.crd.rabbitmq.RabbitMQComputeResources;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Lifecycle;
import io.fabric8.kubernetes.api.model.LifecycleBuilder;
import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.kubernetes.api.model.ProbeBuilder;

import static com.indeed.operators.rabbitmq.Constants.Ports.AMQP_PORT;
import static com.indeed.operators.rabbitmq.Constants.Ports.EPMD_PORT;
import static com.indeed.operators.rabbitmq.Constants.Ports.MANAGEMENT_PORT;
import static com.indeed.operators.rabbitmq.Constants.RABBITMQ_STORAGE_NAME;

public class RabbitMQContainers {
    public Container buildContainer(
            final String namespace,
            final String rabbitName,
            final String rabbitMQImage,
            final RabbitMQComputeResources resources,
            final double highWatermark
    ) {
        final String discoveryServiceName = RabbitMQServices.getDiscoveryServiceName(rabbitName);
        final String secretName = RabbitMQSecrets.getClusterSecretName(rabbitName);

        return new ContainerBuilder()
                .addNewPort().withName("epmd").withContainerPort(EPMD_PORT).withProtocol("TCP").endPort()
                .addNewPort().withName("amqp").withContainerPort(AMQP_PORT).withProtocol("TCP").endPort()
                .addNewPort().withName("management").withContainerPort(MANAGEMENT_PORT).withProtocol("TCP").endPort()
                .withImage(rabbitMQImage)
                .withName(rabbitName)
                .withNewResources()
                    .withRequests(resources.asResourceRequestQuantityMap())
                    .withLimits(resources.asResourceLimitQuantityMap())
                .endResources()
                .addNewEnv().withName("MY_POD_NAME").withNewValueFrom().withNewFieldRef().withFieldPath("metadata.name").endFieldRef().endValueFrom().endEnv()
                .addNewEnv().withName("RABBITMQ_VM_MEMORY_HIGH_WATERMARK").withValue(highWatermark > 0 ? String.valueOf(highWatermark) : "0Mib").endEnv()
                .addNewEnv().withName("RABBITMQ_ERLANG_COOKIE").withNewValueFrom().withNewSecretKeyRef("erlang-cookie", secretName, false).endValueFrom().endEnv()
                .addNewEnv().withName("RABBITMQ_DEFAULT_USER").withNewValueFrom().withNewSecretKeyRef("username", secretName, false).endValueFrom().endEnv()
                .addNewEnv().withName("RABBITMQ_DEFAULT_PASS").withNewValueFrom().withNewSecretKeyRef("password", secretName, false).endValueFrom().endEnv()
                .addNewEnv().withName("K8S_SERVICE_NAME").withValue(discoveryServiceName).endEnv()
                .addNewEnv().withName("RABBITMQ_USE_LONGNAME").withValue("true").endEnv()
                .addNewEnv().withName("RABBITMQ_NODENAME").withValue(String.format("rabbit@$(MY_POD_NAME).%s.%s.svc.cluster.local", discoveryServiceName, namespace)).endEnv()
                .addNewEnv().withName("K8S_HOSTNAME_SUFFIX").withValue(String.format(".%s.%s.svc.cluster.local", discoveryServiceName, namespace)).endEnv()
                .addNewVolumeMount().withName("config").withMountPath("/etc/rabbitmq").endVolumeMount()
                .addNewVolumeMount().withName(RABBITMQ_STORAGE_NAME).withMountPath("/var/lib/rabbitmq").endVolumeMount()
                .addNewVolumeMount().withName("probes").withMountPath("/probes").endVolumeMount()
                .addNewVolumeMount().withName("startup-scripts").withMountPath("/startup-scripts").endVolumeMount()
                .withReadinessProbe(buildReadinessProbe())
                .withLifecycle(buildLifeCycle())
                .build();
    }

    private Lifecycle buildLifeCycle() {
        return new LifecycleBuilder()
                .withNewPostStart()
                .withNewExec().withCommand("/startup-scripts/users.sh").endExec()
                .endPostStart()
                .build();
    }

    private Probe buildReadinessProbe() {
        return new ProbeBuilder()
                .withNewExec().withCommand("/probes/readiness.sh").endExec()
                .withInitialDelaySeconds(20)
                .withTimeoutSeconds(5)
                .withPeriodSeconds(5)
                .withFailureThreshold(3)
                .withSuccessThreshold(1)
                .build();
    }
}
