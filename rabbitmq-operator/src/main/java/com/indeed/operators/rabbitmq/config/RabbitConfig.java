package com.indeed.operators.rabbitmq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indeed.operators.rabbitmq.api.RabbitMQApiClient;
import com.indeed.operators.rabbitmq.api.RabbitMQPasswordConverter;
import com.indeed.operators.rabbitmq.controller.SecretsController;
import com.indeed.operators.rabbitmq.operations.AreQueuesEmptyOperation;
import com.indeed.operators.rabbitmq.resources.RabbitMQContainers;
import com.indeed.operators.rabbitmq.resources.RabbitMQPods;
import com.indeed.operators.rabbitmq.resources.RabbitMQSecrets;
import com.indeed.operators.rabbitmq.resources.RabbitMQServices;
import okhttp3.OkHttpClient;
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
        return new RabbitMQSecrets(randomStringGenerator, DEFAULT_USERNAME);
    }

    @Bean
    public RabbitMQServices rabbitMQServices() {
        return new RabbitMQServices();
    }

    @Bean
    public RabbitMQApiClient rabbitMQApiClient(
            final OkHttpClient okHttpClient,
            final SecretsController secretsController
    ) {
        return new RabbitMQApiClient(okHttpClient, new ObjectMapper(), secretsController);
    }

    @Bean
    public AreQueuesEmptyOperation queuesEmptyOperation(
            final RabbitMQApiClient rabbitMQApiClient
    ) {
        return new AreQueuesEmptyOperation(rabbitMQApiClient);
    }

    @Bean
    public RabbitMQPasswordConverter passwordConverter() throws NoSuchAlgorithmException {
        return new RabbitMQPasswordConverter(new Random(), MessageDigest.getInstance("SHA-256"), Base64.getEncoder(), Base64.getDecoder());
    }
}
