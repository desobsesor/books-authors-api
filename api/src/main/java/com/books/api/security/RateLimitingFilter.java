package com.books.api.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.UrlPathHelper;

import com.books.api.config.RateLimitingConfig;
import com.books.api.config.RateLimitingConfig.EndpointLimit;
import com.books.api.service.ApiAuditService;
import com.books.api.service.RateLimitingService;
import com.books.api.service.RateLimitingService.RateLimitInfo;
import com.books.domain.model.ApiAuditLog;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filter that implements rate limiting for API endpoints.
 * Limits requests based on configured thresholds and strategies.
 * Uses RateLimitingService to manage rate limit logic.
 * Integrated with ApiAuditService to log rate limit violations.
 *
 * @author books-authors-api
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20) // After security filters but before ApiAuditFilter
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingConfig rateLimitingConfig;
    private final RateLimitingService rateLimitingService;
    private final ApiAuditService apiAuditService;
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Ensure we have cacheable request/response for audit logging
        ContentCachingRequestWrapper requestWrapper = request instanceof ContentCachingRequestWrapper
                ? (ContentCachingRequestWrapper) request
                : new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = response instanceof ContentCachingResponseWrapper
                ? (ContentCachingResponseWrapper) response
                : new ContentCachingResponseWrapper(response);

        // Skip rate limiting if disabled
        if (!rateLimitingConfig.isEnabled()) {
            filterChain.doFilter(requestWrapper, responseWrapper);
            return;
        }

        // Get the request path
        String path = urlPathHelper.getPathWithinApplication(requestWrapper);

        // Find matching endpoint limit configuration
        EndpointLimit endpointLimit = rateLimitingService.findEndpointLimit(path);

        // Get the key for rate limiting based on strategy
        String key = getRateLimitKey(requestWrapper, path);

        // Check if request is allowed
        boolean allowed = rateLimitingService.isRequestAllowed(key, endpointLimit);

        // Add rate limit headers if configured
        if (rateLimitingConfig.isResponseHeaders()) {
            addRateLimitHeaders(responseWrapper, key, endpointLimit);
        }

        if (allowed) {
            filterChain.doFilter(requestWrapper, responseWrapper);
            responseWrapper.copyBodyToResponse();
        } else {
            log.warn("Rate limit exceeded for key: {}, path: {}", key, path);
            responseWrapper.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            responseWrapper.getWriter().write("Rate limit exceeded. Please try again later.");

            // Create a special audit log entry for rate limit violation
            ApiAuditLog rateLimitLog = ApiAuditLog.builder()
                    .httpMethod(requestWrapper.getMethod())
                    .endpoint(requestWrapper.getRequestURI())
                    .queryParams(requestWrapper.getQueryString())
                    .clientIp(requestWrapper.getRemoteAddr())
                    .userId(extractUserId(requestWrapper))
                    .sessionId(extractSessionId(requestWrapper))
                    .statusCode(HttpStatus.TOO_MANY_REQUESTS.value())
                    .timestamp(LocalDateTime.now())
                    .processingTimeMs(0L) // Not processed
                    .rateLimitExceeded(true)
                    .additionalInfo("Rate limit exceeded for key: " + key)
                    .build();

            // Save the rate limit violation log
            apiAuditService.saveRateLimitViolation(rateLimitLog);

            // Ensure response body is copied back
            responseWrapper.copyBodyToResponse();
        }
    }

    /**
     * Get the key for rate limiting based on the configured strategy
     *
     * @param request The HTTP request
     * @param path    The request path
     * @return The rate limiting key
     */
    private String getRateLimitKey(HttpServletRequest request, String path) {
        String strategy = rateLimitingConfig.getStrategy();

        return switch (strategy) {
            case "IP_ADDRESS" -> request.getRemoteAddr() + ":" + path;
            case "USER" -> {
                // Assuming user information is available in the security context
                // This would need to be adapted based on your authentication setup
                yield "user:" + path;
            }
            case "TOKEN" -> {
                String token = request.getHeader("Authorization");
                yield (token != null ? token : "anonymous") + ":" + path;
            }
            default -> request.getRemoteAddr() + ":" + path;
        };
    }

    /**
     * Add rate limit headers to the response
     *
     * @param response      The HTTP response
     * @param key           The rate limiting key
     * @param endpointLimit The endpoint limit configuration
     */
    private void addRateLimitHeaders(HttpServletResponse response, String key, EndpointLimit endpointLimit) {
        RateLimitInfo info = rateLimitingService.getRateLimitInfo(key, endpointLimit);

        response.addHeader("X-RateLimit-Limit", String.valueOf(info.limit()));
        response.addHeader("X-RateLimit-Remaining", String.valueOf(info.remaining()));
        response.addHeader("X-RateLimit-Reset", String.valueOf(info.resetTimeMillis()));
    }

    /**
     * Extracts the user ID from the request
     *
     * @param request The HTTP request
     * @return The user ID or null if not available
     */
    private String extractUserId(HttpServletRequest request) {
        // This implementation depends on your authentication mechanism
        // For JWT, you might extract it from the token
        return request.getHeader("X-User-ID");
    }

    /**
     * Extracts the session ID from the request
     *
     * @param request The HTTP request
     * @return The session ID or token
     */
    private String extractSessionId(HttpServletRequest request) {
        // Try to get from session first
        if (request.getSession(false) != null) {
            return request.getSession().getId();
        }

        // Otherwise, try to get from Authorization header (for token-based auth)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Extract the token part
        }

        return null;
    }
}