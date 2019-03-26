package com.indeed.operators.rabbitmq.model.crd.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.Doneable;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;
import io.sundr.builder.annotations.Inline;

/**
 * See https://github.com/fabric8io/kubernetes-client/tree/master/kubernetes-examples/src/main/java/io/fabric8/kubernetes/examples/crds
 */
@Buildable(
        builderPackage = "io.fabric8.kubernetes.api.builder",
        inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"),
        editableEnabled = false,
        refs = @BuildableReference(CustomResource.class)
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(using = JsonDeserializer.None.class)
@JsonPropertyOrder({"apiVersion", "kind", "metadata", "spec"})
public class RabbitMQUserCustomResource extends CustomResource {
    private RabbitMQUserCustomResourceSpec spec;

    @JsonCreator
    public RabbitMQUserCustomResource(
            @JsonProperty("spec") final RabbitMQUserCustomResourceSpec spec
    ) {
        this.spec = spec;
    }

    public RabbitMQUserCustomResourceSpec getSpec() {
        return spec;
    }

    public void setSpec(final RabbitMQUserCustomResourceSpec spec) {
        this.spec = spec;
    }

    @JsonIgnore
    public String getName() {
        return this.getMetadata().getName();
    }
}
