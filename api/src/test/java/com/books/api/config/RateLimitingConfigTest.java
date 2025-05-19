package com.books.api.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RateLimitingConfig.class })
public class RateLimitingConfigTest {

    @Autowired
    private RateLimitingConfig rateLimitingConfig;

    @Test
    public void testRateLimitingEnabled() {
        assertTrue(rateLimitingConfig.isEnabled());
    }
}