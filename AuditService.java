package com.enterprise.gateway.service;

import com.enterprise.gateway.dto.GatewayDto;
import com.enterprise.gateway.model.AuditLog;
import com.enterprise.gateway.model.Partner;
import com.enterprise.gateway.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for comprehensive audit logging of all API requests
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional
    public void logRequest(
            Partner partner,
            String method,
            String endpoint,
            String queryParams,
            String requestBody,
            String responseBody,
            Integer httpStatus,
            String clientIp,
            String userAgent,
            Long durationMs,
            String errorMessage,
            String downstreamService,
            Boolean cacheHit) {

        try {
            AuditLog auditLog = AuditLog.builder()
                .partnerId(partner != null ? partner.getId() : null)
                .partnerName(partner != null ? partner.getName() : "Anonymous")
                .requestId(UUID.randomUUID().toString())
                .method(method)
                .endpoint(endpoint)
                .queryParams(queryParams)
                .requestBody(truncate(requestBody, 5000))
                .responseBody(truncate(responseBody, 5000))
                .httpStatus(httpStatus)
                .clientIp(clientIp)
                .userAgent(truncate(userAgent, 500))
                .durationMs(durationMs)
                .errorMessage(truncate(errorMessage, 2000))
                .correlationId(UUID.randomUUID().toString())
                .downstreamService(downstreamService)
                .cacheHit(cacheHit != null ? cacheHit : false)
                .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log saved for request: {} {} - Status: {}", method, endpoint, httpStatus);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Page<GatewayDto.AuditLogResponse> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
            .map(this::mapToAuditLogResponse);
    }

    @Transactional(readOnly = true)
    public Page<GatewayDto.AuditLogResponse> getAuditLogsByPartner(Long partnerId, Pageable pageable) {
        return auditLogRepository.findByPartnerId(partnerId, pageable)
            .map(this::mapToAuditLogResponse);
    }

    @Transactional(readOnly = true)
    public Page<GatewayDto.AuditLogResponse> getAuditLogsByDateRange(
            LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(start, end, pageable)
            .map(this::mapToAuditLogResponse);
    }

    @Transactional(readOnly = true)
    public Long getRequestCountByPartnerSince(Long partnerId, LocalDateTime since) {
        return auditLogRepository.countRequestsByPartnerSince(partnerId, since);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) + "..." : value;
    }

    private GatewayDto.AuditLogResponse mapToAuditLogResponse(AuditLog auditLog) {
        return GatewayDto.AuditLogResponse.builder()
            .id(auditLog.getId())
            .partnerName(auditLog.getPartnerName())
            .requestId(auditLog.getRequestId())
            .method(auditLog.getMethod())
            .endpoint(auditLog.getEndpoint())
            .httpStatus(auditLog.getHttpStatus())
            .clientIp(auditLog.getClientIp())
            .durationMs(auditLog.getDurationMs())
            .timestamp(auditLog.getTimestamp())
            .downstreamService(auditLog.getDownstreamService())
            .cacheHit(auditLog.getCacheHit())
            .build();
    }
}
