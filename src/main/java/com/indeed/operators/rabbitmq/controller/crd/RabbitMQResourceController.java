package com.indeed.operators.rabbitmq.controller.crd;

import com.indeed.operators.rabbitmq.controller.AbstractResourceController;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.DoneableRabbitMQCustomResource;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.RabbitMQCustomResource;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.RabbitMQCustomResourceList;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

import java.util.Map;

import static com.indeed.operators.rabbitmq.Constants.RABBITMQ_CRD_NAME;

public class RabbitMQResourceController extends AbstractResourceController<RabbitMQCustomResource, RabbitMQCustomResourceList, DoneableRabbitMQCustomResource, Resource<RabbitMQCustomResource, DoneableRabbitMQCustomResource>> {

    public RabbitMQResourceController(
            final KubernetesClient client,
            final Map<String, String> labelsToWatch
    ) {
        super(client, labelsToWatch, RabbitMQCustomResource.class);
    }

    @Override
    protected MixedOperation<RabbitMQCustomResource, RabbitMQCustomResourceList, DoneableRabbitMQCustomResource, Resource<RabbitMQCustomResource, DoneableRabbitMQCustomResource>> operation() {
        final CustomResourceDefinition rabbitCrd = getClient().customResourceDefinitions().withName(RABBITMQ_CRD_NAME).get();

        if (rabbitCrd == null) {
            throw new RuntimeException(String.format("CustomResourceDefinition %s has not been defined", RABBITMQ_CRD_NAME));
        }

        return getClient().customResources(rabbitCrd, RabbitMQCustomResource.class, RabbitMQCustomResourceList.class, DoneableRabbitMQCustomResource.class);
    }
}