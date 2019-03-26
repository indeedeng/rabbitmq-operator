package com.indeed.operators.rabbitmq.controller.crd;

import com.indeed.operators.rabbitmq.controller.AbstractResourceController;
import com.indeed.operators.rabbitmq.model.crd.user.DoneableRabbitMQUserCustomResource;
import com.indeed.operators.rabbitmq.model.crd.user.RabbitMQUserCustomResource;
import com.indeed.operators.rabbitmq.model.crd.user.RabbitMQUserCustomResourceList;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

import java.util.Map;

import static com.indeed.operators.rabbitmq.Constants.RABBITMQ_USER_CRD_NAME;

public class RabbitMQUserResourceController extends AbstractResourceController<RabbitMQUserCustomResource, RabbitMQUserCustomResourceList, DoneableRabbitMQUserCustomResource, Resource<RabbitMQUserCustomResource, DoneableRabbitMQUserCustomResource>> {

    public RabbitMQUserResourceController(
            final KubernetesClient client,
            final Map<String, String> labelsToWatch
    ) {
        super(client, labelsToWatch, RabbitMQUserCustomResource.class);
    }

    @Override
    protected MixedOperation<RabbitMQUserCustomResource, RabbitMQUserCustomResourceList, DoneableRabbitMQUserCustomResource, Resource<RabbitMQUserCustomResource, DoneableRabbitMQUserCustomResource>> operation() {
        final CustomResourceDefinition userCrd = getClient().customResourceDefinitions().withName(RABBITMQ_USER_CRD_NAME).get();

        if (userCrd == null) {
            throw new RuntimeException(String.format("CustomResourceDefinition %s has not been defined", RABBITMQ_USER_CRD_NAME));
        }

        return getClient().customResources(userCrd, RabbitMQUserCustomResource.class, RabbitMQUserCustomResourceList.class, DoneableRabbitMQUserCustomResource.class);
    }
}
