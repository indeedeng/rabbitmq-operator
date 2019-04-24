package com.indeed.operators.rabbitmq.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.indeed.operators.rabbitmq.Constants;
import com.indeed.operators.rabbitmq.controller.SecretsController;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQConnectionInfo;
import com.indeed.operators.rabbitmq.model.rabbitmq.api.BaseParameter;
import com.indeed.operators.rabbitmq.model.rabbitmq.api.ShovelParameterValue;
import com.indeed.operators.rabbitmq.model.rabbitmq.api.User;
import com.indeed.operators.rabbitmq.resources.RabbitMQSecrets;
import com.indeed.operators.rabbitmq.resources.RabbitMQServices;
import io.fabric8.kubernetes.api.model.Secret;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RabbitMQApiClient {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQApiClient.class);

    private final OkHttpClient httpClient;
    private final ObjectMapper mapper;
    private final SecretsController secretsController;

    public RabbitMQApiClient(
            final OkHttpClient httpClient,
            final ObjectMapper mapper,
            final SecretsController secretsController
    ) {
        this.httpClient = httpClient;
        this.mapper = mapper;
        this.secretsController = secretsController;
    }

    public List<User> getUsers(final RabbitMQConnectionInfo connectionInfo) throws IOException {
        final String url = String.format("%s/users", buildRootUrl(connectionInfo));
        final Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", buildAuthorizationHeader(connectionInfo))
                .get()
                .build();

        final Response response = httpClient.newCall(req).execute();

        return mapper.readValue(response.body().byteStream(), new TypeReference<List<User>>() {});
    }

    public List<BaseParameter<ShovelParameterValue>> getShovels(final RabbitMQConnectionInfo connectionInfo, final String vhost) throws IOException {
        final String url = String.format(
                "%s/parameters/%s/%s",
                buildRootUrl(connectionInfo),
                "shovel",
                encodeString(vhost)
        );

        final Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", buildAuthorizationHeader(connectionInfo))
                .get()
                .build();

        final Response response = httpClient.newCall(req).execute();

        return mapper.readValue(response.body().byteStream(), new TypeReference<List<BaseParameter<ShovelParameterValue>>>() {});
    }

    public void createOrUpdateShovel(final RabbitMQConnectionInfo connectionInfo, final BaseParameter<ShovelParameterValue> shovel) throws IOException {
        final String url = String.format(
                "%s/parameters/%s/%s/%s",
                buildRootUrl(connectionInfo),
                shovel.getComponent(),
                encodeString(shovel.getVhost()),
                encodeString(shovel.getName())
        );

        final Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", buildAuthorizationHeader(connectionInfo))
                .put(RequestBody.create(MediaType.parse("application/json"), serializePayload(shovel)))
                .build();

        httpClient.newCall(req).execute();
    }

    public void deleteShovel(final RabbitMQConnectionInfo connectionInfo, final String vhost, final String shovelName) throws IOException {
        final String url = String.format(
                "%s/parameters/%s/%s/%s",
                buildRootUrl(connectionInfo),
                "shovel",
                encodeString(vhost),
                encodeString(shovelName)
        );

        final Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", buildAuthorizationHeader(connectionInfo))
                .delete()
                .build();

        httpClient.newCall(req).execute();
    }

    private byte[] serializePayload(final Object payload) {
        try {
            return mapper.writeValueAsBytes(payload);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildRootUrl(final RabbitMQConnectionInfo connectionInfo) {
        return String.format("http://%s:15672/api", buildApiRootAddress(connectionInfo));
    }

    private String buildApiRootAddress(final RabbitMQConnectionInfo connectionInfo) {
        if (connectionInfo.getNodeName().isPresent()) {
            return String.format("%s.%s", connectionInfo.getNodeName().get(), RabbitMQServices.getDiscoveryServiceName(connectionInfo.getClusterName()));
        }

        return RabbitMQServices.getDiscoveryServiceName(connectionInfo.getClusterName());
    }

    private String buildAuthorizationHeader(final RabbitMQConnectionInfo connectionInfo) {
        final String secretName = RabbitMQSecrets.getClusterSecretName(connectionInfo.getClusterName());
        final Secret secret = secretsController.get(secretName, connectionInfo.getNamespace());

        Preconditions.checkNotNull(secret, String.format("Could not find secret with name [%s] in namespace [%s]", secretName, connectionInfo.getNamespace()));

        final String username = secretsController.decodeSecretPayload(secret.getData().get(Constants.Secrets.USERNAME_KEY));
        final String password = secretsController.decodeSecretPayload(secret.getData().get(Constants.Secrets.PASSWORD_KEY));

        return Credentials.basic(username, password);
    }

    private String encodeString(final String str) {
        try {
            return URLEncoder.encode(str, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
