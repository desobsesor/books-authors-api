package com.books.api.service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.books.api.config.ApiAuditConfig;
import com.books.domain.model.ApiAuditLog;
import com.books.domain.repository.ApiAuditLogRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for handling API audit logging functionality.
 * Processes and stores detailed information about API requests and responses.
 *
 * @author books-authors-api
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiAuditService {

    private final ApiAuditLogRepository apiAuditLogRepository;
    private final ApiAuditConfig apiAuditConfig;

    /**
     * Creates an audit log entry for an API request/response
     *
     * @param request           The HTTP request
     * @param response          The HTTP response
     * @param executionTimeMs   The request processing time in milliseconds
     * @param rateLimitExceeded Whether the request exceeded rate limits
     * @return The created ApiAuditLog entity
     */
    public ApiAuditLog createAuditLog(
            ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response,
            long executionTimeMs,
            boolean rateLimitExceeded) {

        try {
            ApiAuditLog auditLog = ApiAuditLog.builder()
                    .httpMethod(request.getMethod())
                    .endpoint(request.getRequestURI())
                    .queryParams(request.getQueryString())
                    .requestHeaders(extractHeadersAsString(request))
                    .clientIp(request.getRemoteAddr())
                    .userId(extractUserId(request))
                    .sessionId(extractSessionId(request))
                    .statusCode(response.getStatus())
                    .timestamp(LocalDateTime.now())
                    .processingTimeMs(executionTimeMs)
                    .requestBody(extractRequestBody(request))
                    .responseBody(extractResponseBody(response))
                    .rateLimitExceeded(rateLimitExceeded)
                    .build();

            return apiAuditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Error creating audit log", e);
            return null;
        }
    }

    /**
     * Searches for audit logs based on the provided specification
     *
     * @param spec     The specification for filtering
     * @param pageable The pagination information
     * @return A page of matching audit logs
     */
    public Page<ApiAuditLog> searchAuditLogs(Specification<ApiAuditLog> spec, Pageable pageable) {
        return apiAuditLogRepository.findAll(spec, pageable);
    }

    /**
     * Keeps a record of rate limit violations
     *
     * @param auditLog The audit log of the violation
     * @return The saved audit log
     */
    public ApiAuditLog saveRateLimitViolation(ApiAuditLog auditLog) {
        try {
            if (apiAuditConfig.isDetailedRateLimitLogging()) {
                log.warn("Rate limit violation detected: {} {} from IP: {}",
                        auditLog.getHttpMethod(), auditLog.getEndpoint(), auditLog.getClientIp());
            }
            return apiAuditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Error saving rate limit violation", e);
            return null;
        }
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
        // For now, we'll use a placeholder implementation
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

    /**
     * Extracts headers from the request as a string
     *
     * @param request The HTTP request
     * @return Headers as a string
     */
    private String extractHeadersAsString(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                // Skip sensitive headers
                if (!isSensitiveHeader(headerName)) {
                    headers.put(headerName, request.getHeader(headerName));
                }
            }
        }

        return headers.toString();
    }

    /**
     * Checks if a header is sensitive and should not be logged
     *
     * @param headerName The header name
     * @return true if the header is sensitive
     */
    private boolean isSensitiveHeader(String headerName) {
        String lowerCaseHeader = headerName.toLowerCase();
        return lowerCaseHeader.contains("password") ||
                lowerCaseHeader.contains("secret") ||
                lowerCaseHeader.contains("token") ||
                lowerCaseHeader.contains("authorization") ||
                lowerCaseHeader.contains("cookie");
    }

    /**
     * Extracts the request body
     *
     * @param request The HTTP request
     * @return The request body as a string
     */
    private String extractRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            try {
                String contentString = new String(content, request.getCharacterEncoding());
                // Truncate if too long
                return contentString.length() <= 4000 ? contentString
                        : contentString.substring(0, 4000) + "... (truncated)";
            } catch (UnsupportedEncodingException e) {
                log.error("Error extracting request body", e);
                return "[Error extracting request body]";
            }
        }
        return null;
    }

    /**
     * Extracts the response body
     *
     * @param response The HTTP response
     * @return The response body as a string
     */
    private String extractResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            try {
                String contentString = new String(content, response.getCharacterEncoding());
                // Truncate if too long
                return contentString.length() <= 4000 ? contentString
                        : contentString.substring(0, 4000) + "... (truncated)";
            } catch (UnsupportedEncodingException e) {
                log.error("Error extracting response body", e);
                return "[Error extracting response body]";
            }
        }
        return null;
    }
}