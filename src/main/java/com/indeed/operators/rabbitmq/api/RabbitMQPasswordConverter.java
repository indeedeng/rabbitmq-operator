package com.indeed.operators.rabbitmq.api;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

public class RabbitMQPasswordConverter {

    private final Random random;
    private final MessageDigest messageDigest;
    private final Base64.Encoder base64Encoder;
    private final Base64.Decoder base64Decoder;


    public RabbitMQPasswordConverter(
            final Random random,
            final MessageDigest messageDigest,
            final Base64.Encoder base64Encoder,
            final Base64.Decoder base64Decoder
    ) {
        this.random = random;
        this.messageDigest = messageDigest;
        this.base64Encoder = base64Encoder;
        this.base64Decoder = base64Decoder;
    }

    public String convertPasswordToHash(final String password) {
        return convertPasswordToHash(password, random.nextInt());
    }

    public String convertPasswordToHash(final String password, final int salt) {
        final byte[] saltBytes = ByteBuffer.allocate(4).putInt(salt).array();
        final byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

        final byte[] passwordWithSalt = ByteBuffer.allocate(saltBytes.length + passwordBytes.length).put(saltBytes).put(passwordBytes).array();

        final byte[] hashedBytes = messageDigest.digest(passwordWithSalt);

        return base64Encoder.encodeToString(ByteBuffer.allocate(hashedBytes.length + saltBytes.length).put(saltBytes).put(hashedBytes).array());
    }

    public boolean passwordMatchesHash(final String password, final String hash) {
        final byte[] decodedHash = base64Decoder.decode(hash);
        final int salt = ByteBuffer.wrap(decodedHash).getInt();

        return convertPasswordToHash(password, salt).equals(hash);
    }
}
