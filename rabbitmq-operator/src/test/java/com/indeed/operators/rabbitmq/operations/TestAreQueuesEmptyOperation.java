package com.indeed.operators.rabbitmq.operations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiProvider;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQConnectionInfo;
import com.indeed.rabbitmq.admin.RabbitManagementApi;
import com.indeed.rabbitmq.admin.RabbitManagementApiFactory;
import com.indeed.rabbitmq.admin.pojo.Queue;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private RabbitManagementApiProvider apiCache;

    @InjectMocks
    private AreQueuesEmptyOperation operation;

    private MockWebServer mockWebServer;

    @BeforeEach
    public void setup() {
        mockWebServer = new MockWebServer();
    }

    @AfterEach
    public void cleanup() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testAllQueuesEmpty() throws IOException {
        final RabbitMQConnectionInfo connectionInfo = new RabbitMQConnectionInfo("username", "password", "nodename", "servicename");

        final Queue queue1 = new Queue().withName("queue1").withMessages(0L);
        final Queue queue2 = new Queue().withName("queue2").withMessages(0L);
        final Queue queue3 = new Queue().withName("queue3").withMessages(0L);

        mockWebServer.enqueue(new MockResponse().setBody(MAPPER.writeValueAsString(Lists.newArrayList(queue1, queue2, queue3))));

        mockWebServer.start();

        final RabbitManagementApi apiClient = RabbitManagementApiFactory.newInstance(mockWebServer.url("/api/").uri());

        when(apiCache.getApi(connectionInfo)).thenReturn(apiClient);
        apiClient.listQueues();

        assertTrue(operation.execute(connectionInfo));
    }

    @Test
    public void testOneQueuesHasMessages() throws IOException {
        final RabbitMQConnectionInfo connectionInfo = new RabbitMQConnectionInfo("username", "password", "nodename", "servicename");

        final Queue queue1 = new Queue().withName("queue1").withMessages(0L);
        final Queue queue2 = new Queue().withName("queue2").withMessages(1L);
        final Queue queue3 = new Queue().withName("queue1").withMessages(0L);

        mockWebServer.enqueue(new MockResponse().setBody(MAPPER.writeValueAsString(Lists.newArrayList(queue1, queue2, queue3))));

        mockWebServer.start();

        final RabbitManagementApi apiClient = RabbitManagementApiFactory.newInstance(mockWebServer.url("/api/").uri());

        when(apiCache.getApi(connectionInfo)).thenReturn(apiClient);

        assertFalse(operation.execute(connectionInfo));
    }

    @Test
    public void testMultipleQueuesHaveMessages() throws IOException {
        final RabbitMQConnectionInfo connectionInfo = new RabbitMQConnectionInfo("username", "password", "nodename", "servicename");

        final Queue queue1 = new Queue().withName("queue1").withMessages(0L);
        final Queue queue2 = new Queue().withName("queue2").withMessages(1L);
        final Queue queue3 = new Queue().withName("queue3").withMessages(1L);

        mockWebServer.enqueue(new MockResponse().setBody(MAPPER.writeValueAsString(Lists.newArrayList(queue1, queue2, queue3))));

        mockWebServer.start();

        final RabbitManagementApi apiClient = RabbitManagementApiFactory.newInstance(mockWebServer.url("/api/").uri());

        when(apiCache.getApi(connectionInfo)).thenReturn(apiClient);

        assertFalse(operation.execute(connectionInfo));
    }
}
