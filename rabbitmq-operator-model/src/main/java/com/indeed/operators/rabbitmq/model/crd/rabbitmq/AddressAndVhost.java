package com.indeed.operators.rabbitmq.model.crd.rabbitmq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AddressAndVhost {

    private final String address;
    private final String vhost;

    @JsonCreator
    public AddressAndVhost(
            @JsonProperty("address") final String address,
            @JsonProperty("vhost") final String vhost
    ) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(address), "'address' cannot be empty or null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(address), "'vhost' cannot be empty or null");

        this.address = address;
        this.vhost = vhost;
    }

    public String getAddress() {
        return address;
    }

    public String getVhost() {
        return vhost;
    }

    @JsonIgnore
    public String asRabbitUri() {
        try {
            return String.format("%s/%s", address, URLEncoder.encode(vhost, StandardCharsets.UTF_8.name()));
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
