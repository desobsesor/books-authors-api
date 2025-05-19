package com.books.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * General API settings.
 * Manages configuration of API versioning and base paths.
 *
 * @author books
 */
@Configuration
@EnableWebMvc
public class ApiConfig {

    @Value("${CONTEXT_PATH}")
    private String contextPath;

    public String getContextPath() {
        return contextPath;
    }
}