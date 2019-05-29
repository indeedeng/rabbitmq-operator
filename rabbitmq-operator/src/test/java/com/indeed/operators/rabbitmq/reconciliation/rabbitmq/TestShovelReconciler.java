package com.indeed.operators.rabbitmq.reconciliation.rabbitmq;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.indeed.operators.rabbitmq.Constants;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiFacade;
import com.indeed.operators.rabbitmq.api.RabbitManagementApiProvider;
import com.indeed.operators.rabbitmq.controller.SecretsController;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.AddressAndVhost;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.DestinationShovelSpec;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.ShovelSpec;
import com.indeed.operators.rabbitmq.model.crd.rabbitmq.SourceShovelSpec;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQCluster;
import com.indeed.rabbitmq.admin.pojo.Shovel;
import com.indeed.rabbitmq.admin.pojo.ShovelArguments;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static com.indeed.operators.rabbitmq.Constants.Uris.AMQP_BASE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestShovelReconciler {

    @Mock
    private RabbitManagementApiProvider apiProvider;

    @Mock
    private SecretsController secretsController;

    @InjectMocks
    private ShovelReconciler shovelReconciler;

    @Test
    public void testReconcile_newShovel() {
        final SourceShovelSpec sourceShovelSpec = new SourceShovelSpec("myqueue", "myvhost");
        final DestinationShovelSpec destShovelSpec = new DestinationShovelSpec(
                Lists.newArrayList(new AddressAndVhost("addr1", "vhost1")),
                "mysecretname",
                "mysecretnamespace"
        );
        final ShovelSpec shovelSpec = new ShovelSpec("myshovel", sourceShovelSpec, destShovelSpec);

        final Secret shovelSecret = new SecretBuilder().withData(ImmutableMap.of(
                Constants.Secrets.USERNAME_KEY, "username",
                Constants.Secrets.PASSWORD_KEY, "password"
        )).build();

        final RabbitMQCluster cluster = buildCluster(Lists.newArrayList(shovelSpec));

        final RabbitManagementApiFacade api = mock(RabbitManagementApiFacade.class);

        when(apiProvider.getApi(cluster)).thenReturn(api);
        when(api.listShovels()).thenReturn(Collections.emptyList());
        when(secretsController.get("mysecretname", "mysecretnamespace")).thenReturn(shovelSecret);
        when(secretsController.decodeSecretPayload("username")).thenReturn("decoded-username");
        when(secretsController.decodeSecretPayload("password")).thenReturn("decoded-password");

        shovelReconciler.reconcile(cluster);

        final ShovelArguments shovelArguments = new ShovelArguments()
                .withSrcUri(Lists.newArrayList(AMQP_BASE))
                .withSrcQueue("myqueue")
                .withDestUri(Lists.newArrayList("amqp://decoded-username:decoded-password@addr1/vhost1"));
        final Shovel shovel = new Shovel()
                .withValue(shovelArguments)
                .withVhost("myvhost")
                .withName("myshovel");

        verify(api).createShovel("myvhost", "myshovel", shovel);
    }

    @Test
    public void testReconcile_updateShovel() {
        final SourceShovelSpec sourceShovelSpec = new SourceShovelSpec("myqueue", "myvhost");
        final DestinationShovelSpec destShovelSpec = new DestinationShovelSpec(
                Lists.newArrayList(new AddressAndVhost("addr1", "vhost1")),
                "mysecretname",
                "mysecretnamespace"
        );
        final ShovelSpec shovelSpec = new ShovelSpec("myshovel", sourceShovelSpec, destShovelSpec);

        final Secret shovelSecret = new SecretBuilder().withData(ImmutableMap.of(
                Constants.Secrets.USERNAME_KEY, "username",
                Constants.Secrets.PASSWORD_KEY, "password"
        )).build();

        final RabbitMQCluster cluster = buildCluster(Lists.newArrayList(shovelSpec));

        final RabbitManagementApiFacade api = mock(RabbitManagementApiFacade.class);
        final Shovel existingShovel = new Shovel().withName("myshovel");

        when(apiProvider.getApi(cluster)).thenReturn(api);
        when(api.listShovels()).thenReturn(Lists.newArrayList(existingShovel));
        when(secretsController.get("mysecretname", "mysecretnamespace")).thenReturn(shovelSecret);
        when(secretsController.decodeSecretPayload("username")).thenReturn("decoded-username");
        when(secretsController.decodeSecretPayload("password")).thenReturn("decoded-password");

        shovelReconciler.reconcile(cluster);

        final ShovelArguments shovelArguments = new ShovelArguments()
                .withSrcUri(Lists.newArrayList(AMQP_BASE))
                .withSrcQueue("myqueue")
                .withDestUri(Lists.newArrayList("amqp://decoded-username:decoded-password@addr1/vhost1"));
        final Shovel shovel = new Shovel()
                .withValue(shovelArguments)
                .withVhost("myvhost")
                .withName("myshovel");

        verify(api).createShovel("myvhost", "myshovel", shovel);
    }

    @Test
    public void testReconcile_skipUpToDateShovel() {
        final SourceShovelSpec sourceShovelSpec = new SourceShovelSpec("myqueue", "myvhost");
        final DestinationShovelSpec destShovelSpec = new DestinationShovelSpec(
                Lists.newArrayList(new AddressAndVhost("addr1", "vhost1")),
                "mysecretname",
                "mysecretnamespace"
        );
        final ShovelSpec shovelSpec = new ShovelSpec("myshovel", sourceShovelSpec, destShovelSpec);

        final Secret shovelSecret = new SecretBuilder().withData(ImmutableMap.of(
                Constants.Secrets.USERNAME_KEY, "username",
                Constants.Secrets.PASSWORD_KEY, "password"
        )).build();

        final RabbitMQCluster cluster = buildCluster(Lists.newArrayList(shovelSpec));

        final RabbitManagementApiFacade api = mock(RabbitManagementApiFacade.class);
        final Shovel existingShovel = new Shovel()
                .withName("myshovel")
                .withVhost("myvhost")
                .withValue(new ShovelArguments()
                    .withSrcUri(Lists.newArrayList("amqp://"))
                    .withSrcQueue("myqueue")
                    .withDestUri(Lists.newArrayList("amqp://decoded-username:decoded-password@addr1/vhost1"))
                );

        when(apiProvider.getApi(cluster)).thenReturn(api);
        when(api.listShovels()).thenReturn(Lists.newArrayList(existingShovel));
        when(secretsController.get("mysecretname", "mysecretnamespace")).thenReturn(shovelSecret);
        when(secretsController.decodeSecretPayload("username")).thenReturn("decoded-username");
        when(secretsController.decodeSecretPayload("password")).thenReturn("decoded-password");

        shovelReconciler.reconcile(cluster);

        verify(api, never()).createShovel(any(), any(), any(Shovel.class));
    }

    @Test
    public void testReconcile_deleteUnknownShovel() {
        final SourceShovelSpec sourceShovelSpec = new SourceShovelSpec("myqueue", "myvhost");
        final DestinationShovelSpec destShovelSpec = new DestinationShovelSpec(
                Lists.newArrayList(new AddressAndVhost("addr1", "vhost1")),
                "mysecretname",
                "mysecretnamespace"
        );
        final ShovelSpec shovelSpec = new ShovelSpec("myshovel", sourceShovelSpec, destShovelSpec);

        final Secret shovelSecret = new SecretBuilder().withData(ImmutableMap.of(
                Constants.Secrets.USERNAME_KEY, "username",
                Constants.Secrets.PASSWORD_KEY, "password"
        )).build();

        final RabbitMQCluster cluster = buildCluster(Lists.newArrayList(shovelSpec));

        final RabbitManagementApiFacade api = mock(RabbitManagementApiFacade.class);
        final Shovel existingShovel = new Shovel().withName("some-unknown-shovel").withVhost("some-vhost");

        when(apiProvider.getApi(cluster)).thenReturn(api);
        when(api.listShovels()).thenReturn(Lists.newArrayList(existingShovel));
        when(secretsController.get("mysecretname", "mysecretnamespace")).thenReturn(shovelSecret);
        when(secretsController.decodeSecretPayload("username")).thenReturn("decoded-username");
        when(secretsController.decodeSecretPayload("password")).thenReturn("decoded-password");

        shovelReconciler.reconcile(cluster);

        final ShovelArguments shovelArguments = new ShovelArguments()
                .withSrcUri(Lists.newArrayList(AMQP_BASE))
                .withSrcQueue("myqueue")
                .withDestUri(Lists.newArrayList("amqp://decoded-username:decoded-password@addr1/vhost1"));
        final Shovel shovel = new Shovel()
                .withValue(shovelArguments)
                .withVhost("myvhost")
                .withName("myshovel");

        verify(api).deleteShovel("some-vhost", "some-unknown-shovel");
        verify(api).createShovel("myvhost", "myshovel", shovel);
    }

    public RabbitMQCluster buildCluster(final List<ShovelSpec> shovels) {
        return RabbitMQCluster.newBuilder()
                .withName("mycluster")
                .withNamespace("ns")
                .withShovels(shovels)
                .build();
    }
}
