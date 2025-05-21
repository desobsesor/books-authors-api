package com.books.api.dto;

import java.time.LocalDateTime;

import com.books.domain.model.ApiAuditLog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for API audit log information.
 * Used for transferring audit data to clients while potentially
 * filtering sensitive information.
 *
 * @author books-authors-api
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiAuditLogDTO {

    private Long id;
    private String httpMethod;
    private String endpoint;
    private String queryParams;
    private String clientIp;
    private String userId;
    private Integer statusCode;
    private LocalDateTime timestamp;
    private Long processingTimeMs;
    private Boolean rateLimitExceeded;
    private String additionalInfo;

    /**
     * Converts an ApiAuditLog entity to a DTO
     *
     * @param auditLog the entity to convert
     * @return the corresponding DTO
     */
    public static ApiAuditLogDTO fromEntity(ApiAuditLog auditLog) {
        return ApiAuditLogDTO.builder()
                .id(auditLog.getId())
                .httpMethod(auditLog.getHttpMethod())
                .endpoint(auditLog.getEndpoint())
                .queryParams(auditLog.getQueryParams())
                .clientIp(auditLog.getClientIp())
                .userId(auditLog.getUserId())
                .statusCode(auditLog.getStatusCode())
                .timestamp(auditLog.getTimestamp())
                .processingTimeMs(auditLog.getProcessingTimeMs())
                .rateLimitExceeded(auditLog.getRateLimitExceeded())
                .additionalInfo(auditLog.getAdditionalInfo())
                .build();
    }
}