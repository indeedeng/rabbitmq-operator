package com.indeed.operators.rabbitmq.api;

import retrofit2.Response;

import java.io.IOException;

public class RabbitApiResponseConsumer {

    public static <T> T consumeResponse(final Response<T> response) {
        if (!response.isSuccessful()) {
            final String errorMessage;
            try {
                errorMessage = response.errorBody().string();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }

            throw new RabbitManagementApiException(errorMessage);
        }

        return response.body();
    }
}
