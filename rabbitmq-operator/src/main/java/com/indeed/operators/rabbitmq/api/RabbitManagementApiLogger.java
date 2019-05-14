package com.indeed.operators.rabbitmq.api;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RabbitManagementApiLogger implements Interceptor {

    private final Logger log = LoggerFactory.getLogger(RabbitManagementApiLogger.class);

    @Override
    public Response intercept(final Chain chain) throws IOException {
        final Request req = chain.request();

        log.debug("Executing {} call to {}", req.method(), req.url().toString());

        return chain.proceed(req);
    }
}
