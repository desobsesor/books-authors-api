package com.books.api.service;

import com.books.domain.model.ApiAuditLog;
import com.books.domain.repository.ApiAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the API audit service.
 * Check the audit log logging and query functionality.
 *
 * @author books-authors-api
 */
@ExtendWith(MockitoExtension.class)
public class ApiAuditServiceTest {

    @Mock
    private ApiAuditLogRepository apiAuditLogRepository;

    @InjectMocks
    private ApiAuditService apiAuditService;

    private MockHttpServletRequest mockRequest;
    private MockHttpServletResponse mockResponse;
    private ContentCachingRequestWrapper requestWrapper;
    private ContentCachingResponseWrapper responseWrapper;

    @BeforeEach
    void setUp() {
        mockRequest = new MockHttpServletRequest();
        mockRequest.setMethod("GET");
        mockRequest.setRequestURI("/api/authors");
        mockRequest.setRemoteAddr("127.0.0.1");
        mockRequest.addHeader("X-User-ID", "test-user");

        mockResponse = new MockHttpServletResponse();
        mockResponse.setStatus(200);

        requestWrapper = new ContentCachingRequestWrapper(mockRequest);
        responseWrapper = new ContentCachingResponseWrapper(mockResponse);
    }

    @Test
    @DisplayName("Debería crear un registro de auditoría correctamente")
    void shouldCreateAuditLogSuccessfully() {
        // Given
        when(apiAuditLogRepository.save(any(ApiAuditLog.class))).thenAnswer(invocation -> {
            ApiAuditLog savedLog = invocation.getArgument(0);
            savedLog.setId(1L);
            return savedLog;
        });

        // When
        ApiAuditLog result = apiAuditService.createAuditLog(requestWrapper, responseWrapper, 100L, false);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("GET", result.getHttpMethod());
        assertEquals("/api/authors", result.getEndpoint());
        assertEquals("127.0.0.1", result.getClientIp());
        assertEquals("test-user", result.getUserId());
        assertEquals(200, result.getStatusCode());
        assertEquals(100L, result.getProcessingTimeMs());
        assertFalse(result.getRateLimitExceeded());
        verify(apiAuditLogRepository).save(any(ApiAuditLog.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Debería buscar registros de auditoría con especificación")
    void shouldSearchAuditLogsWithSpecification() {
        // Given
        Specification<ApiAuditLog> spec = mock(Specification.class);
        Pageable pageable = mock(Pageable.class);
        List<ApiAuditLog> logs = List.of(
                createSampleAuditLog(1L),
                createSampleAuditLog(2L));
        Page<ApiAuditLog> page = new PageImpl<>(logs);

        when(apiAuditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // When
        Page<ApiAuditLog> result = apiAuditService.searchAuditLogs(spec, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(apiAuditLogRepository).findAll(spec, pageable);
    }

    private ApiAuditLog createSampleAuditLog(Long id) {
        return ApiAuditLog.builder()
                .id(id)
                .httpMethod("GET")
                .endpoint("/api/authors")
                .clientIp("127.0.0.1")
                .userId("test-user")
                .statusCode(200)
                .timestamp(LocalDateTime.now())
                .processingTimeMs(100L)
                .rateLimitExceeded(false)
                .build();
    }
}