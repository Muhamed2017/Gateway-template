package com.enterprise.gateway.repository;

import com.enterprise.gateway.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for AuditLog entity operations
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Optional<AuditLog> findByRequestId(String requestId);

    Page<AuditLog> findByPartnerId(Long partnerId, Pageable pageable);

    Page<AuditLog> findByPartnerIdAndTimestampBetween(
        Long partnerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.timestamp BETWEEN :start AND :end")
    Page<AuditLog> findByTimestampBetween(
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end, 
        Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.httpStatus >= 400 AND al.timestamp BETWEEN :start AND :end")
    List<AuditLog> findErrorLogsBetween(
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.partnerId = :partnerId AND al.timestamp > :since")
    Long countRequestsByPartnerSince(
        @Param("partnerId") Long partnerId, 
        @Param("since") LocalDateTime since);

    @Query("SELECT AVG(al.durationMs) FROM AuditLog al WHERE al.partnerId = :partnerId")
    Double getAverageResponseTime(@Param("partnerId") Long partnerId);

    @Query("SELECT al.endpoint, COUNT(al) as count FROM AuditLog al " +
           "WHERE al.timestamp BETWEEN :start AND :end " +
           "GROUP BY al.endpoint ORDER BY count DESC")
    List<Object[]> getMostUsedEndpoints(
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end);
}
