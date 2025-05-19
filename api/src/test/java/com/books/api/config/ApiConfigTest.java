package com.books.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit test for ApiConfig class.
 * Tests the configuration properties without loading the full Spring context.
 */
public class ApiConfigTest {

    private ApiConfig apiConfig;
    private MockEnvironment environment;

    @BeforeEach
    public void setUp() {
        // Create a new instance of ApiConfig
        apiConfig = new ApiConfig();

        // Create a mock environment with our test properties
        environment = new MockEnvironment();
        environment.setProperty("CONTEXT_PATH", "/api");

        // Inject the environment into the apiConfig using reflection
        ReflectionTestUtils.setField(apiConfig, "contextPath", "/api");
    }

    @Test
    public void testContextPath() {
        // Test that the context path is correctly retrieved
        String expectedContextPath = "/api";
        assertEquals(expectedContextPath, apiConfig.getContextPath());
    }
}