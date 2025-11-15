package com.enterprise.gateway.controller;

import com.enterprise.gateway.dto.GatewayDto;
import com.enterprise.gateway.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for authentication operations
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Partner authentication endpoints")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(summary = "Partner login", description = "Authenticate partner and receive JWT token")
    public ResponseEntity<GatewayDto.ApiResponse<GatewayDto.LoginResponse>> login(
            @Valid @RequestBody GatewayDto.LoginRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);
        GatewayDto.LoginResponse response = authenticationService.login(request, clientIp);

        return ResponseEntity.ok(GatewayDto.ApiResponse.success("Login successful", response));
    }

    @PostMapping("/register")
    @Operation(summary = "Register new partner", description = "Register a new API consumer partner")
    public ResponseEntity<GatewayDto.ApiResponse<GatewayDto.PartnerResponse>> register(
            @Valid @RequestBody GatewayDto.RegisterPartnerRequest request) {

        GatewayDto.PartnerResponse response = authenticationService.registerPartner(request);

        return ResponseEntity.ok(GatewayDto.ApiResponse.success("Partner registered successfully", response));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
