package com.books.api.service;

import com.books.api.config.RateLimitingConfig;
import com.books.api.config.RateLimitingConfig.EndpointLimit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service that manages rate limiting functionality.
 * Provides methods to check if requests are allowed based on configured limits.
 *
 * @author books
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitingService {

    private final RateLimitingConfig rateLimitingConfig;

    // Cache to store request counts per key (IP address, user, or token)
    private final Map<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();

    /**
     * Checks if a request is allowed based on rate limits for the given key and
     * endpoint
     *
     * @param key           The rate limiting key (IP, user, token + path)
     * @param endpointLimit The endpoint limit configuration
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean isRequestAllowed(String key, EndpointLimit endpointLimit) {
        if (!rateLimitingConfig.isEnabled()) {
            return true;
        }

        RequestCounter counter = requestCounters.computeIfAbsent(key, k -> new RequestCounter());
        return counter.incrementAndCheckLimit(endpointLimit.getLimit(),
                endpointLimit.getRefreshPeriod(),
                endpointLimit.getTimeUnit());
    }

    /**
     * Find the appropriate endpoint limit configuration for the given path
     *
     * @param path The request path
     * @return The matching endpoint limit or default settings
     */
    public EndpointLimit findEndpointLimit(String path) {
        return rateLimitingConfig.getEndpoints().stream()
                .filter(endpoint -> path.matches(endpoint.getPattern()))
                .findFirst()
                .orElse(new EndpointLimit() {
                    {
                        setLimit(rateLimitingConfig.getDefaultSettings().getLimit());
                        setRefreshPeriod(rateLimitingConfig.getDefaultSettings().getRefreshPeriod());
                        setTimeUnit(rateLimitingConfig.getDefaultSettings().getTimeUnit());
                    }
                });
    }

    /**
     * Get rate limit information for a specific key
     *
     * @param key           The rate limiting key
     * @param endpointLimit The endpoint limit configuration
     * @return RateLimitInfo containing current limit, remaining requests and reset
     *         time
     */
    public RateLimitInfo getRateLimitInfo(String key, EndpointLimit endpointLimit) {
        RequestCounter counter = requestCounters.get(key);
        if (counter == null) {
            return new RateLimitInfo(endpointLimit.getLimit(), endpointLimit.getLimit(), System.currentTimeMillis());
        }

        return new RateLimitInfo(
                endpointLimit.getLimit(),
                Math.max(0, endpointLimit.getLimit() - counter.getCount()),
                counter.getResetTimeMillis());
    }

    /**
     * Data class to hold rate limit information
     */
    public record RateLimitInfo(int limit, int remaining, long resetTimeMillis) {
    }

    /**
     * Helper class to track request counts and reset times
     */
    private static class RequestCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private long resetTimeMillis = System.currentTimeMillis();

        /**
         * Increment the counter and check if the limit has been exceeded
         */
        public synchronized boolean incrementAndCheckLimit(int limit, int refreshPeriod, TimeUnit timeUnit) {
            long currentTimeMillis = System.currentTimeMillis();

            // Reset counter if refresh period has elapsed
            if (currentTimeMillis > resetTimeMillis) {
                count.set(0);
                resetTimeMillis = currentTimeMillis + timeUnit.toMillis(refreshPeriod);
            }

            // Increment counter and check against limit
            return count.incrementAndGet() <= limit;
        }

        /**
         * Get the current count
         */
        public int getCount() {
            return count.get();
        }

        /**
         * Get the reset time in milliseconds
         */
        public long getResetTimeMillis() {
            return resetTimeMillis;
        }
    }
}