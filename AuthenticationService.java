package com.enterprise.gateway.service;

import com.enterprise.gateway.dto.GatewayDto;
import com.enterprise.gateway.exception.Exceptions;
import com.enterprise.gateway.model.Partner;
import com.enterprise.gateway.repository.PartnerRepository;
import com.enterprise.gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Service for handling authentication operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final PartnerRepository partnerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;

    @Transactional
    public GatewayDto.LoginResponse login(GatewayDto.LoginRequest request, String clientIp) {
        log.info("Login attempt for API key: {}", maskApiKey(request.getApiKey()));

        Partner partner = partnerRepository.findByApiKey(request.getApiKey())
            .orElseThrow(() -> new Exceptions.InvalidApiKeyException("Invalid API key or secret"));

        if (!passwordEncoder.matches(request.getSecret(), partner.getSecretHash())) {
            log.warn("Invalid secret for partner: {}", partner.getName());
            throw new Exceptions.AuthenticationException("Invalid API key or secret");
        }

        if (partner.getStatus() != Partner.PartnerStatus.ACTIVE) {
            log.warn("Inactive partner login attempt: {} with status: {}", 
                partner.getName(), partner.getStatus());
            throw new Exceptions.PartnerSuspendedException(
                "Partner account is " + partner.getStatus().name().toLowerCase());
        }

        // Update last login
        partnerRepository.updateLastLogin(partner.getId(), LocalDateTime.now());

        // Generate tokens
        String accessToken = jwtUtil.generateToken(partner);
        String refreshToken = jwtUtil.generateRefreshToken(partner);

        log.info("Successful login for partner: {} from IP: {}", partner.getName(), clientIp);

        GatewayDto.PartnerInfo partnerInfo = GatewayDto.PartnerInfo.builder()
            .id(partner.getId())
            .name(partner.getName())
            .email(partner.getEmail())
            .roles(partner.getRoles())
            .status(partner.getStatus().name())
            .lastLogin(partner.getLastLogin())
            .build();

        return GatewayDto.LoginResponse.builder()
            .token(accessToken)
            .tokenType("Bearer")
            .expiresIn(3600L) // 1 hour
            .refreshToken(refreshToken)
            .partnerInfo(partnerInfo)
            .build();
    }

    @Transactional
    public GatewayDto.PartnerResponse registerPartner(GatewayDto.RegisterPartnerRequest request) {
        log.info("Registering new partner: {}", request.getName());

        // Check for duplicates
        if (partnerRepository.existsByName(request.getName())) {
            throw new Exceptions.DuplicatePartnerException("Partner name already exists");
        }

        if (partnerRepository.existsByEmail(request.getEmail())) {
            throw new Exceptions.DuplicatePartnerException("Email already registered");
        }

        // Generate unique API key
        String apiKey = generateApiKey();
        while (partnerRepository.existsByApiKey(apiKey)) {
            apiKey = generateApiKey();
        }

        // Create partner
        Partner partner = Partner.builder()
            .name(request.getName())
            .email(request.getEmail())
            .apiKey(apiKey)
            .secretHash(passwordEncoder.encode(request.getSecret()))
            .status(Partner.PartnerStatus.ACTIVE)
            .roles(request.getRoles() != null ? request.getRoles() : Set.of("USER"))
            .allowedIpAddresses(request.getAllowedIpAddresses())
            .rateLimitPerMinute(request.getRateLimitPerMinute() != null ? request.getRateLimitPerMinute() : 60)
            .rateLimitPerHour(request.getRateLimitPerHour() != null ? request.getRateLimitPerHour() : 1000)
            .description(request.getDescription())
            .loginCount(0L)
            .build();

        partner = partnerRepository.save(partner);
        log.info("Successfully registered partner: {} with API key: {}", 
            partner.getName(), maskApiKey(partner.getApiKey()));

        return mapToPartnerResponse(partner);
    }

    private String generateApiKey() {
        return "GW_" + UUID.randomUUID().toString().replace("-", "").substring(0, 32).toUpperCase();
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) return "***";
        return apiKey.substring(0, 4) + "***" + apiKey.substring(apiKey.length() - 4);
    }

    private GatewayDto.PartnerResponse mapToPartnerResponse(Partner partner) {
        return GatewayDto.PartnerResponse.builder()
            .id(partner.getId())
            .name(partner.getName())
            .email(partner.getEmail())
            .apiKey(partner.getApiKey())
            .status(partner.getStatus().name())
            .roles(partner.getRoles())
            .allowedIpAddresses(partner.getAllowedIpAddresses())
            .rateLimitPerMinute(partner.getRateLimitPerMinute())
            .rateLimitPerHour(partner.getRateLimitPerHour())
            .createdAt(partner.getCreatedAt())
            .updatedAt(partner.getUpdatedAt())
            .description(partner.getDescription())
            .build();
    }
}
