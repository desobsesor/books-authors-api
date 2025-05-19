package com.books.api.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.books.api.security.JwtTokenFilter;
import com.books.api.security.RateLimitingFilter;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { SecurityConfig.class, SecurityConfigTest.TestConfig.class })
public class SecurityConfigTest {

    @Mock
    private HttpSecurity httpSecurity;

    @SuppressWarnings("rawtypes")
    @Mock
    private AuthorizationManagerRequestMatcherRegistry authorizeRequestsRegistry;

    @Autowired
    private SecurityConfig securityConfig;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RateLimitingFilter rateLimitingFilter() {
            return mock(RateLimitingFilter.class);
        }

        @Bean
        public JwtTokenFilter jwtTokenFilter() {
            return mock(JwtTokenFilter.class);
        }
    }

    @Test
    @DisplayName("You should configure the security filter chain correctly.")
    void testSecurityFilterChain() throws Exception {
        // Configure mocks to return the httpSecurity object
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
        when(httpSecurity.addFilterBefore(any(), any())).thenReturn(httpSecurity);

        // Execute the method under test
        SecurityFilterChain filterChain = securityConfig.securityFilterChain(httpSecurity);

        // Check results
        assertNotNull(filterChain);
    }
}