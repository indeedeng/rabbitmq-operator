package com.indeed.operators.rabbitmq;

public class Constants {

    public static final String RABBITMQ_CRD_NAME = "rabbitmqs.indeed.com";
    public static final String RABBITMQ_NETWORK_PARTITION_CRD_NAME = "rabbitmqnetworkpartitions.indeed.com";
    public static final String RABBITMQ_USER_CRD_NAME = "rabbitmqusers.indeed.com";

    public static final String RABBITMQ_STORAGE_NAME = "rabbitmq-storage";

    public static final String DEFAULT_USERNAME = "rabbit";

    public static class Ports {
        public static final String EPMD = "epmd";
        public static final String AMQP = "amqp";
        public static final String MANAGEMENT = "management";

        public static final int EPMD_PORT = 4369;
        public static final int AMQP_PORT = 5672;
        public static final int MANAGEMENT_PORT = 15672;
    }
}