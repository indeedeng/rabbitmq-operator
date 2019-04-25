package com.indeed.operators.rabbitmq.operations;

import com.google.common.collect.Lists;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiProvider;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQConnectionInfo;
import com.indeed.rabbitmq.admin.RabbitManagementApi;
import com.indeed.rabbitmq.admin.pojo.Queue;
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
    private RabbitManagementApi apiClient;

    @Mock
    private RabbitManagementApiProvider apiCache;

    @InjectMocks
    private AreQueuesEmptyOperation operation;

    @Test
    public void testAllQueuesEmpty() {
        final RabbitMQConnectionInfo connectionInfo = new RabbitMQConnectionInfo("username", "password", "nodename", "servicename");

        final Queue queue1 = new Queue().withName("queue1").withMessages(0L);
        final Queue queue2 = new Queue().withName("queue2").withMessages(0L);
        final Queue queue3 = new Queue().withName("queue3").withMessages(0L);

        when(apiCache.getApi(connectionInfo)).thenReturn(apiClient);
        when(apiClient.listQueues()).thenReturn(Lists.newArrayList(queue1, queue2, queue3));

        assertTrue(operation.execute(connectionInfo));
    }

    @Test
    public void testOneQueuesHasMessages() {
        final RabbitMQConnectionInfo connectionInfo = new RabbitMQConnectionInfo("username", "password", "nodename", "servicename");

        final Queue queue1 = new Queue().withName("queue1").withMessages(0L);
        final Queue queue2 = new Queue().withName("queue2").withMessages(1L);
        final Queue queue3 = new Queue().withName("queue1").withMessages(0L);

        when(apiCache.getApi(connectionInfo)).thenReturn(apiClient);
        when(apiClient.listQueues()).thenReturn(Lists.newArrayList(queue1, queue2, queue3));

        assertFalse(operation.execute(connectionInfo));
    }

    @Test
    public void testMultipleQueuesHaveMessages() {
        final RabbitMQConnectionInfo connectionInfo = new RabbitMQConnectionInfo("username", "password", "nodename", "servicename");

        final Queue queue1 = new Queue().withName("queue1").withMessages(0L);
        final Queue queue2 = new Queue().withName("queue2").withMessages(1L);
        final Queue queue3 = new Queue().withName("queue3").withMessages(1L);

        when(apiCache.getApi(connectionInfo)).thenReturn(apiClient);
        when(apiClient.listQueues()).thenReturn(Lists.newArrayList(queue1, queue2, queue3));

        assertFalse(operation.execute(connectionInfo));
    }
}
