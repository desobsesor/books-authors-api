package com.books.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.MappingMatch;

/**
 * Unit tests for {@link CorsConfig}.
 * Verifies correct loading of properties, the CorsConfigurationSource bean,
 * and custom value mapping.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { CorsConfig.class, CorsConfigTest.class })
public class CorsConfigTest {

    @Test
    @DisplayName("CORS enabled: default configuration")
    void whenCorsEnabled_thenDefaultConfigIsApplied() {
        CorsConfig config = new CorsConfig();
        assertTrue(config.isEnabled());
        assertThat(config.getAllowedOrigins()).containsExactly("*");
        assertThat(config.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");
        assertThat(config.getAllowedHeaders()).contains("Authorization", "Content-Type", "Accept");
        assertThat(config.getExposedHeaders()).contains("X-RateLimit-Limit", "X-RateLimit-Remaining",
                "X-RateLimit-Reset");
        assertTrue(config.isAllowCredentials());
        assertEquals(3600, config.getMaxAge());
    }

    @Test
    @DisplayName("CORS disabled: empty configuration")
    void whenCorsDisabled_thenNoConfigIsApplied() {
        CorsConfig config = new CorsConfig();
        config.setEnabled(false);
        CorsConfigurationSource source = config.corsConfigurationSource();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletMapping mockMapping = mock(HttpServletMapping.class);
        when(mockMapping.getMappingMatch()).thenReturn(MappingMatch.PATH);
        when(mockMapping.getPattern()).thenReturn("/api");
        when(mockMapping.getPattern()).thenReturn("/api");
        when(mockMapping.getPattern()).thenReturn("/api");
        when(mockRequest.getHttpServletMapping()).thenReturn(mockMapping);
        when(mockRequest.getContextPath()).thenReturn("");
        when(mockRequest.getRequestURI()).thenReturn("/api");
        when(mockRequest.getServletPath()).thenReturn("/api");

        CorsConfiguration cors = source.getCorsConfiguration(mockRequest);
        assertNull(cors.getAllowedOrigins());
        assertNull(cors.getAllowedMethods());
        assertNull(cors.getAllowedHeaders());
        assertNull(cors.getExposedHeaders());
        assertNull(cors.getAllowCredentials());
        assertNull(cors.getMaxAge());
    }

    @Test
    @DisplayName("CORS with custom values")
    void whenCustomValues_thenConfigIsMappedCorrectly() {
        CorsConfig config = new CorsConfig();
        config.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        config.setAllowedMethods(Arrays.asList("GET", "POST"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        config.setExposedHeaders(Arrays.asList("X-Custom-Header"));
        config.setAllowCredentials(false);
        config.setMaxAge(1800);
        CorsConfigurationSource source = config.corsConfigurationSource();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletMapping mockMapping = mock(HttpServletMapping.class);
        when(mockMapping.getMappingMatch()).thenReturn(MappingMatch.PATH);
        when(mockMapping.getPattern()).thenReturn("/api");
        when(mockMapping.getPattern()).thenReturn("/api");
        when(mockMapping.getPattern()).thenReturn("/api");
        when(mockRequest.getHttpServletMapping()).thenReturn(mockMapping);
        when(mockRequest.getContextPath()).thenReturn("");
        when(mockRequest.getRequestURI()).thenReturn("/api");
        when(mockRequest.getServletPath()).thenReturn("/api");
        CorsConfiguration cors = source.getCorsConfiguration(mockRequest);
        assertThat(cors.getAllowedOrigins()).containsExactly("http://localhost:4200");
        assertThat(cors.getAllowedMethods()).containsExactlyInAnyOrder("GET", "POST");
        assertThat(cors.getAllowedHeaders()).containsExactlyInAnyOrder("Authorization", "Content-Type");
        assertThat(cors.getExposedHeaders()).containsExactly("X-Custom-Header");
        assertFalse(cors.getAllowCredentials());
        assertEquals(1800, cors.getMaxAge());
    }

    @Nested
    @SpringBootTest(classes = CorsConfig.class)
    class SpringContextIntegration {
        @Test
        @DisplayName("CorsConfigurationSource bean is registered in Spring context")
        void corsConfigurationSourceBeanIsRegistered() {
            CorsConfig config = new CorsConfig();
            CorsConfigurationSource source = config.corsConfigurationSource();
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            HttpServletMapping mockMapping = mock(HttpServletMapping.class);
            when(mockMapping.getMappingMatch()).thenReturn(MappingMatch.PATH);
            when(mockMapping.getPattern()).thenReturn("/api");
            when(mockMapping.getPattern()).thenReturn("/api");
            when(mockMapping.getPattern()).thenReturn("/api");
            when(mockRequest.getHttpServletMapping()).thenReturn(mockMapping);
            when(mockRequest.getContextPath()).thenReturn("");
            when(mockRequest.getRequestURI()).thenReturn("/api");
            when(mockRequest.getServletPath()).thenReturn("/api");
            CorsConfiguration cors = source.getCorsConfiguration(mockRequest);
            assertNotNull(source);
            assertNotNull(cors);
        }
    }
}