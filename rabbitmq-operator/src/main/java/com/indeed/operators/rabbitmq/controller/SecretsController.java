package com.indeed.operators.rabbitmq.controller;

import io.fabric8.kubernetes.api.model.DoneableSecret;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;

public class SecretsController extends AbstractResourceController<Secret, SecretList, DoneableSecret, Resource<Secret, DoneableSecret>> {

    private static final Logger log = LoggerFactory.getLogger(SecretsController.class);

    private final Function<String, String> decoder;

    public SecretsController(
            final KubernetesClient client,
            final Map<String, String> labelsToWatch,
            final Function<String, String> decoder
    ) {
        super(client, labelsToWatch, Secret.class);

        this.decoder = decoder;
    }

    @Override
    protected MixedOperation<Secret, SecretList, DoneableSecret, Resource<Secret, DoneableSecret>> operation() {
        return getClient().secrets();
    }

    @Override
    public Secret createOrUpdate(final Secret resource) {
        final Secret maybeExistingResource = get(resource.getMetadata().getName(), resource.getMetadata().getNamespace());

        if (maybeExistingResource == null) {
            log.info("Creating resource of type {} with name {}", getResourceType(), resource.getMetadata().getName());
            return operation().inNamespace(resource.getMetadata().getNamespace()).withName(resource.getMetadata().getName()).create(resource);
        } else {
            log.info("Patching metadata for resource type Secret with name {} - leaving Secret payload alone", resource.getMetadata().getName());
            final Secret patchedSecret = new SecretBuilder(resource)
                    .withStringData(maybeExistingResource.getStringData())
                    .withData(maybeExistingResource.getData())
                    .build();

            return patch(patchedSecret);
        }
    }

    public Secret createOrForceUpdate(final Secret resource) {
        return super.createOrUpdate(resource);
    }

    public String decodeSecretPayload(final String secretText) {
        return decoder.apply(secretText);
    }
}