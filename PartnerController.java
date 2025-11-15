package com.enterprise.gateway.controller;

import com.enterprise.gateway.dto.GatewayDto;
import com.enterprise.gateway.model.Partner;
import com.enterprise.gateway.service.PartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for partner management operations
 */
@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
@Tag(name = "Partner Management", description = "Partner CRUD operations")
@SecurityRequirement(name = "Bearer Authentication")
public class PartnerController {

    private final PartnerService partnerService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @partnerSecurityService.isCurrentPartner(#id)")
    @Operation(summary = "Get partner by ID", description = "Retrieve partner details by ID")
    public ResponseEntity<GatewayDto.ApiResponse<GatewayDto.PartnerResponse>> getPartner(
            @PathVariable Long id) {

        GatewayDto.PartnerResponse partner = partnerService.getPartnerById(id);
        return ResponseEntity.ok(GatewayDto.ApiResponse.success(partner));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all partners", description = "Retrieve paginated list of all partners")
    public ResponseEntity<GatewayDto.ApiResponse<Page<GatewayDto.PartnerResponse>>> getAllPartners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<GatewayDto.PartnerResponse> partners = partnerService.getAllPartners(pageable);
        return ResponseEntity.ok(GatewayDto.ApiResponse.success(partners));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get partners by status", description = "Retrieve partners filtered by status")
    public ResponseEntity<GatewayDto.ApiResponse<List<GatewayDto.PartnerResponse>>> getPartnersByStatus(
            @PathVariable Partner.PartnerStatus status) {

        List<GatewayDto.PartnerResponse> partners = partnerService.getPartnersByStatus(status);
        return ResponseEntity.ok(GatewayDto.ApiResponse.success(partners));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update partner status", description = "Update partner account status")
    public ResponseEntity<GatewayDto.ApiResponse<GatewayDto.PartnerResponse>> updatePartnerStatus(
            @PathVariable Long id,
            @RequestParam Partner.PartnerStatus status) {

        GatewayDto.PartnerResponse partner = partnerService.updatePartnerStatus(id, status);
        return ResponseEntity.ok(GatewayDto.ApiResponse.success("Status updated successfully", partner));
    }

    @PutMapping("/{id}/rate-limits")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update rate limits", description = "Update partner rate limits")
    public ResponseEntity<GatewayDto.ApiResponse<GatewayDto.PartnerResponse>> updateRateLimits(
            @PathVariable Long id,
            @RequestParam Integer perMinute,
            @RequestParam Integer perHour) {

        GatewayDto.PartnerResponse partner = partnerService.updatePartnerRateLimits(id, perMinute, perHour);
        return ResponseEntity.ok(GatewayDto.ApiResponse.success("Rate limits updated successfully", partner));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete partner", description = "Delete partner account")
    public ResponseEntity<GatewayDto.ApiResponse<Void>> deletePartner(@PathVariable Long id) {
        partnerService.deletePartner(id);
        return ResponseEntity.ok(GatewayDto.ApiResponse.success("Partner deleted successfully", null));
    }
}
