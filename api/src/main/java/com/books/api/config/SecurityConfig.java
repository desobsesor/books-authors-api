package com.books.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.books.api.security.JwtTokenFilter;
import com.books.api.security.RateLimitingFilter;

/**
 * Security configuration for the application.
 * Configures security filters, authentication, and authorization rules.
 *
 * @author books
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${security.secret-key}")
    private String secretKey;

    private final RateLimitingFilter rateLimitingFilter;

    public SecurityConfig(RateLimitingFilter rateLimitingFilter) {
        this.rateLimitingFilter = rateLimitingFilter;
    }

    /**
     * Configures the security filter chain with JWT authentication and rate
     * limiting.
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/authors/generate-token").permitAll()
                        .requestMatchers("/api/**").permitAll() // .authenticated()
                        .anyRequest().permitAll())
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Apply rate limiting filter first
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                // Then JWT authentication filter
                .addFilterBefore(new JwtTokenFilter(secretKey), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}