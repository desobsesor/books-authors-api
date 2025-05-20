package com.books.api.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.Data;

/**
 * Configuration for Cross-Origin Resource Sharing (CORS)
 * Allows configuring which origins, methods and headers are allowed
 * for cross-origin requests to our API.
 *
 * @author Books API Team
 */
@Configuration
@ConfigurationProperties(prefix = "cors")
@Data
public class CorsConfig {

    /**
     * Flag to enable/disable CORS globally
     */
    private boolean enabled = true;

    /**
     * List of allowed origins (URLs)
     * Example: ["http://localhost:3000", "https://my-app.com"]
     */
    private List<String> allowedOrigins = Arrays.asList("*");

    /**
     * List of allowed HTTP methods
     * Example: ["GET", "POST", "PUT", "DELETE"]
     */
    private List<String> allowedMethods = Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");

    /**
     * List of allowed headers
     */
    private List<String> allowedHeaders = Arrays.asList("Authorization", "Content-Type", "Accept");

    /**
     * List of exposed headers
     */
    private List<String> exposedHeaders = Arrays.asList("X-RateLimit-Limit", "X-RateLimit-Remaining",
            "X-RateLimit-Reset");

    /**
     * Indicates whether credentials are allowed in CORS requests
     */
    private boolean allowCredentials = true;

    /**
     * Maximum time in seconds that preflight request results
     * can be cached
     */
    private long maxAge = 3600;

    /**
     * Configures the URL-based CORS configuration source
     *
     * @return The CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Only apply settings if CORS is enabled
        if (enabled) {
            configuration.setAllowedOrigins(allowedOrigins);
            configuration.setAllowedMethods(allowedMethods);
            configuration.setAllowedHeaders(allowedHeaders);
            configuration.setExposedHeaders(exposedHeaders);
            configuration.setAllowCredentials(allowCredentials);
            configuration.setMaxAge(maxAge);
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}