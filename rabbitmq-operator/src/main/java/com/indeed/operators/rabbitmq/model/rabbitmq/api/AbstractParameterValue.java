package com.indeed.operators.rabbitmq.model.rabbitmq.api;

/**
 * This class is the common base for all objects that live under the /api/parameters endpoint of the
 * Rabbit API. If a new class needs to be created for a parameter type, extend this class and return the
 * [component] portion of the URL when {@link AbstractParameterValue#getComponent()} is called.
 *
 * If you want automatic JSON serialization/deserialization (you almost certainly do), add a new
 * {@link com.fasterxml.jackson.annotation.JsonSubTypes.Type} declaration on the "value" field of the
 * {@link BaseParameter#(AbstractParameterValue, String, String, String)} constructor. It should look like
 * this:
 * <pre>
 *     @JsonSubTypes(value = {
 *      @JsonSubTypes.Type(value = ShovelParameterValue.class, name = "shovel"),
 *      @JsonSubTypes.Type(value = MyNewParameterValue.class, name = "mynewparametercomponent")
 *     }) final T value
 * </pre>
 */
public abstract class AbstractParameterValue {

    public abstract String getComponent();
}
