package com.books.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the API audit system.
 * Controls various aspects of the audit logging functionality.
 *
 * @author books
 */
@Configuration
@ConfigurationProperties(prefix = "api.audit")
@Data
public class ApiAuditConfig {

    /**
     * Whether the audit system is enabled
     */
    private boolean enabled = true;

    /**
     * Whether to log request bodies
     */
    private boolean logRequestBody = true;

    /**
     * Whether to log response bodies
     */
    private boolean logResponseBody = false;

    /**
     * Maximum size of request/response bodies to log (in characters)
     */
    private int maxBodySize = 4000;

    /**
     * Whether to log headers
     */
    private boolean logHeaders = true;

    /**
     * Whether to log query parameters
     */
    private boolean logQueryParams = true;

    /**
     * List of paths to exclude from audit logging (e.g., health checks, metrics)
     */
    private String[] excludePaths = {
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/favicon.ico"
    };

    /**
     * Whether to enable detailed rate limit violation logging
     */
    private boolean detailedRateLimitLogging = true;

    /**
     * Whether to retain sensitive information in audit logs
     * (should typically be false in production)
     */
    private boolean retainSensitiveInfo = false;
}