package com.books.api.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.books.api.service.ApiAuditService;
import com.books.domain.model.ApiAuditLog;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

/**
 * Unit tests for the API audit filter.
 * Verifies that the filter correctly intercepts requests and logs audit
 * information.
 *
 * @author books-authors-api
 */
@ExtendWith(MockitoExtension.class)
public class ApiAuditFilterTest {

    @Mock
    private ApiAuditService apiAuditService;

    @Mock
    private RateLimitingFilter rateLimitingFilter;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private ApiAuditFilter apiAuditFilter;

    private MockHttpServletRequest mockRequest;
    private MockHttpServletResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockRequest = new MockHttpServletRequest();
        mockRequest.setMethod("GET");
        mockRequest.setRequestURI("/api/authors");
        mockRequest.setRemoteAddr("127.0.0.1");

        mockResponse = new MockHttpServletResponse();
        mockResponse.setStatus(200);
    }

    @Test
    @DisplayName("Should process the request and create an audit log entry")
    void shouldProcessRequestAndCreateAuditLog() throws ServletException, IOException {
        // Given
        when(apiAuditService.createAuditLog(any(ContentCachingRequestWrapper.class),
                any(ContentCachingResponseWrapper.class), anyLong(), anyBoolean()))
                .thenReturn(new ApiAuditLog());

        // When
        apiAuditFilter.doFilterInternal(mockRequest, mockResponse, filterChain);

        // Then
        verify(filterChain).doFilter(any(ContentCachingRequestWrapper.class), any(ContentCachingResponseWrapper.class));
        verify(apiAuditService).createAuditLog(any(ContentCachingRequestWrapper.class),
                any(ContentCachingResponseWrapper.class), anyLong(), eq(false));
    }

    @Test
    @DisplayName("Should not filter excluded paths")
    void shouldNotFilterExcludedPaths() {
        // Given
        mockRequest.setRequestURI("/actuator/health");

        // When
        boolean shouldNotFilter = apiAuditFilter.shouldNotFilter(mockRequest);

        // Then
        assertTrue(shouldNotFilter);

        // Given
        mockRequest.setRequestURI("/swagger-ui/index.html");

        // When
        shouldNotFilter = apiAuditFilter.shouldNotFilter(mockRequest);

        // Then
        assertTrue(shouldNotFilter);

        // Given
        mockRequest.setRequestURI("/api/authors");

        // When
        shouldNotFilter = apiAuditFilter.shouldNotFilter(mockRequest);

        // Then
        assertFalse(shouldNotFilter);
    }

    private void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true, but was false");
        }
    }

    private void assertFalse(boolean condition) {
        if (condition) {
            throw new AssertionError("Expected condition to be false, but was true");
        }
    }
}