package com.indeed.operators.rabbitmq.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class TestRabbitMQPasswordConverter {

    private static final String HASH_FROM_RABBIT = "awdOVA7vfvoxZZnftimygbY4kfrvQdv9vlESjpvmMQooWDV7";
    private static final String PASSWORD = "rJCW15GyswdLMXOSo4fLvgMykf6Q3Q";
    private static final int SALT = 1795640916; // first four bytes of HASH_FROM_RABBIT

    private RabbitMQPasswordConverter converter;

    @BeforeEach
    private void setup() throws NoSuchAlgorithmException {
        final Random myRandom = new Random() {
            @Override
            public int nextInt() {
                return SALT;
            }
        };

        converter = new RabbitMQPasswordConverter(myRandom, MessageDigest.getInstance("SHA-256"), Base64.getEncoder(), Base64.getDecoder());
    }

    @Test
    public void testConvertPasswordToHash() {
        final String hash = converter.convertPasswordToHash(PASSWORD);

        assertEquals(HASH_FROM_RABBIT, hash);
    }

    @Test
    public void testPasswordMatchesHashMatches() {
        assertTrue(converter.passwordMatchesHash(PASSWORD, HASH_FROM_RABBIT));
    }

    @Test
    public void testPasswordMatchesHashDoesNotMatch() {
        assertFalse(converter.passwordMatchesHash(PASSWORD, "awdOVA7vfvoxZZnftimygbY4kfrvQdv9vlESjpvmMQooWDV8"));
    }
}
