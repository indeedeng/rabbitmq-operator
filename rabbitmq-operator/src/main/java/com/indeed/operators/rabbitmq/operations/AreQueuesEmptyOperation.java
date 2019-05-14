package com.indeed.operators.rabbitmq.operations;

import com.indeed.operators.rabbitmq.api.RabbitApiResponseConsumer;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiProvider;
import com.indeed.rabbitmq.admin.RabbitManagementApi;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQConnectionInfo;
import com.indeed.rabbitmq.admin.pojo.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class AreQueuesEmptyOperation {
    private static final Logger log = LoggerFactory.getLogger(AreQueuesEmptyOperation.class);

    private final RabbitManagementApiProvider rabbitManagementApiProvider;

    public AreQueuesEmptyOperation(
            final RabbitManagementApiProvider rabbitManagementApiProvider
    ) {
        this.rabbitManagementApiProvider = rabbitManagementApiProvider;
    }

    public boolean execute(final RabbitMQConnectionInfo connectionInfo) {
        final RabbitManagementApi api = rabbitManagementApiProvider.getApi(connectionInfo);
        final List<Queue> queueStates;
        try {
            queueStates = RabbitApiResponseConsumer.consumeResponse(api.listQueues().execute());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        final List<String> nonEmptyQueues = queueStates.stream()
                .filter(queue -> queue.getMessages() > 0)
                .map(Queue::getName)
                .collect(Collectors.toList());

        log.info("Non-empty queues: {}", nonEmptyQueues);

        return nonEmptyQueues.isEmpty();
    }
}
