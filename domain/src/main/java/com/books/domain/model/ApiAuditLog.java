package com.books.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity that represents an API request audit log.
 * Stores detailed information about API requests for monitoring and analysis
 * purposes.
 *
 * @author books-authors-api
 */
@Entity
@Table(name = "request_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The HTTP method used in the request (GET, POST, PUT, DELETE, etc.)
     */
    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    /**
     * The endpoint path that was accessed
     */
    @Column(nullable = false)
    private String endpoint;

    /**
     * Query parameters from the request URL
     */
    @Column(name = "query_params", length = 1024)
    private String queryParams;

    /**
     * Request headers (selected ones, may exclude sensitive information)
     */
    @Column(name = "request_headers", length = 2048)
    private String requestHeaders;

    /**
     * Client IP address
     */
    @Column(name = "client_ip", nullable = false)
    private String clientIp;

    /**
     * User identifier (if authenticated)
     */

    @Column(name = "user_id")
    private String userId;

    /**
     * Session or token identifier
     */
    @Column(name = "session_id")
    private String sessionId;

    /**
     * HTTP status code of the response
     */
    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    /**
     * Timestamp when the request was received
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Request processing time in milliseconds
     */

    @Column(name = "processing_time_ms", nullable = false)
    private Long processingTimeMs;

    /**
     * Request body (may be truncated or sanitized)
     */
    @Column(name="request_body",length = 4096)
    private String requestBody;

    /**
     * Response body (may be truncated or sanitized)
     */
    @Column(name="response_body",length = 4096)
    private String responseBody;

    /**
     * Flag indicating if the request exceeded rate limits
     */

    @Column(name = "rate_limit_exceeded")
    private Boolean rateLimitExceeded;

    /**
     * Additional information or context about the request
     */
    @Column(name = "additional_info", length = 1024)
    private String additionalInfo;
}