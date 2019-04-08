package com.indeed.operators.rabbitmq.model.rabbitmq.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Objects;

@JsonDeserialize(using = JsonDeserializer.None.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private final String username;
    private final String passwordHash;
    private final String hashingAlgorithm;
    private final String tags;

    @JsonCreator
    public User(
            @JsonProperty("name") final String username,
            @JsonProperty("password_hash") final String passwordHash,
            @JsonProperty("hashAlgorithm") final String hashingAlgorithm,
            @JsonProperty("tags") final String tags
    ) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.hashingAlgorithm = hashingAlgorithm;
        this.tags = tags;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getHashingAlgorithm() {
        return hashingAlgorithm;
    }

    public String getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equal(username, user.username) &&
                Objects.equal(passwordHash, user.passwordHash) &&
                Objects.equal(hashingAlgorithm, user.hashingAlgorithm) &&
                Objects.equal(tags, user.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username, passwordHash, hashingAlgorithm, tags);
    }
}
