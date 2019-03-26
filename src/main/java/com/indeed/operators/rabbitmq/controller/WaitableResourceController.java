package com.indeed.operators.rabbitmq.controller;

import java.util.concurrent.TimeUnit;

public interface WaitableResourceController {

    void waitForReady(String name, String namespace, final long time, final TimeUnit timeUnit) throws InterruptedException;

    void waitForDeletion(String name, String namespace, final long time, final TimeUnit timeUnit) throws InterruptedException;
}
