package com.books.api.security;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.books.api.config.RateLimitingConfig;
import com.books.api.config.RateLimitingConfig.EndpointLimit;
import com.books.api.service.RateLimitingService;
import com.books.api.service.RateLimitingService.RateLimitInfo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for {@link RateLimitingFilter}.
 * Verifies the filter's behavior with different rate limit configurations.
 *
 * @author books
 */
@ExtendWith(MockitoExtension.class)
public class RateLimitingFilterTest {

    @Mock
    private RateLimitingConfig rateLimitingConfig;

    @Mock
    private RateLimitingService rateLimitingService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter printWriter;

    @InjectMocks
    private RateLimitingFilter rateLimitingFilter;

    private EndpointLimit endpointLimit;

    @BeforeEach
    void setUp() throws IOException {
        // Basic test configuration
        endpointLimit = new EndpointLimit();
        endpointLimit.setLimit(10);
        endpointLimit.setRefreshPeriod(60);
        endpointLimit.setTimeUnit(TimeUnit.SECONDS);

        lenient().when(rateLimitingConfig.isEnabled()).thenReturn(true);
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(rateLimitingConfig.getStrategy()).thenReturn("IP_ADDRESS");
        lenient().when(request.getContextPath()).thenReturn("");
        lenient().when(request.getServletPath()).thenReturn("");
        lenient().when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    @DisplayName("Should skip rate limiting when disabled")
    void shouldSkipRateLimitingWhenDisabled() throws ServletException, IOException {
        // Given
        when(rateLimitingConfig.isEnabled()).thenReturn(false);

        // When
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(rateLimitingService);
    }

    @Test
    @DisplayName("Should allow request when within rate limit")
    void shouldAllowRequestWhenWithinRateLimit() throws ServletException, IOException {
        // Given
        String path = "/api/authors";
        when(request.getRequestURI()).thenReturn(path);

        // Configure the rate limiting service
        when(rateLimitingService.findEndpointLimit(path)).thenReturn(endpointLimit);
        when(rateLimitingService.isRequestAllowed(anyString(), eq(endpointLimit))).thenReturn(true);

        // When
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    @DisplayName("Should reject request when exceeds rate limit")
    void shouldRejectRequestWhenExceedsRateLimit() throws ServletException, IOException {
        // Given
        String path = "/api/authors";
        when(request.getRequestURI()).thenReturn(path);
        when(response.getWriter()).thenReturn(printWriter);

        // Configure the rate limiting service
        when(rateLimitingService.findEndpointLimit(path)).thenReturn(endpointLimit);
        when(rateLimitingService.isRequestAllowed(anyString(), eq(endpointLimit))).thenReturn(false);

        // When
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(printWriter).write("Rate limit exceeded. Please try again later.");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Should add rate limit headers when configured")
    void shouldAddRateLimitHeadersWhenConfigured() throws ServletException, IOException {
        // Given
        String path = "/api/authors";
        when(request.getRequestURI()).thenReturn(path);

        // Configure the rate limiting service
        when(rateLimitingService.findEndpointLimit(path)).thenReturn(endpointLimit);
        when(rateLimitingService.isRequestAllowed(anyString(), eq(endpointLimit))).thenReturn(true);
        when(rateLimitingConfig.isResponseHeaders()).thenReturn(true);

        // Mock for rate limit information
        RateLimitInfo rateLimitInfo = new RateLimitInfo(10, 5, System.currentTimeMillis() + 30000);
        when(rateLimitingService.getRateLimitInfo(anyString(), eq(endpointLimit))).thenReturn(rateLimitInfo);

        // When
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).addHeader(eq("X-RateLimit-Limit"), anyString());
        verify(response).addHeader(eq("X-RateLimit-Remaining"), anyString());
        verify(response).addHeader(eq("X-RateLimit-Reset"), anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should use IP-based rate limiting strategy by default")
    void shouldUseIpBasedRateLimitingStrategyByDefault() throws ServletException, IOException {
        // Given
        String path = "/api/authors";
        String ipAddress = "192.168.1.1";
        when(request.getRequestURI()).thenReturn(path);
        when(request.getRemoteAddr()).thenReturn(ipAddress);

        // Configure the rate limiting service
        when(rateLimitingService.findEndpointLimit(path)).thenReturn(endpointLimit);
        when(rateLimitingConfig.getStrategy()).thenReturn("IP_ADDRESS");

        // Capture the rate limit key
        when(rateLimitingService.isRequestAllowed(anyString(), eq(endpointLimit))).thenReturn(true);

        // When
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimitingService).isRequestAllowed(eq(ipAddress + ":" + path), eq(endpointLimit));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should generate correct key for USER strategy")
    void shouldGenerateCorrectKeyForUserStrategy() throws ServletException, IOException {
        // Given
        String path = "/api/authors";
        when(request.getRequestURI()).thenReturn(path);
        when(rateLimitingConfig.getStrategy()).thenReturn("USER");

        // Configure the rate limiting service
        when(rateLimitingService.findEndpointLimit(path)).thenReturn(endpointLimit);
        when(rateLimitingService.isRequestAllowed(eq("user:" + path), eq(endpointLimit))).thenReturn(true);

        // When
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimitingService).isRequestAllowed(eq("user:" + path), eq(endpointLimit));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should generate correct key for TOKEN strategy with Authorization header")
    void shouldGenerateCorrectKeyForTokenStrategyWithAuth() throws ServletException, IOException {
        // Given
        String path = "/api/authors";
        String token = "Bearer abc123";
        when(request.getRequestURI()).thenReturn(path);
        when(rateLimitingConfig.getStrategy()).thenReturn("TOKEN");
        when(request.getHeader("Authorization")).thenReturn(token);

        // Configure the rate limiting service
        when(rateLimitingService.findEndpointLimit(path)).thenReturn(endpointLimit);
        when(rateLimitingService.isRequestAllowed(eq(token + ":" + path), eq(endpointLimit))).thenReturn(true);

        // When
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimitingService).isRequestAllowed(eq(token + ":" + path), eq(endpointLimit));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should generate correct key for TOKEN strategy without Authorization header")
    void shouldGenerateCorrectKeyForTokenStrategyWithoutAuth() throws ServletException, IOException {
        // Given
        String path = "/api/authors";
        when(request.getRequestURI()).thenReturn(path);
        when(rateLimitingConfig.getStrategy()).thenReturn("TOKEN");
        when(request.getHeader("Authorization")).thenReturn(null);

        // Configure the rate limiting service
        when(rateLimitingService.findEndpointLimit(path)).thenReturn(endpointLimit);
        when(rateLimitingService.isRequestAllowed(eq("anonymous:" + path), eq(endpointLimit))).thenReturn(true);

        // When
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimitingService).isRequestAllowed(eq("anonymous:" + path), eq(endpointLimit));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should use IP-based rate limiting for unrecognized strategy")
    void shouldUseIpBasedRateLimitingForUnrecognizedStrategy() throws ServletException, IOException {
        // Given
        String path = "/api/authors";
        String ipAddress = "192.168.1.1";
        when(request.getRequestURI()).thenReturn(path);
        when(request.getRemoteAddr()).thenReturn(ipAddress);
        when(rateLimitingConfig.getStrategy()).thenReturn("UNKNOWN_STRATEGY");

        // Configure the rate limiting service
        when(rateLimitingService.findEndpointLimit(path)).thenReturn(endpointLimit);
        when(rateLimitingService.isRequestAllowed(eq(ipAddress + ":" + path), eq(endpointLimit))).thenReturn(true);

        // When
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimitingService).isRequestAllowed(eq(ipAddress + ":" + path), eq(endpointLimit));
        verify(filterChain).doFilter(request, response);
    }

}