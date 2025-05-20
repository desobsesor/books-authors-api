package com.books.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
        "rate-limiting.enabled=true",
        "rate-limiting.strategy=IP_ADDRESS",
        "rate-limiting.response-headers=true"
})
public class RateLimitingConfigTest {

    private RateLimitingConfig rateLimitingConfig;

    @BeforeEach
    void setUp() {
        rateLimitingConfig = new RateLimitingConfig();
        rateLimitingConfig.setStrategy("IP_ADDRESS");
        rateLimitingConfig.setResponseHeaders(true);
        rateLimitingConfig.setDefaultSettings(new RateLimitingConfig.RateLimitSettings());
        rateLimitingConfig.setEndpoints(new ArrayList<>());
    }

    @Test
    @DisplayName("Should have correct default values after initialization")
    void shouldHaveCorrectDefaultValues() {
        assertTrue(rateLimitingConfig.isEnabled());
        assertEquals("IP_ADDRESS", rateLimitingConfig.getStrategy());
        assertTrue(rateLimitingConfig.isResponseHeaders());
        assertNotNull(rateLimitingConfig.getDefaultSettings());
        assertNotNull(rateLimitingConfig.getEndpoints());
        assertTrue(rateLimitingConfig.getEndpoints().isEmpty());
    }

    @Test
    @DisplayName("Should configure default settings correctly")
    void shouldConfigureDefaultSettings() {
        RateLimitingConfig.RateLimitSettings settings = rateLimitingConfig.getDefaultSettings();
        settings.setLimit(100);
        settings.setRefreshPeriod(60);
        settings.setTimeUnit(TimeUnit.SECONDS);

        assertEquals(100, settings.getLimit());
        assertEquals(60, settings.getRefreshPeriod());
        assertEquals(TimeUnit.SECONDS, settings.getTimeUnit());

        settings.setLimit(200);
        settings.setRefreshPeriod(30);
        settings.setTimeUnit(TimeUnit.MINUTES);

        assertEquals(200, settings.getLimit());
        assertEquals(30, settings.getRefreshPeriod());
        assertEquals(TimeUnit.MINUTES, settings.getTimeUnit());
    }

    @Test
    @DisplayName("Should manage endpoint limits correctly")
    void shouldManageEndpointLimits() {
        RateLimitingConfig.EndpointLimit endpoint = new RateLimitingConfig.EndpointLimit();
        endpoint.setPattern("/api/books/");
        endpoint.setLimit(50);
        endpoint.setRefreshPeriod(120);
        endpoint.setTimeUnit(TimeUnit.SECONDS);

        List<RateLimitingConfig.EndpointLimit> endpoints = new ArrayList<>();
        endpoints.add(endpoint);
        rateLimitingConfig.setEndpoints(endpoints);

        assertEquals(1, rateLimitingConfig.getEndpoints().size());
        RateLimitingConfig.EndpointLimit savedEndpoint = rateLimitingConfig.getEndpoints().get(0);
        assertEquals("/api/books/", savedEndpoint.getPattern());
        assertEquals(50, savedEndpoint.getLimit());
        assertEquals(120, savedEndpoint.getRefreshPeriod());
        assertEquals(TimeUnit.SECONDS, savedEndpoint.getTimeUnit());
    }

    @Test
    @DisplayName("Should allow changing rate limiting strategy")
    void shouldAllowChangingRateLimitingStrategy() {
        rateLimitingConfig.setStrategy("USER");
        assertEquals("USER", rateLimitingConfig.getStrategy());

        rateLimitingConfig.setStrategy("TOKEN");
        assertEquals("TOKEN", rateLimitingConfig.getStrategy());
    }

    @Test
    @DisplayName("Should allow enabling/disabling response headers")
    void shouldToggleResponseHeaders() {
        rateLimitingConfig.setResponseHeaders(false);
        assertFalse(rateLimitingConfig.isResponseHeaders());

        rateLimitingConfig.setResponseHeaders(true);
        assertTrue(rateLimitingConfig.isResponseHeaders());
    }

    @Test
    @DisplayName("Should allow enabling/disabling global rate limiting")
    void shouldToggleGlobalRateLimiting() {
        rateLimitingConfig.setEnabled(false);
        assertFalse(rateLimitingConfig.isEnabled());

        rateLimitingConfig.setEnabled(true);
        assertTrue(rateLimitingConfig.isEnabled());
    }
}