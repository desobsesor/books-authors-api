package com.books.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Configuration properties for rate limiting functionality.
 * Reads values from application.yml under the 'rate-limiting' prefix.
 *
 * @author books
 */
@Configuration
@ConfigurationProperties(prefix = "rate-limiting")
@Data
public class RateLimitingConfig {

    /**
     * Flag to enable/disable rate limiting globally
     */
    private boolean enabled = true;

    /**
     * Default rate limit settings applied to all endpoints
     * if not specifically configured
     */
    private RateLimitSettings defaultSettings = new RateLimitSettings();

    /**
     * List of endpoint-specific rate limit settings
     */
    private List<EndpointLimit> endpoints = new ArrayList<>();

    /**
     * Strategy to use for rate limiting (IP_ADDRESS, USER, TOKEN)
     */
    private String strategy = "IP_ADDRESS";

    /**
     * Whether to include rate limit headers in responses
     */
    private boolean responseHeaders = true;

    /**
     * Settings for a specific endpoint rate limit
     */
    @Data
    public static class EndpointLimit {
        /**
         * URL pattern to match for this rate limit
         */
        private String pattern;

        /**
         * Maximum number of requests allowed in the refresh period
         */
        private int limit = 100;

        /**
         * Time period after which the limit resets
         */
        private int refreshPeriod = 60;

        /**
         * Time unit for the refresh period (SECONDS, MINUTES, etc.)
         */
        private TimeUnit timeUnit = TimeUnit.SECONDS;
    }

    /**
     * Default rate limit settings
     */
    @Data
    public static class RateLimitSettings {
        /**
         * Maximum number of requests allowed in the refresh period
         */
        private int limit = 100;

        /**
         * Time period after which the limit resets
         */
        private int refreshPeriod = 60;

        /**
         * Time unit for the refresh period (SECONDS, MINUTES, etc.)
         */
        private TimeUnit timeUnit = TimeUnit.SECONDS;
    }
}