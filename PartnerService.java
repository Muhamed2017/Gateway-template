package com.enterprise.gateway.service;

import com.enterprise.gateway.dto.GatewayDto;
import com.enterprise.gateway.exception.Exceptions;
import com.enterprise.gateway.model.Partner;
import com.enterprise.gateway.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing partner operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    @Cacheable(value = "partners", key = "#id")
    public GatewayDto.PartnerResponse getPartnerById(Long id) {
        Partner partner = partnerRepository.findById(id)
            .orElseThrow(() -> new Exceptions.PartnerNotFoundException("Partner not found with id: " + id));
        return mapToPartnerResponse(partner);
    }

    @Transactional(readOnly = true)
    public Page<GatewayDto.PartnerResponse> getAllPartners(Pageable pageable) {
        return partnerRepository.findAll(pageable)
            .map(this::mapToPartnerResponse);
    }

    @Transactional(readOnly = true)
    public List<GatewayDto.PartnerResponse> getPartnersByStatus(Partner.PartnerStatus status) {
        return partnerRepository.findByStatus(status).stream()
            .map(this::mapToPartnerResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "partners", key = "#id")
    public GatewayDto.PartnerResponse updatePartnerStatus(Long id, Partner.PartnerStatus status) {
        Partner partner = partnerRepository.findById(id)
            .orElseThrow(() -> new Exceptions.PartnerNotFoundException("Partner not found with id: " + id));

        partner.setStatus(status);
        partner = partnerRepository.save(partner);

        log.info("Updated partner {} status to {}", partner.getName(), status);
        return mapToPartnerResponse(partner);
    }

    @Transactional
    @CacheEvict(value = "partners", key = "#id")
    public GatewayDto.PartnerResponse updatePartnerRateLimits(Long id, Integer perMinute, Integer perHour) {
        Partner partner = partnerRepository.findById(id)
            .orElseThrow(() -> new Exceptions.PartnerNotFoundException("Partner not found with id: " + id));

        partner.setRateLimitPerMinute(perMinute);
        partner.setRateLimitPerHour(perHour);
        partner = partnerRepository.save(partner);

        log.info("Updated rate limits for partner {}: {}/min, {}/hour", 
            partner.getName(), perMinute, perHour);
        return mapToPartnerResponse(partner);
    }

    @Transactional
    @CacheEvict(value = "partners", key = "#id")
    public void deletePartner(Long id) {
        Partner partner = partnerRepository.findById(id)
            .orElseThrow(() -> new Exceptions.PartnerNotFoundException("Partner not found with id: " + id));

        partnerRepository.delete(partner);
        log.info("Deleted partner: {}", partner.getName());
    }

    @Transactional(readOnly = true)
    public Partner getPartnerByApiKey(String apiKey) {
        return partnerRepository.findByApiKey(apiKey)
            .orElseThrow(() -> new Exceptions.InvalidApiKeyException("Invalid API key"));
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
