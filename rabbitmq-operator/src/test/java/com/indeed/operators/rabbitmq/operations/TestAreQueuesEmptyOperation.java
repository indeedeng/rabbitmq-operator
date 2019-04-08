package com.indeed.operators.rabbitmq.operations;

import com.google.common.collect.Lists;
import com.indeed.operators.rabbitmq.api.RabbitMQApiClient;
import com.indeed.operators.rabbitmq.model.rabbitmq.api.QueueState;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQConnectionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestAreQueuesEmptyOperation {

    @Mock
    private RabbitMQApiClient apiClient;

    @InjectMocks
    private AreQueuesEmptyOperation operation;

    @Test
    public void testAllQueuesEmpty() throws IOException {
        final RabbitMQConnectionInfo connectionInfo = new RabbitMQConnectionInfo("username", "password", "nodename", "servicename");

        final QueueState queueState1 = new QueueState("queue1", 0);
        final QueueState queueState2 = new QueueState("queue2", 0);
        final QueueState queueState3 = new QueueState("queue3", 0);

        when(apiClient.getQueues(connectionInfo)).thenReturn(Lists.newArrayList(queueState1, queueState2, queueState3));

        assertTrue(operation.execute(connectionInfo));
    }

    @Test
    public void testOneQueuesHasMessages() throws IOException {
        final RabbitMQConnectionInfo connectionInfo = new RabbitMQConnectionInfo("username", "password", "nodename", "servicename");

        final QueueState queueState1 = new QueueState("queue1", 0);
        final QueueState queueState2 = new QueueState("queue2", 1);
        final QueueState queueState3 = new QueueState("queue3", 0);

        when(apiClient.getQueues(connectionInfo)).thenReturn(Lists.newArrayList(queueState1, queueState2, queueState3));

        assertFalse(operation.execute(connectionInfo));
    }

    @Test
    public void testMultipleQueuesHaveMessages() throws IOException {
        final RabbitMQConnectionInfo connectionInfo = new RabbitMQConnectionInfo("username", "password", "nodename", "servicename");

        final QueueState queueState1 = new QueueState("queue1", 0);
        final QueueState queueState2 = new QueueState("queue2", 1);
        final QueueState queueState3 = new QueueState("queue3", 1);

        when(apiClient.getQueues(connectionInfo)).thenReturn(Lists.newArrayList(queueState1, queueState2, queueState3));

        assertFalse(operation.execute(connectionInfo));
    }
}
