package com.books.api.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Test token creation")
    void testCreateToken() {
        String token = jwtTokenProvider.createToken();
        assertNotNull(token, "Token should not be null");
    }

    @Test
    @DisplayName("Test getSecretKey")
    void testGetSecretKey() {
        assertNotNull(jwtTokenProvider.getSecretKey(), "SecretKey should not be null");
    }
}