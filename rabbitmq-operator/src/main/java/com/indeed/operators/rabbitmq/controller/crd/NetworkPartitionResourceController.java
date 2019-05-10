package com.indeed.operators.rabbitmq.controller.crd;

import com.indeed.operators.rabbitmq.controller.AbstractResourceController;
import com.indeed.operators.rabbitmq.model.crd.partition.DoneableRabbitMQNetworkPartitionCustomResource;
import com.indeed.operators.rabbitmq.model.crd.partition.RabbitMQNetworkPartitionCustomResource;
import com.indeed.operators.rabbitmq.model.crd.partition.RabbitMQNetworkPartitionCustomResourceList;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.indeed.operators.rabbitmq.Constants.RABBITMQ_NETWORK_PARTITION_CRD_NAME;

public class NetworkPartitionResourceController extends AbstractResourceController<RabbitMQNetworkPartitionCustomResource, RabbitMQNetworkPartitionCustomResourceList, DoneableRabbitMQNetworkPartitionCustomResource, Resource<RabbitMQNetworkPartitionCustomResource, DoneableRabbitMQNetworkPartitionCustomResource>> {

    private static final Logger log = LoggerFactory.getLogger(NetworkPartitionResourceController.class);

    public NetworkPartitionResourceController(
            final KubernetesClient client,
            final Map<String, String> labelsToWatch
    ) {
        super(client, labelsToWatch, RabbitMQNetworkPartitionCustomResource.class);
    }

    @Override
    public boolean delete(final String name, final String namespace) {
        log.info("Deleting resource");
        operation().inNamespace(namespace).withName(name).cascading(true).delete();

        return true;
    }

    @Override
    protected MixedOperation<RabbitMQNetworkPartitionCustomResource, RabbitMQNetworkPartitionCustomResourceList, DoneableRabbitMQNetworkPartitionCustomResource, Resource<RabbitMQNetworkPartitionCustomResource, DoneableRabbitMQNetworkPartitionCustomResource>> operation() {
        final CustomResourceDefinition networkPartitionCrd = getClient().customResourceDefinitions().withName(RABBITMQ_NETWORK_PARTITION_CRD_NAME).get();

        if (networkPartitionCrd == null) {
            throw new RuntimeException(String.format("CustomResourceDefinition %s has not been defined", RABBITMQ_NETWORK_PARTITION_CRD_NAME));
        }

        return getClient().customResources(networkPartitionCrd, RabbitMQNetworkPartitionCustomResource.class, RabbitMQNetworkPartitionCustomResourceList.class, DoneableRabbitMQNetworkPartitionCustomResource.class);
    }
}
