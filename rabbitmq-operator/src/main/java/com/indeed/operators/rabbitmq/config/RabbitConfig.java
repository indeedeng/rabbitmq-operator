package com.indeed.operators.rabbitmq.config;

import com.indeed.operators.rabbitmq.api.RabbitMQPasswordConverter;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiProvider;
import com.indeed.operators.rabbitmq.controller.SecretsController;
import com.indeed.operators.rabbitmq.operations.AreQueuesEmptyOperation;
import com.indeed.operators.rabbitmq.resources.RabbitMQContainers;
import com.indeed.operators.rabbitmq.resources.RabbitMQPods;
import com.indeed.operators.rabbitmq.resources.RabbitMQSecrets;
import com.indeed.operators.rabbitmq.resources.RabbitMQServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.function.Function;

import static com.indeed.operators.rabbitmq.Constants.DEFAULT_USERNAME;

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
        return new RabbitMQSecrets(randomStringGenerator);
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
}
