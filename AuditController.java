package com.enterprise.gateway.controller;

import com.enterprise.gateway.dto.GatewayDto;
import com.enterprise.gateway.model.Partner;
import com.enterprise.gateway.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controller for audit log operations
 */
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Audit log retrieval endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all audit logs", description = "Retrieve paginated audit logs (Admin only)")
    public ResponseEntity<GatewayDto.ApiResponse<Page<GatewayDto.AuditLogResponse>>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<GatewayDto.AuditLogResponse> logs = auditService.getAuditLogs(pageable);
        return ResponseEntity.ok(GatewayDto.ApiResponse.success(logs));
    }

    @GetMapping("/my-logs")
    @Operation(summary = "Get own audit logs", description = "Retrieve audit logs for current partner")
    public ResponseEntity<GatewayDto.ApiResponse<Page<GatewayDto.AuditLogResponse>>> getMyAuditLogs(
            @AuthenticationPrincipal Partner partner,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<GatewayDto.AuditLogResponse> logs = auditService.getAuditLogsByPartner(partner.getId(), pageable);
        return ResponseEntity.ok(GatewayDto.ApiResponse.success(logs));
    }

    @GetMapping("/partner/{partnerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get audit logs by partner", description = "Retrieve audit logs for specific partner")
    public ResponseEntity<GatewayDto.ApiResponse<Page<GatewayDto.AuditLogResponse>>> getAuditLogsByPartner(
            @PathVariable Long partnerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<GatewayDto.AuditLogResponse> logs = auditService.getAuditLogsByPartner(partnerId, pageable);
        return ResponseEntity.ok(GatewayDto.ApiResponse.success(logs));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get audit logs by date range", description = "Retrieve audit logs within date range")
    public ResponseEntity<GatewayDto.ApiResponse<Page<GatewayDto.AuditLogResponse>>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<GatewayDto.AuditLogResponse> logs = auditService.getAuditLogsByDateRange(start, end, pageable);
        return ResponseEntity.ok(GatewayDto.ApiResponse.success(logs));
    }
}
