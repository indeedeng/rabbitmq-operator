package com.indeed.operators.rabbitmq;

public class OperatorException extends RuntimeException {
    public OperatorException(final String description, final Throwable cause) {
        super(description, cause);
    }
}
