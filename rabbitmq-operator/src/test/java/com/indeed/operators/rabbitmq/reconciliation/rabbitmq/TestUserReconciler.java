package com.indeed.operators.rabbitmq.reconciliation.rabbitmq;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.indeed.operators.rabbitmq.Constants;
import com.indeed.operators.rabbitmq.api.RabbitMQPasswordConverter;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiFacade;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiProvider;
import com.indeed.operators.rabbitmq.controller.SecretsController;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.VhostOperationPermissions;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.VhostPermissions;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQCluster;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQUser;
import com.indeed.operators.rabbitmq.resources.RabbitMQSecrets;
import com.indeed.operators.rabbitmq.resources.RabbitMQServices;
import com.indeed.rabbitmq.admin.pojo.Permission;
import com.indeed.rabbitmq.admin.pojo.User;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestUserReconciler {
    private static final String CLUSTER_NAME = "mycluster";
    private static final String NAMESPACE = "ns";

    @Mock
    private SecretsController secretsController;

    @Mock
    private RabbitManagementApiProvider managementApiProvider;

    @Mock
    private RabbitMQPasswordConverter passwordConverter;

    @InjectMocks
    private UserReconciler userReconciler;

    @Test
    public void testReconcile_createMissingUser() {
        final RabbitManagementApiFacade api = mock(RabbitManagementApiFacade.class);

        final RabbitMQUser user = generateRabbitMQUser(Collections.emptyList(), Collections.emptyList());
        final RabbitMQCluster cluster = generateCluster(Lists.newArrayList(user));

        when(managementApiProvider.getApi(cluster)).thenReturn(api);
        when(api.listUsers()).thenReturn(Collections.emptyList());
        when(secretsController.createOrUpdate(user.getUserSecret())).thenReturn(user.getUserSecret());
        when(secretsController.decodeSecretPayload("password")).thenReturn("password");
        when(passwordConverter.convertPasswordToHash("password")).thenReturn("password-hash");

        userReconciler.reconcile(cluster);

        final User rabbitUser = new User().withName("username").withPasswordHash("password-hash").withTags("");

        verify(api).createUser("username", rabbitUser);
    }

    @Test
    public void testReconcile_updateUser() {
        final RabbitManagementApiFacade api = mock(RabbitManagementApiFacade.class);

        final RabbitMQUser rabbitmqUser = generateRabbitMQUser(Collections.emptyList(), Lists.newArrayList("newtag1", "newtag2"));
        final RabbitMQCluster cluster = generateCluster(Lists.newArrayList(rabbitmqUser));
        final User user = new User().withName("username").withPasswordHash("password-hash").withTags("tag1,tag2");

        when(managementApiProvider.getApi(cluster)).thenReturn(api);
        when(api.listUsers()).thenReturn(Lists.newArrayList(user));
        when(secretsController.get(rabbitmqUser.getUserSecret().getMetadata().getName(), rabbitmqUser.getUserSecret().getMetadata().getNamespace())).thenReturn(rabbitmqUser.getUserSecret());
        when(secretsController.decodeSecretPayload("password")).thenReturn("password");
        when(passwordConverter.convertPasswordToHash("password")).thenReturn("new-password-hash");

        userReconciler.reconcile(cluster);

        final User rabbitUser = new User().withName("username").withPasswordHash("new-password-hash").withTags("newtag1,newtag2");

        verify(api).createUser("username", rabbitUser);
    }

    @Test
    public void testReconcile_updateVhost() {
        final RabbitManagementApiFacade api = mock(RabbitManagementApiFacade.class);

        final List<VhostPermissions> newVhostPermissions = Lists.newArrayList(
                new VhostPermissions("newVhost", new VhostOperationPermissions("conf", "write", "read"))
        );

        final RabbitMQUser rabbitmqUser = generateRabbitMQUser(newVhostPermissions, Collections.emptyList());
        final RabbitMQCluster cluster = generateCluster(Lists.newArrayList(rabbitmqUser));
        final User user = new User().withName("username").withPasswordHash("password-hash").withTags("");

        when(managementApiProvider.getApi(cluster)).thenReturn(api);
        when(api.listUsers()).thenReturn(Lists.newArrayList(user));
        when(secretsController.get(rabbitmqUser.getUserSecret().getMetadata().getName(), rabbitmqUser.getUserSecret().getMetadata().getNamespace())).thenReturn(rabbitmqUser.getUserSecret());
        when(secretsController.decodeSecretPayload("password")).thenReturn("password");
        when(passwordConverter.convertPasswordToHash("password")).thenReturn("password-hash");

        userReconciler.reconcile(cluster);

        final ArgumentCaptor<Permission> permissionCaptor = ArgumentCaptor.forClass(Permission.class);

        verify(api).createUser("username", user);
        verify(api).createPermission(eq("newVhost"), eq("username"), permissionCaptor.capture());

        final Permission capturedPermission = permissionCaptor.getValue();
        assertEquals("conf", capturedPermission.getConfigure().pattern());
        assertEquals("write", capturedPermission.getWrite().pattern());
        assertEquals("read", capturedPermission.getRead().pattern());
    }

    @Test
    public void testReconcile_updatePermissions() {
        final RabbitManagementApiFacade api = mock(RabbitManagementApiFacade.class);

        final List<VhostPermissions> newVhostPermissions = Lists.newArrayList(
                new VhostPermissions("vhost", new VhostOperationPermissions("newconf", "newwrite", "newread"))
        );

        final RabbitMQUser rabbitmqUser = generateRabbitMQUser(newVhostPermissions, Collections.emptyList());
        final RabbitMQCluster cluster = generateCluster(Lists.newArrayList(rabbitmqUser));
        final User user = new User().withName("username").withPasswordHash("password-hash").withTags("");

        when(managementApiProvider.getApi(cluster)).thenReturn(api);
        when(api.listUsers()).thenReturn(Lists.newArrayList(user));
        when(secretsController.get(rabbitmqUser.getUserSecret().getMetadata().getName(), rabbitmqUser.getUserSecret().getMetadata().getNamespace())).thenReturn(rabbitmqUser.getUserSecret());
        when(secretsController.decodeSecretPayload("password")).thenReturn("password");
        when(passwordConverter.convertPasswordToHash("password")).thenReturn("password-hash");

        userReconciler.reconcile(cluster);

        final ArgumentCaptor<Permission> permissionCaptor = ArgumentCaptor.forClass(Permission.class);

        verify(api).createUser("username", user);
        verify(api).createPermission(eq("vhost"), eq("username"), permissionCaptor.capture());

        final Permission capturedPermission = permissionCaptor.getValue();
        assertEquals("newconf", capturedPermission.getConfigure().pattern());
        assertEquals("newwrite", capturedPermission.getWrite().pattern());
        assertEquals("newread", capturedPermission.getRead().pattern());
    }

    @Test
    public void testReconcile_deleteUnknownUser() {
        final RabbitManagementApiFacade api = mock(RabbitManagementApiFacade.class);

        final RabbitMQCluster cluster = generateCluster(Collections.emptyList());
        final User user = new User().withName("username").withPasswordHash("password-hash").withTags("");

        when(managementApiProvider.getApi(cluster)).thenReturn(api);
        when(api.listUsers()).thenReturn(Lists.newArrayList(user));

        userReconciler.reconcile(cluster);

        verify(api, times(1)).deleteUser("username");
        verify(api, times(1)).deleteUser(anyString());
    }

    private RabbitMQUser generateRabbitMQUser(final List<VhostPermissions> vhostPermissions, final List<String> tags) {
        final Secret userSecret = new SecretBuilder()
                .withNewMetadata().withName(RabbitMQSecrets.getUserSecretName("username", CLUSTER_NAME)).withNamespace("ns").endMetadata()
                .withData(ImmutableMap.of(Constants.Secrets.USERNAME_KEY, "username", Constants.Secrets.PASSWORD_KEY, "password"))
                .build();
        final ObjectMeta clusterMetadata = new ObjectMetaBuilder().withName(CLUSTER_NAME).withNamespace(NAMESPACE).build();
        return new RabbitMQUser("username", userSecret, clusterMetadata, null, vhostPermissions, tags);
    }

    private RabbitMQCluster generateCluster(final List<RabbitMQUser> users) {
        return RabbitMQCluster.newBuilder()
                .withName(CLUSTER_NAME)
                .withNamespace(NAMESPACE)
                .withDiscoveryService(new ServiceBuilder().withNewMetadata().withName(RabbitMQServices.getDiscoveryServiceName(CLUSTER_NAME)).endMetadata().build())
                .withUsers(users)
                .build();
    }
}
