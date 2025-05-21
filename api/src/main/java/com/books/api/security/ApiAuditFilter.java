package com.books.api.security;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.books.api.service.ApiAuditService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filter that intercepts all API requests to create detailed audit logs.
 * Captures request/response details, timing information, and client data.
 * Positioned with high precedence to ensure it wraps the entire request
 * processing chain.
 *
 * @author books-authors-api
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // Ensure it runs early but after security filters
@RequiredArgsConstructor
@Slf4j
public class ApiAuditFilter extends OncePerRequestFilter {

    private final ApiAuditService apiAuditService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Wrap request and response to cache their content
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        boolean rateLimitExceeded = false;

        try {
            // Continue with the filter chain
            filterChain.doFilter(requestWrapper, responseWrapper);
        } catch (Exception e) {
            // Capture any exceptions that occur during processing
            log.error("Exception during request processing", e);
            throw e;
        } finally {
            // Calculate request processing time
            long executionTime = System.currentTimeMillis() - startTime;

            // Check if rate limit was exceeded (based on response status)
            if (response.getStatus() == 429) { // 429 Too Many Requests
                rateLimitExceeded = true;
            }

            // Create audit log entry
            apiAuditService.createAuditLog(requestWrapper, responseWrapper, executionTime, rateLimitExceeded);

            // Copy content back to the original response
            responseWrapper.copyBodyToResponse();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Skip auditing for specific paths like health checks or static resources
        return path.contains("/actuator/health") ||
                path.contains("/swagger-ui") ||
                path.contains("/v3/api-docs") ||
                path.contains("/favicon.ico");
    }
}