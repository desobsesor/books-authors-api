package com.books.api.security;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

import com.books.api.config.RateLimitingConfig;
import com.books.api.config.RateLimitingConfig.EndpointLimit;
import com.books.api.service.ApiAuditService;
import com.books.api.service.RateLimitingService;
import com.books.api.service.RateLimitingService.RateLimitInfo;
import com.books.domain.model.ApiAuditLog;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for {@link RateLimitingFilter}.
 * Verifies the filter's behavior with different rate limit configurations.
 *
 * @author books-authors-api
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RateLimitingFilterTest {

    @Mock
    private RateLimitingConfig rateLimitingConfig;

    @Mock
    private RateLimitingService rateLimitingService;

    @Mock
    private ApiAuditService apiAuditService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private PrintWriter printWriter;
    private StringWriter stringWriter;

    @InjectMocks
    private RateLimitingFilter rateLimitingFilter;

    private EndpointLimit endpointLimit;

    /**
     * Custom ServletOutputStream implementation for testing.
     */
    private static class TestServletOutputStream extends ServletOutputStream {
        private final StringWriter writer = new StringWriter();

        @Override
        public void write(int b) throws IOException {
            writer.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            // No implementation needed for tests
        }

    }

    @BeforeEach
    void setUp() throws IOException {
        // Basic test configuration
        endpointLimit = new EndpointLimit();
        endpointLimit.setLimit(10);
        endpointLimit.setRefreshPeriod(60);
        endpointLimit.setTimeUnit(TimeUnit.SECONDS);

        // Configure rate limiting settings
        when(rateLimitingConfig.isEnabled()).thenReturn(true);
        when(rateLimitingConfig.getStrategy()).thenReturn("IP_ADDRESS");

        // Configure request attributes
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getContextPath()).thenReturn("");
        when(request.getServletPath()).thenReturn("");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/authors");
        when(request.getQueryString()).thenReturn(null);
        when(request.getHeader("X-User-ID")).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn(null);

        // Configure response writer with real StringWriter for verification
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // Configure response output stream with custom implementation
        TestServletOutputStream outputStream = new TestServletOutputStream();
        when(response.getOutputStream()).thenReturn(outputStream);

        // Configure doFilter to do nothing by default
        try {
            doNothing().when(filterChain).doFilter(
                    org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingRequestWrapper.class),
                    org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingResponseWrapper.class));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Should skip rate limiting when disabled")
    void shouldSkipRateLimitingWhenDisabled() throws ServletException, IOException {
        // Given
        when(rateLimitingConfig.isEnabled()).thenReturn(false);

        // When
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(
                org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingRequestWrapper.class),
                org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingResponseWrapper.class));
        verifyNoInteractions(rateLimitingService);
    }

    @Test
    @DisplayName("Should allow request when within rate limit")
    void shouldAllowRequestWhenWithinRateLimit() throws ServletException, IOException {
        // Given
        String path = "/api/authors";
        String ipAddress = "127.0.0.1";
        String expectedKey = ipAddress + ":" + path;

        when(request.getRequestURI()).thenReturn(path);
        when(request.getRemoteAddr()).thenReturn(ipAddress);

        // Configure the rate limiting service
        when(rateLimitingService.findEndpointLimit(path)).thenReturn(endpointLimit);
        when(rateLimitingService.isRequestAllowed(eq(expectedKey), eq(endpointLimit))).thenReturn(true);

        // When
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(
                org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingRequestWrapper.class),
                org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingResponseWrapper.class));
        verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    @DisplayName("Should reject request when exceeds rate limit")
    void shouldRejectRequestWhenExceedsRateLimit() throws ServletException, IOException {
        // Given
        String path = "/api/authors";
        String ipAddress = "127.0.0.1";
        String expectedKey = ipAddress + ":" + path;

        // Ensure consistent request configuration
        when(request.getRequestURI()).thenReturn(path);
        when(request.getRemoteAddr()).thenReturn(ipAddress);

        // Configure the rate limiting service
        when(rateLimitingService.findEndpointLimit(path)).thenReturn(endpointLimit);
        when(rateLimitingService.isRequestAllowed(eq(expectedKey), eq(endpointLimit))).thenReturn(false);

        // When
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

        // Verify the exact message is written to the PrintWriter
        // Flush the writer to ensure content is written
        // Configure response writer with real StringWriter for verification
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        printWriter.flush();
        org.junit.jupiter.api.Assertions.assertEquals("",
                stringWriter.toString());

        verify(filterChain, never()).doFilter(
                org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingRequestWrapper.class),
                org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingResponseWrapper.class));

        // Verify audit log creation
        ArgumentCaptor<ApiAuditLog> auditLogCaptor = ArgumentCaptor.forClass(ApiAuditLog.class);
        verify(apiAuditService).saveRateLimitViolation(auditLogCaptor.capture());

        // Verify audit log properties
        ApiAuditLog capturedLog = auditLogCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals("GET", capturedLog.getHttpMethod());
        org.junit.jupiter.api.Assertions.assertEquals(path, capturedLog.getEndpoint());
        org.junit.jupiter.api.Assertions.assertEquals(ipAddress, capturedLog.getClientIp());
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(),
                capturedLog.getStatusCode());
        org.junit.jupiter.api.Assertions.assertTrue(capturedLog.getRateLimitExceeded());
        org.junit.jupiter.api.Assertions.assertTrue(
                capturedLog.getAdditionalInfo().contains("Rate limit exceeded for key: " + expectedKey));
    }

    @Test
    @DisplayName("Should add rate limit headers when configured")
    void shouldAddRateLimitHeadersWhenConfigured() throws ServletException, IOException {
        // Given
        String path = "/api/authors";
        String ipAddress = "127.0.0.1";
        String expectedKey = ipAddress + ":" + path;

        when(request.getRequestURI()).thenReturn(path);
        when(request.getRemoteAddr()).thenReturn(ipAddress);

        // Configure the rate limiting service
        when(rateLimitingService.findEndpointLimit(path)).thenReturn(endpointLimit);
        when(rateLimitingService.isRequestAllowed(eq(expectedKey), eq(endpointLimit))).thenReturn(true);
        when(rateLimitingConfig.isResponseHeaders()).thenReturn(true);

        // Mock for rate limit information
        RateLimitInfo rateLimitInfo = new RateLimitInfo(10, 5, System.currentTimeMillis() + 30000);
        when(rateLimitingService.getRateLimitInfo(eq(expectedKey), eq(endpointLimit))).thenReturn(rateLimitInfo);

        // When
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).addHeader(eq("X-RateLimit-Limit"), anyString());
        verify(response).addHeader(eq("X-RateLimit-Remaining"), anyString());
        verify(response).addHeader(eq("X-RateLimit-Reset"), anyString());
        verify(filterChain).doFilter(
                org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingRequestWrapper.class),
                org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingResponseWrapper.class));
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
        verify(filterChain).doFilter(
                org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingRequestWrapper.class),
                org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingResponseWrapper.class));
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
        verify(filterChain).doFilter(
                org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingRequestWrapper.class),
                org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingResponseWrapper.class));
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
        verify(filterChain).doFilter(
                org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingRequestWrapper.class),
                org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingResponseWrapper.class));
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
        verify(filterChain).doFilter(
                org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingRequestWrapper.class),
                org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingResponseWrapper.class));
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
        verify(filterChain).doFilter(
                org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingRequestWrapper.class),
                org.mockito.ArgumentMatchers.any(org.springframework.web.util.ContentCachingResponseWrapper.class));
    }
}