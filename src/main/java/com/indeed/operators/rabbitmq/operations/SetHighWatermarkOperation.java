package com.indeed.operators.rabbitmq.operations;

import com.google.common.base.Preconditions;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SetHighWatermarkOperation {
    private static final Logger log = LoggerFactory.getLogger(SetHighWatermarkOperation.class);

    private final KubernetesClient client;

    public SetHighWatermarkOperation(
            final KubernetesClient client
    ) {
        this.client = Preconditions.checkNotNull(client);
    }

    public void execute(final List<String> podNames, final double highWatermark) {
        Preconditions.checkArgument(highWatermark >= 0 && highWatermark <= 1);
        podNames.forEach(pod -> {
            log.info("Setting high watermark on node {}", pod);
            try(final ExecWatch ignored = client.pods().inNamespace(client.getNamespace()).withName(pod).writingOutput(System.out).exec("rabbitmqctl", "set_vm_memory_high_watermark", String.valueOf(highWatermark))) { }
        });
    }
}
