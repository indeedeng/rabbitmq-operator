package com.indeed.operators.rabbitmq.reconciliation;

import com.indeed.operators.rabbitmq.controller.PodController;
import com.indeed.operators.rabbitmq.controller.StatefulSetController;
import com.indeed.operators.rabbitmq.controller.crd.NetworkPartitionResourceController;
import com.indeed.operators.rabbitmq.controller.crd.RabbitMQResourceController;
import com.indeed.operators.rabbitmq.model.Labels;
import com.indeed.operators.rabbitmq.model.ModelFieldLookups;
import com.indeed.operators.rabbitmq.model.crd.partition.RabbitMQNetworkPartitionCustomResource;
import com.indeed.operators.rabbitmq.model.crd.partition.RabbitMQNetworkPartitionCustomResourceBuilder;
import com.indeed.operators.rabbitmq.model.crd.partition.RabbitMQNetworkPartitionCustomResourceSpec;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.RabbitMQCustomResource;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.RabbitMQCustomResourceSpec;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQConnectionInfo;
import com.indeed.operators.rabbitmq.operations.AreQueuesEmptyOperation;
import com.indeed.operators.rabbitmq.resources.RabbitMQContainers;
import com.indeed.operators.rabbitmq.resources.RabbitMQPods;
import com.indeed.operators.rabbitmq.resources.RabbitMQServices;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.indeed.operators.rabbitmq.Constants.RABBITMQ_STORAGE_NAME;

public class NetworkPartitionReconciler {
    private static final Logger log = LoggerFactory.getLogger(NetworkPartitionReconciler.class);

    private final RabbitMQResourceController rabbitMQResourceController;
    private final NetworkPartitionResourceController partitionResourceController;
    private final AreQueuesEmptyOperation queuesEmptyOperation;
    private final RabbitMQPods rabbitMQPods;
    private final RabbitMQContainers rabbitMQContainers;
    private final StatefulSetController statefulSetController;
    private final PodController podController;
    private final String namespace;

    public NetworkPartitionReconciler(
            final RabbitMQResourceController rabbitMQResourceController,
            final NetworkPartitionResourceController partitionResourceController,
            final AreQueuesEmptyOperation queuesEmptyOperation,
            final RabbitMQPods rabbitMQPods,
            final RabbitMQContainers rabbitMQContainers,
            final StatefulSetController statefulSetController,
            final PodController podController,
            final String namespace
    ) {
        this.rabbitMQResourceController = rabbitMQResourceController;
        this.partitionResourceController = partitionResourceController;
        this.queuesEmptyOperation = queuesEmptyOperation;
        this.rabbitMQPods = rabbitMQPods;
        this.rabbitMQContainers = rabbitMQContainers;
        this.statefulSetController = statefulSetController;
        this.podController = podController;
        this.namespace = namespace;
    }

    public void reconcile(
            final Reconciliation reconciliation
    ) throws InterruptedException {
        final RabbitMQNetworkPartitionCustomResource networkPartition = partitionResourceController.get(reconciliation.getResourceName(), reconciliation.getNamespace());

        if (networkPartition == null) {
            log.info("Not reconciling because NetworkPartitionCustomResource {} no longer exists", reconciliation.getResourceName());
            return;
        }

        final RabbitMQNetworkPartitionCustomResourceSpec partitionSpec = networkPartition.getSpec();
        final String clusterName = partitionSpec.getClusterName();
        final String namespace = networkPartition.getMetadata().getNamespace();

        final RabbitMQCustomResource partitionedRabbit = rabbitMQResourceController.get(clusterName, namespace);
        final Map<String, String> labels = Optional.ofNullable(partitionedRabbit.getMetadata().getLabels()).orElse(new HashMap<>());

        log.info("Locking cluster with 'lockedBy:network-partition' label");
        labels.put(Labels.Indeed.LOCKED_BY, "network-partition");
        partitionedRabbit.getMetadata().setLabels(labels);
        rabbitMQResourceController.patch(partitionedRabbit);

        log.info("Deleting statefulset");
        statefulSetController.delete(clusterName, namespace);
        statefulSetController.waitForDeletion(clusterName, namespace, 5, TimeUnit.MINUTES);
        log.info("Successfully deleted statefulset");

        for (final Set<String> drainedPodNames : partitionSpec.getDrained()) {
            drainedPodNames.forEach(name -> podController.delete(name, namespace));
        }

        for (final Set<String> partitionedPodNames : partitionSpec.getPartitions()) {
            final RabbitMQNetworkPartitionCustomResource currentResource = partitionResourceController.get(networkPartition.getName(), networkPartition.getMetadata().getNamespace());
            final List<Pod> partitionedPods = generateDrainPods(partitionedPodNames, clusterName, partitionedRabbit.getSpec(), currentResource);

            processSideOfPartition(partitionedPods, currentResource);
        }

        log.info("Deleting the NetworkPartition resource...");
        partitionResourceController.delete(networkPartition.getName(), networkPartition.getMetadata().getNamespace());

        final RabbitMQCustomResource lockedRabbit = rabbitMQResourceController.get(clusterName, namespace);

        if (lockedRabbit.getMetadata().getLabels() != null) {
            log.info("Removing 'locked' label cluster");
            lockedRabbit.getMetadata().getLabels().remove(Labels.Indeed.LOCKED_BY);
            rabbitMQResourceController.patch(lockedRabbit);
        }

        log.info("Finished healing network partition");
    }

