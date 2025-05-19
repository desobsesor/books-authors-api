package com.books.api.security;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for {@link JwtTokenFilter}.
 * Verifies the filter behavior with different types of JWT tokens.
 *
 * @author books
 */
@ExtendWith(MockitoExtension.class)
public class JwtTokenFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter printWriter;

    private JwtTokenFilter jwtTokenFilter;
    private final String secretKey = "testSecretKeyWithAtLeast256BitsForHS512SignatureAlgorithm";

    @BeforeEach
    void setUp() throws IOException {
        lenient().when(response.getWriter()).thenReturn(printWriter);

        jwtTokenFilter = new JwtTokenFilter(secretKey);
    }

    @Test
    @DisplayName("Should continue filter chain when there is no authorization token")
    void shouldContinueFilterChainWhenNoAuthorizationHeader() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtTokenFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoMoreInteractions(response);
    }

    @Test
    @DisplayName("Should continue filter chain when header does not start with 'Bearer '")
    void shouldContinueFilterChainWhenHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Token xyz");

        // When
        jwtTokenFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoMoreInteractions(response);
    }

    @Test
    @DisplayName("Should send error when JWT token has expired")
    void shouldSendErrorWhenTokenIsExpired() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer expired-token");
        lenient().when(response.getWriter()).thenReturn(printWriter);

        ExpiredJwtException exception = mock(ExpiredJwtException.class);
        try (var jwtsStaticMock = mockStatic(Jwts.class)) {
            jwtsStaticMock.when(Jwts::parser).thenThrow(exception);

            // When
            jwtTokenFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            verify(filterChain, never()).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("Should send error when JWT token is malformed")
    void shouldSendErrorWhenTokenIsMalformed() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer malformed-token");
        lenient().when(response.getWriter()).thenReturn(printWriter);

        MalformedJwtException exception = mock(MalformedJwtException.class);
        try (var jwtsStaticMock = mockStatic(Jwts.class)) {
            jwtsStaticMock.when(Jwts::parser).thenThrow(exception);

            // When
            jwtTokenFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            verify(filterChain, never()).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("Should send error when JWT token signature is invalid")
    void shouldSendErrorWhenTokenSignatureIsInvalid() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-signature-token");
        lenient().when(response.getWriter()).thenReturn(printWriter);

        SignatureException exception = mock(SignatureException.class);
        try (var jwtsStaticMock = mockStatic(Jwts.class)) {
            jwtsStaticMock.when(Jwts::parser).thenThrow(exception);

            // When
            jwtTokenFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            verify(filterChain, never()).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("Should continue filter chain when JWT token is valid")
    void shouldContinueFilterChainWhenTokenIsValid() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");

        // Mock for JWT parser and Claims
        try (var jwtsStaticMock = mockStatic(Jwts.class)) {
            var parser = mock(io.jsonwebtoken.JwtParser.class);
            var jws = mock(io.jsonwebtoken.Jws.class);
            var claims = mock(Claims.class);

            jwtsStaticMock.when(Jwts::parser).thenReturn(parser);
            when(parser.setSigningKey(anyString())).thenReturn(parser);
            when(parser.parseClaimsJws(anyString())).thenReturn(jws);
            when(jws.getBody()).thenReturn(claims);

            // When
            jwtTokenFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            verifyNoMoreInteractions(response);
        }
    }
}