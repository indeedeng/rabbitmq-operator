package com.indeed.operators.rabbitmq.api;

public class RabbitManagementApiException extends RuntimeException {

    RabbitManagementApiException(final String message) {
        super(message);
    }

    RabbitManagementApiException(final String message, final Throwable t) {
        super(message, t);
    }
}