    private void processSideOfPartition(final List<Pod> pods, final RabbitMQNetworkPartitionCustomResource existingResource) throws InterruptedException {
        log.info("Creating drain pods with pods {}", pods.stream().map(ModelFieldLookups::getName).collect(Collectors.joining(",")));

        pods.forEach(pod -> {
            final String podName = ModelFieldLookups.getName(pod);

            final Pod maybeExistingPod = podController.get(podName, namespace);

            if (maybeExistingPod == null) {
                podController.createOrUpdate(pod);
            }
        });

        log.info("Waiting for pods to start...");
        waitForPodsToBecomeReady(pods);

        log.info("Created pods. Draining...");
        waitForPodsToDrain(existingResource.getSpec().getClusterName(), pods);

        final Set<String> drained = pods.stream().map(ModelFieldLookups::getName).collect(Collectors.toSet());

        final RabbitMQNetworkPartitionCustomResource resource = new RabbitMQNetworkPartitionCustomResourceBuilder(existingResource)
                .editSpec()
                .addToDrained(drained)
                .endSpec()
                .build();

        partitionResourceController.patch(resource);

        log.info("Drain complete, deleting pods...");
        pods.forEach(pod -> podController.delete(pod.getMetadata().getName(), pod.getMetadata().getNamespace()));

        waitForPodsToBeDeleted(pods);
    }

    private List<Pod> generateDrainPods(
            final Set<String> podNames,
            final String clusterName,
            final RabbitMQCustomResourceSpec rabbit,
            final RabbitMQNetworkPartitionCustomResource networkPartition
    ) {
        final Container container = rabbitMQContainers.buildContainer(
                namespace,
                clusterName,
                rabbit.getRabbitMQImage(),
                rabbit.getComputeResources(),
                0);

        return podNames.stream().map(podName ->
                new PodBuilder()
                        .withNewMetadata()
                        .withName(podName)
                        .withNamespace(namespace)
                        .addToLabels(Labels.Kubernetes.INSTANCE, clusterName)
                        .addToLabels(Labels.Kubernetes.MANAGED_BY, Labels.Values.RABBITMQ_OPERATOR)
                        .addToLabels(Labels.Kubernetes.PART_OF, Labels.Values.RABBITMQ)
                        .addToLabels(Labels.Indeed.LOCKED_BY, "network-partition")
                        .addToLabels(Labels.Indeed.getIndeedLabels(networkPartition))
                        .addToOwnerReferences(new OwnerReference(networkPartition.getApiVersion(), false, true, networkPartition.getKind(), networkPartition.getName(), networkPartition.getMetadata().getUid()))
                        .endMetadata()
                        .withSpec(rabbitMQPods.buildPodSpec(clusterName, rabbit.getInitContainerImage(), container))
                        .editSpec()
                        .withHostname(podName)
                        .withSubdomain(RabbitMQServices.getDiscoveryServiceName(clusterName))
                        .addNewVolume().withName(RABBITMQ_STORAGE_NAME).withNewPersistentVolumeClaim().withClaimName(RABBITMQ_STORAGE_NAME + "-" + podName).endPersistentVolumeClaim().endVolume()
                        .endSpec()
                        .build()
        ).collect(Collectors.toList());
    }

    private void waitForPodsToBecomeReady(final List<Pod> pods) {
        for (final Pod pod : pods) {
            final String podName = ModelFieldLookups.getName(pod);
            try {
                log.info("Waiting for pod {} to become ready", podName);
                // if we timeout here, this reconciliation will abort and we'll try again on the next reconciliation loop
                podController.waitForReady(podName, namespace, 1, TimeUnit.MINUTES);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void waitForPodsToBeDeleted(final List<Pod> pods) throws InterruptedException {
        for (final Pod pod : pods) {
            final String podName = ModelFieldLookups.getName(pod);
            log.info("Waiting for pod {} to be deleted", podName);
            // if we timeout here, this reconciliation will abort and we'll try again on the next reconciliation loop
            podController.waitForDeletion(podName, namespace, 1, TimeUnit.MINUTES);
        }
    }

    private void waitForPodsToDrain(
            final String clusterName,
            final List<Pod> pods
    ) throws InterruptedException {
        final RabbitMQConnectionInfo connectionInfo = new RabbitMQConnectionInfo(clusterName, namespace, RabbitMQServices.getDiscoveryServiceName(clusterName), ModelFieldLookups.getName(pods.get(0)));

        while(!queuesEmptyOperation.execute(connectionInfo)) {
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        }
    }
}
