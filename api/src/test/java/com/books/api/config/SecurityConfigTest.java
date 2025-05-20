package com.books.api.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.AssertionErrors.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.cors.CorsConfigurationSource;

import com.books.api.security.JwtTokenFilter;
import com.books.api.security.RateLimitingFilter;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { SecurityConfig.class, SecurityConfigTest.TestConfig.class })
public class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RateLimitingFilter rateLimitingFilter() {
            return new RateLimitingFilter(null, null);
        }

        @Bean
        public JwtTokenFilter jwtTokenFilter() {
            return mock(JwtTokenFilter.class);
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            return request -> null;
        }
    }

    @Test
    @DisplayName("CorsConfigurationSource bean should exist in Spring context")
    void testCorsConfigurationSourceBeanExists() {
        assertNotNull(corsConfigurationSource,
                "CorsConfigurationSource bean should be present in Spring context");
    }

    @Test
    @DisplayName("Should properly configure SecurityFilterChain")
    void testSecurityFilterChainIntegration() throws Exception {
        SecurityFilterChain filterChain = securityConfig.securityFilterChain(null);
        assertNotNull(filterChain, "SecurityFilterChain no debe ser null");
        assertTrue("CorsConfigurationSource bean should be present", corsConfigurationSource != null);
    }
}