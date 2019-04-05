package com.indeed.operators.rabbitmq.resources;

import com.google.common.base.Preconditions;
import com.indeed.operators.rabbitmq.model.Labels;
import com.indeed.operators.rabbitmq.model.ModelFieldLookups;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.RabbitMQCustomResource;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.UserSpec;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;

import java.util.Optional;
import java.util.function.Function;

public class RabbitMQSecrets {

    private final Function<Integer, String> randomStringGenerator;
    private final String rabbitUsername;

    public RabbitMQSecrets(
            final Function<Integer, String> randomStringGenerator,
            final String rabbitUsername
    ) {
        this.randomStringGenerator = Preconditions.checkNotNull(randomStringGenerator);
        this.rabbitUsername = Preconditions.checkNotNull(rabbitUsername);
    }

    public Secret createClusterSecret(final RabbitMQCustomResource rabbit) {
        final String clusterName = ModelFieldLookups.getName(rabbit);

        final String erlangCookie = randomStringGenerator.apply(50);
        final String password = randomStringGenerator.apply(30);

        return new SecretBuilder()
                .addToStringData("erlang-cookie", erlangCookie)
                .addToStringData("username", rabbitUsername)
                .addToStringData("password", password)
                .withNewMetadata()
                    .withName(getClusterSecretName(clusterName))
                    .withNamespace(rabbit.getMetadata().getNamespace())
                    .addToLabels(Labels.Kubernetes.INSTANCE, clusterName)
                    .addToLabels(Labels.Kubernetes.MANAGED_BY, Labels.Values.RABBITMQ_OPERATOR)
                    .addToLabels(Labels.Kubernetes.PART_OF, Labels.Values.RABBITMQ)
                    .addToLabels(Labels.Indeed.getIndeedLabels(rabbit))
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
                .build();
    }

    public Secret createUserSecret(
            final String username,
            final RabbitMQCustomResource rabbit
    ) {
        final String clusterName = rabbit.getName();
        final String password = randomStringGenerator.apply(30);

        return new SecretBuilder()
                .addToStringData("username", username)
                .addToStringData("password", password)
                .withNewMetadata()
                .withName(getUserSecretName(username, clusterName))
                .withNamespace(rabbit.getMetadata().getNamespace())
                .addToLabels(Labels.Kubernetes.INSTANCE, clusterName)
                .addToLabels(Labels.Kubernetes.MANAGED_BY, Labels.Values.RABBITMQ_OPERATOR)
                .addToLabels(Labels.Kubernetes.PART_OF, Labels.Values.RABBITMQ)
                .addToLabels(Labels.Indeed.getIndeedLabels(rabbit))
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
                .build();
    }

    public static String getClusterSecretName(final String rabbitName) {
        return String.format("%s-runtime-secret", rabbitName);
    }

    public static String getUserSecretName(final String username, final String rabbitName) {
        return String.format("%s-%s-user-secret", username, rabbitName);
    }
}
