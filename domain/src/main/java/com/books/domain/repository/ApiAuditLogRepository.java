package com.books.domain.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.books.domain.model.ApiAuditLog;

/**
 * Repository for managing ApiAuditLog entities.
 * Provides methods for querying and filtering audit logs.
 *
 * @author books-authors-api
 */
@Repository
public interface ApiAuditLogRepository extends JpaRepository<ApiAuditLog, Long>, JpaSpecificationExecutor<ApiAuditLog> {

    /**
     * Find audit logs by client IP address
     *
     * @param clientIp the client IP address
     * @return list of matching audit logs
     */
    List<ApiAuditLog> findByClientIp(String clientIp);

    /**
     * Find audit logs by user ID
     *
     * @param userId the user ID
     * @return list of matching audit logs
     */
    List<ApiAuditLog> findByUserId(String userId);

    /**
     * Find audit logs by endpoint path
     *
     * @param endpoint the endpoint path
     * @return list of matching audit logs
     */
    List<ApiAuditLog> findByEndpoint(String endpoint);

    /**
     * Find audit logs by HTTP status code
     *
     * @param statusCode the HTTP status code
     * @return list of matching audit logs
     */
    List<ApiAuditLog> findByStatusCode(Integer statusCode);

    /**
     * Find audit logs created within a time range
     *
     * @param startTime the start time
     * @param endTime   the end time
     * @return list of matching audit logs
     */
    List<ApiAuditLog> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find audit logs that exceeded rate limits
     *
     * @return list of matching audit logs
     */
    List<ApiAuditLog> findByRateLimitExceededTrue();

    /**
     * Find audit logs by client IP and within a time range
     *
     * @param clientIp  the client IP address
     * @param startTime the start time
     * @param endTime   the end time
     * @return list of matching audit logs
     */
    List<ApiAuditLog> findByClientIpAndTimestampBetween(String clientIp, LocalDateTime startTime,
            LocalDateTime endTime);
}