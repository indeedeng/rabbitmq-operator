package com.indeed.operators.rabbitmq.config;

import com.google.common.collect.ImmutableMap;
import com.indeed.operators.rabbitmq.controller.PersistentVolumeClaimController;
import com.indeed.operators.rabbitmq.controller.PodController;
import com.indeed.operators.rabbitmq.controller.PodDisruptionBudgetController;
import com.indeed.operators.rabbitmq.controller.SecretsController;
import com.indeed.operators.rabbitmq.controller.ServicesController;
import com.indeed.operators.rabbitmq.controller.StatefulSetController;
import com.indeed.operators.rabbitmq.controller.crd.NetworkPartitionResourceController;
import com.indeed.operators.rabbitmq.controller.crd.RabbitMQResourceController;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Configuration
public class ControllerConfig {

    private static final String WATCH_LABELS_ENV_VAR = "WATCH_LABELS";

    @Bean
    @Qualifier("LABELS_TO_WATCH")
    public Map<String, String> labelsToWatch() {
        final ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();

        if (System.getenv().containsKey(WATCH_LABELS_ENV_VAR)) {
            final String watchLabels = System.getenv().get(WATCH_LABELS_ENV_VAR);
            for (final String label : watchLabels.split(",")) {
                if (label.contains("=")) {
                    final String[] keyValue = label.split("=");
                    mapBuilder.put(keyValue[0], keyValue[1]);
                }
            }
        }

        return mapBuilder.build();
    }

    @Bean
    public RabbitMQResourceController rabbitResourceController(
            final KubernetesClient client,
            @Qualifier("LABELS_TO_WATCH") final Map<String, String> labelsToWatch
    ) {
        return new RabbitMQResourceController(client, labelsToWatch);
    }

    @Bean
    public NetworkPartitionResourceController networkPartitionResourceController(
            final KubernetesClient client,
            @Qualifier("LABELS_TO_WATCH") final Map<String, String> labelsToWatch
    ) {
        return new NetworkPartitionResourceController(client, labelsToWatch);
    }

    @Bean
    public SecretsController secretsController(
            final KubernetesClient client,
            @Qualifier("LABELS_TO_WATCH") final Map<String, String> labelsToWatch
    ) {
        return new SecretsController(client, labelsToWatch, (val) -> new String(Base64.getDecoder().decode(val), StandardCharsets.UTF_8));
    }

    @Bean
    public ServicesController servicesController(
            final KubernetesClient client,
            @Qualifier("LABELS_TO_WATCH") final Map<String, String> labelsToWatch
    ) {
        return new ServicesController(client, labelsToWatch);
    }

    @Bean
    public StatefulSetController statefulSetController(
            final KubernetesClient client,
            @Qualifier("LABELS_TO_WATCH") final Map<String, String> labelsToWatch,
            final PodController podController
    ) {
        return new StatefulSetController(client, labelsToWatch, podController);
    }

    @Bean
    public PodController podController(
            final KubernetesClient client,
            @Qualifier("LABELS_TO_WATCH") final Map<String, String> labelsToWatch
    ) {
        return new PodController(client, labelsToWatch);
    }

    @Bean
    public PodDisruptionBudgetController podDisruptionBudgetController(
            final KubernetesClient client,
            @Qualifier("LABELS_TO_WATCH") final Map<String, String> labelsToWatch
    ) {
        return new PodDisruptionBudgetController(client, labelsToWatch);
    }

    @Bean
    public PersistentVolumeClaimController persistentVolumeClaimController(
            final KubernetesClient client,
            @Qualifier("LABELS_TO_WATCH") final Map<String, String> labelsToWatch
    ) {
        return new PersistentVolumeClaimController(client, labelsToWatch);
    }
}
