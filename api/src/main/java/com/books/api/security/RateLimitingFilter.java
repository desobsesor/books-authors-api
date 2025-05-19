package com.books.api.security;

import com.books.api.config.RateLimitingConfig;
import com.books.api.config.RateLimitingConfig.EndpointLimit;
import com.books.api.service.RateLimitingService;
import com.books.api.service.RateLimitingService.RateLimitInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;

/**
 * Filter that implements rate limiting for API endpoints.
 * Limits requests based on configured thresholds and strategies.
 * Uses RateLimitingService to manage rate limit logic.
 *
 * @author books
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingConfig rateLimitingConfig;
    private final RateLimitingService rateLimitingService;
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip rate limiting if disabled
        if (!rateLimitingConfig.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get the request path
        String path = urlPathHelper.getPathWithinApplication(request);

        // Find matching endpoint limit configuration
        EndpointLimit endpointLimit = rateLimitingService.findEndpointLimit(path);

        // Get the key for rate limiting based on strategy
        String key = getRateLimitKey(request, path);

        // Check if request is allowed
        boolean allowed = rateLimitingService.isRequestAllowed(key, endpointLimit);

        // Add rate limit headers if configured
        if (rateLimitingConfig.isResponseHeaders()) {
            addRateLimitHeaders(response, key, endpointLimit);
        }

        if (allowed) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for key: {}, path: {}", key, path);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded. Please try again later.");
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
}