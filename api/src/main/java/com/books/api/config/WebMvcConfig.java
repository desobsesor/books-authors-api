package com.books.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

/**
 * Spring MVC configuration that integrates CORS configuration
 * with the rest of the web application.
 *
 * @author Books API Team
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final CorsConfig corsConfig;

    /**
     * Configures CORS rules for the entire application based on
     * the properties defined in the application.yml file.
     *
     * @param registry Spring MVC's CORS registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Only apply settings if CORS is enabled
        if (corsConfig.isEnabled()) {
            registry.addMapping("/**")
                    .allowedOrigins(corsConfig.getAllowedOrigins().toArray(new String[0]))
                    .allowedMethods(corsConfig.getAllowedMethods().toArray(new String[0]))
                    .allowedHeaders(corsConfig.getAllowedHeaders().toArray(new String[0]))
                    .exposedHeaders(corsConfig.getExposedHeaders().toArray(new String[0]))
                    .allowCredentials(corsConfig.isAllowCredentials())
                    .maxAge(corsConfig.getMaxAge());
        }
    }
}