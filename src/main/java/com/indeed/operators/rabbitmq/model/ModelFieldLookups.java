package com.indeed.operators.rabbitmq.model;

import io.fabric8.kubernetes.api.model.HasMetadata;

public class ModelFieldLookups {

    public static String getName(final HasMetadata object) {
        return object.getMetadata().getName();
    }
}
