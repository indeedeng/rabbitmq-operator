package com.indeed.operators.rabbitmq.operations;

import com.indeed.operators.rabbitmq.api.RabbitMQApiClient;
import com.indeed.operators.rabbitmq.model.rabbitmq.api.QueueState;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class AreQueuesEmptyOperation {
    private static final Logger log = LoggerFactory.getLogger(AreQueuesEmptyOperation.class);

    private final RabbitMQApiClient rabbitMQApiClient;

    public AreQueuesEmptyOperation(
            final RabbitMQApiClient rabbitMQApiClient
    ) {
        this.rabbitMQApiClient = rabbitMQApiClient;
    }

    public boolean execute(final RabbitMQConnectionInfo connectionInfo) {
        final List<QueueState> queueStates;
        try {
            queueStates = rabbitMQApiClient.getQueues(connectionInfo);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        final List<String> nonEmptyQueues = queueStates.stream()
                .filter(queue -> queue.getMessageCount() > 0)
                .map(QueueState::getName)
                .collect(Collectors.toList());

        log.info("Non-empty queues: {}", nonEmptyQueues);

        return nonEmptyQueues.isEmpty();
    }
}
