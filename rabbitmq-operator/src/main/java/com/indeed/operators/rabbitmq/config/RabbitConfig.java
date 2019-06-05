package com.indeed.operators.rabbitmq.config;

import com.google.common.collect.ImmutableList;
import com.indeed.operators.rabbitmq.api.RabbitMQPasswordConverter;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiProvider;
import com.indeed.operators.rabbitmq.controller.SecretsController;
import com.indeed.operators.rabbitmq.operations.AreQueuesEmptyOperation;
import com.indeed.operators.rabbitmq.reconciliation.rabbitmq.RabbitMQClusterFactory;
import com.indeed.operators.rabbitmq.reconciliation.validators.OperatorPolicyValidator;
import com.indeed.operators.rabbitmq.reconciliation.validators.PolicyValidator;
import com.indeed.operators.rabbitmq.reconciliation.validators.RabbitClusterValidator;
import com.indeed.operators.rabbitmq.reconciliation.validators.UserValidator;
import com.indeed.operators.rabbitmq.resources.RabbitMQContainers;
import com.indeed.operators.rabbitmq.resources.RabbitMQPods;
import com.indeed.operators.rabbitmq.resources.RabbitMQSecrets;
import com.indeed.operators.rabbitmq.resources.RabbitMQServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

@Configuration
public class RabbitConfig {

    @Bean
    public RabbitMQPods rabbitMQPods() {
        return new RabbitMQPods();
    }

    @Bean
    public RabbitMQContainers rabbitMQContainers() {
        return new RabbitMQContainers();
    }

    @Bean
    public RabbitMQSecrets rabbitSecrets(
            final Function<Integer, String> randomStringGenerator
    ) {
        return new RabbitMQSecrets(randomStringGenerator, (str) -> Base64.getEncoder().encodeToString(str.getBytes()));
    }

    @Bean
    public RabbitMQServices rabbitMQServices() {
        return new RabbitMQServices();
    }

    @Bean
    public RabbitManagementApiProvider managementApiCache(final SecretsController secretsController) {
        return new RabbitManagementApiProvider(secretsController);
    }

    @Bean
    public AreQueuesEmptyOperation queuesEmptyOperation(
            final RabbitManagementApiProvider managementApiCache
    ) {
        return new AreQueuesEmptyOperation(managementApiCache);
    }

    @Bean
    public RabbitMQPasswordConverter passwordConverter() throws NoSuchAlgorithmException {
        return new RabbitMQPasswordConverter(new Random(), MessageDigest.getInstance("SHA-256"), Base64.getEncoder(), Base64.getDecoder());
    }

    @Bean
    public List<RabbitClusterValidator> rabbitClusterValidators() {
        return ImmutableList.of(
                new OperatorPolicyValidator(),
                new PolicyValidator(),
                new UserValidator()
        );
    }

    @Bean
    public RabbitMQClusterFactory clusterFactory(
            final List<RabbitClusterValidator> rabbitClusterValidators,
            final RabbitMQContainers rabbitMQContainers,
            final RabbitMQPods rabbitMQPods,
            final RabbitMQSecrets rabbitMQSecrets,
            final RabbitMQServices rabbitMQServices,
            final SecretsController secretsController
    ) {
        return new RabbitMQClusterFactory(rabbitClusterValidators, rabbitMQContainers, rabbitMQPods, rabbitMQSecrets, rabbitMQServices, secretsController);
    }
}
