package com.indeed.operators.rabbitmq.model;

import com.google.common.collect.Maps;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.Collections;
import java.util.Map;

public class Labels {

    public static class Indeed {
        public static final String INDEED_PREFIX = "indeed.com/";
        public static final String LOCKED_BY = INDEED_PREFIX + "locked-by";

        private Indeed() {}

        public static Map<String, String> getIndeedLabels(final HasMetadata object) {
            if (object.getMetadata().getLabels() == null || object.getMetadata().getLabels().isEmpty()) {
                return Collections.emptyMap();
            }

            final Map<String, String> indeedLabels = Maps.newHashMap();
            for (final Map.Entry<String, String> entry : object.getMetadata().getLabels().entrySet()) {
                if (entry.getKey().startsWith(INDEED_PREFIX)) {
                    indeedLabels.put(entry.getKey(), entry.getValue());
                }
            }

            return indeedLabels;
        }
    }

    public static class Kubernetes {
        public static final String KUBERNETES_PREFIX = "app.kubernetes.io/";
        public static final String PART_OF = KUBERNETES_PREFIX + "part-of";
        public static final String MANAGED_BY = KUBERNETES_PREFIX + "managed-by";
        public static final String INSTANCE = KUBERNETES_PREFIX + "instance";

        private Kubernetes() {}
    }

    public static class Values {

        public static final String RABBITMQ = "rabbitmq";
        public static final String RABBITMQ_OPERATOR = "rabbitmq-operator";
    }
}
