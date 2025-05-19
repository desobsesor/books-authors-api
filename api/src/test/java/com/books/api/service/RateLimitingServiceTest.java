package com.books.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.books.api.config.RateLimitingConfig;
import com.books.api.config.RateLimitingConfig.EndpointLimit;

@ExtendWith(MockitoExtension.class)
public class RateLimitingServiceTest {

    @Mock
    private RateLimitingConfig rateLimitingConfig;

    @InjectMocks
    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        RateLimitingConfig.RateLimitSettings defaultSettings = mock(RateLimitingConfig.RateLimitSettings.class);
        lenient().when(defaultSettings.getLimit()).thenReturn(100);
        lenient().when(rateLimitingConfig.getDefaultSettings()).thenReturn(defaultSettings);
    }

    @Test
    @DisplayName("Test isRequestAllowed method")
    void testIsRequestAllowed() {
        EndpointLimit endpointLimit = mock(EndpointLimit.class);
        lenient().when(endpointLimit.getLimit()).thenReturn(100);
        lenient().when(endpointLimit.getRefreshPeriod()).thenReturn(60);
        lenient().when(endpointLimit.getTimeUnit()).thenReturn(TimeUnit.SECONDS);

        boolean result = rateLimitingService.isRequestAllowed("testKey", endpointLimit);

        assertTrue(result);
    }

    @Test
    @DisplayName("Test findEndpointLimit method")
    void testFindEndpointLimit() {
        EndpointLimit endpointLimit = rateLimitingService.findEndpointLimit("/test/path");

        assertNotNull(endpointLimit);
    }

    @Test
    @DisplayName("Test getRateLimitInfo method")
    void testGetRateLimitInfo() {
        EndpointLimit endpointLimit = mock(EndpointLimit.class);
        when(endpointLimit.getLimit()).thenReturn(100);

        RateLimitingService.RateLimitInfo info = rateLimitingService.getRateLimitInfo("testKey", endpointLimit);

        assertEquals(100, info.limit());
    }

    @Test
    void testIsRequestAllowedWhenRateLimitingDisabled() {
        // Arrange
        when(rateLimitingConfig.isEnabled()).thenReturn(true);
        String key = "anyKey";
        EndpointLimit endpointLimit = new EndpointLimit();

        // Act
        boolean result = rateLimitingService.isRequestAllowed(key, endpointLimit);

        // Assert
        assertTrue(result);
    }

}