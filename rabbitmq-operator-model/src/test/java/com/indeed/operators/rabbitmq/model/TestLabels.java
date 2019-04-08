package com.indeed.operators.rabbitmq.model;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PodBuilder;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestLabels {

    @Test
    public void testGetIndeedLabels() {
        final Map<String, String> labels = ImmutableMap.<String, String>builder()
                .put("notindeed.com/label1", "label1")
                .put("indeed.com/label2", "label2")
                .put(" indeed.com/label3", "label3")
                .put("Indeed.com/label4", "label4")
                .put("indeed.comcom/label5", "label5")
                .build();

        final HasMetadata obj = new PodBuilder().withMetadata(new ObjectMetaBuilder().withLabels(labels).build()).build();

        final Map<String, String> indeedLabels = Labels.Indeed.getIndeedLabels(obj);

        assertEquals(1, indeedLabels.size());
        assertTrue(indeedLabels.containsKey("indeed.com/label2"));
        assertEquals("label2", indeedLabels.get("indeed.com/label2"));
    }
}
