package com.books.api.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.books.api.dto.ApiAuditLogDTO;
import com.books.api.service.ApiAuditService;
import com.books.domain.model.ApiAuditLog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for accessing API audit log information.
 * Provides endpoints for querying and analyzing API usage patterns.
 *
 * @author books-authors-api
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "API Audit", description = "Endpoints for querying API usage and audit logs")
public class ApiAuditController {

    private final ApiAuditService apiAuditService;

    /**
     * Get audit logs with pagination and filtering options
     *
     * @param clientIp       Filter by client IP address
     * @param userId         Filter by user ID
     * @param endpoint       Filter by endpoint path
     * @param statusCode     Filter by HTTP status code
     * @param startTime      Filter by start time
     * @param endTime        Filter by end time
     * @param rateLimitAlert Filter for rate limit alerts only
     * @param pageable       Pagination information
     * @return Page of matching audit logs
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get audit logs with filtering options", description = "Retrieves API audit logs with various filtering criteria and pagination support")
    public ResponseEntity<Page<ApiAuditLogDTO>> getAuditLogs(
            @Parameter(description = "Filter by client IP address") @RequestParam(required = false) String clientIp,

            @Parameter(description = "Filter by user ID") @RequestParam(required = false) String userId,

            @Parameter(description = "Filter by endpoint path") @RequestParam(required = false) String endpoint,

            @Parameter(description = "Filter by HTTP status code") @RequestParam(required = false) Integer statusCode,

            @Parameter(description = "Filter by start time (ISO format)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,

            @Parameter(description = "Filter by end time (ISO format)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,

            @Parameter(description = "Filter for rate limit alerts only") @RequestParam(required = false) Boolean rateLimitAlert,

            Pageable pageable) {

        // Build specification based on filter criteria
        Specification<ApiAuditLog> spec = Specification.where(null);

        if (clientIp != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("clientIp"), clientIp));
        }

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }

        if (endpoint != null) {
            spec = spec.and((root, query, cb) -> cb.like(root.get("endpoint"), "%" + endpoint + "%"));
        }

        if (statusCode != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("statusCode"), statusCode));
        }

        if (startTime != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("timestamp"), startTime));
        }

        if (endTime != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("timestamp"), endTime));
        }

        if (rateLimitAlert != null && rateLimitAlert) {
            spec = spec.and((root, query, cb) -> cb.isTrue(root.get("rateLimitExceeded")));
        }

        // Execute the query with the specification and pagination
        Page<ApiAuditLog> auditLogs = apiAuditService.searchAuditLogs(spec, pageable);

        // Convert entities to DTOs
        Page<ApiAuditLogDTO> auditLogDTOs = auditLogs.map(ApiAuditLogDTO::fromEntity);

        return ResponseEntity.ok(auditLogDTOs);
    }

    /**
     * Get rate limit violation alerts
     *
     * @return List of audit logs for rate limit violations
     */
    @GetMapping("/rate-limit-alerts")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get rate limit violation alerts", description = "Retrieves audit logs for requests that exceeded rate limits")
    public ResponseEntity<List<ApiAuditLogDTO>> getRateLimitAlerts() {
        Specification<ApiAuditLog> spec = (root, query, cb) -> cb.isTrue(root.get("rateLimitExceeded"));

        List<ApiAuditLog> alerts = apiAuditService.searchAuditLogs(spec, Pageable.unpaged()).getContent();

        List<ApiAuditLogDTO> alertDTOs = alerts.stream()
                .map(ApiAuditLogDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(alertDTOs);
    }

    /**
     * Get usage metrics by endpoint
     *
     * @param startTime Start time for the metrics period
     * @param endTime   End time for the metrics period
     * @return Usage metrics grouped by endpoint
     */
    @GetMapping("/metrics/endpoints")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get usage metrics by endpoint", description = "Retrieves usage statistics grouped by endpoint for a specified time period")
    public ResponseEntity<List<ApiAuditLogDTO>> getEndpointMetrics(
            @Parameter(description = "Start time for metrics period (ISO format)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,

            @Parameter(description = "End time for metrics period (ISO format)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        Specification<ApiAuditLog> spec = (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("timestamp"), startTime),
                cb.lessThanOrEqualTo(root.get("timestamp"), endTime));

        // In a real implementation, you would use a custom query to group by endpoint
        // For now, we'll just return the matching logs
        List<ApiAuditLog> metrics = apiAuditService.searchAuditLogs(spec, Pageable.unpaged()).getContent();

        List<ApiAuditLogDTO> metricDTOs = metrics.stream()
                .map(ApiAuditLogDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(metricDTOs);
    }

    /**
     * Get usage metrics by client
     *
     * @param startTime Start time for the metrics period
     * @param endTime   End time for the metrics period
     * @return Usage metrics grouped by client
     */
    @GetMapping("/metrics/clients")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get usage metrics by client", description = "Retrieves usage statistics grouped by client for a specified time period")
    public ResponseEntity<List<ApiAuditLogDTO>> getClientMetrics(
            @Parameter(description = "Start time for metrics period (ISO format)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,

            @Parameter(description = "End time for metrics period (ISO format)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        Specification<ApiAuditLog> spec = (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("timestamp"), startTime),
                cb.lessThanOrEqualTo(root.get("timestamp"), endTime));

        // In a real implementation, you would use a custom query to group by client
        // For now, we'll just return the matching logs
        List<ApiAuditLog> metrics = apiAuditService.searchAuditLogs(spec, Pageable.unpaged()).getContent();

        List<ApiAuditLogDTO> metricDTOs = metrics.stream()
                .map(ApiAuditLogDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(metricDTOs);
    }
}