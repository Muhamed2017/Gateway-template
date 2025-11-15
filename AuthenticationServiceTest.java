package com.enterprise.gateway.service;

import com.enterprise.gateway.dto.GatewayDto;
import com.enterprise.gateway.exception.Exceptions;
import com.enterprise.gateway.model.Partner;
import com.enterprise.gateway.repository.PartnerRepository;
import com.enterprise.gateway.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationService
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private PartnerRepository partnerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private Partner testPartner;
    private GatewayDto.LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testPartner = Partner.builder()
            .id(1L)
            .name("Test Partner")
            .apiKey("TEST_API_KEY")
            .secretHash("hashed_secret")
            .email("test@example.com")
            .status(Partner.PartnerStatus.ACTIVE)
            .roles(Set.of("USER"))
            .rateLimitPerMinute(60)
            .build();

        loginRequest = new GatewayDto.LoginRequest();
        loginRequest.setApiKey("TEST_API_KEY");
        loginRequest.setSecret("test123");
    }

    @Test
    void testSuccessfulLogin() {
        // Given
        when(partnerRepository.findByApiKey(anyString())).thenReturn(Optional.of(testPartner));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(any(Partner.class))).thenReturn("test_jwt_token");
        when(jwtUtil.generateRefreshToken(any(Partner.class))).thenReturn("test_refresh_token");

        // When
        GatewayDto.LoginResponse response = authenticationService.login(loginRequest, "127.0.0.1");

        // Then
        assertNotNull(response);
        assertEquals("test_jwt_token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getPartnerInfo());
        assertEquals("Test Partner", response.getPartnerInfo().getName());
        
        verify(partnerRepository).updateLastLogin(eq(1L), any());
        verify(jwtUtil).generateToken(testPartner);
        verify(jwtUtil).generateRefreshToken(testPartner);
    }

    @Test
    void testLoginWithInvalidApiKey() {
        // Given
        when(partnerRepository.findByApiKey(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(Exceptions.InvalidApiKeyException.class, () -> {
            authenticationService.login(loginRequest, "127.0.0.1");
        });
        
        verify(partnerRepository, never()).updateLastLogin(any(), any());
    }

    @Test
    void testLoginWithInvalidSecret() {
        // Given
        when(partnerRepository.findByApiKey(anyString())).thenReturn(Optional.of(testPartner));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThrows(Exceptions.AuthenticationException.class, () -> {
            authenticationService.login(loginRequest, "127.0.0.1");
        });
        
        verify(partnerRepository, never()).updateLastLogin(any(), any());
    }

    @Test
    void testLoginWithSuspendedPartner() {
        // Given
        testPartner.setStatus(Partner.PartnerStatus.SUSPENDED);
        when(partnerRepository.findByApiKey(anyString())).thenReturn(Optional.of(testPartner));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // When & Then
        assertThrows(Exceptions.PartnerSuspendedException.class, () -> {
            authenticationService.login(loginRequest, "127.0.0.1");
        });
        
        verify(partnerRepository, never()).updateLastLogin(any(), any());
    }

    @Test
    void testRegisterPartner() {
        // Given
        GatewayDto.RegisterPartnerRequest registerRequest = new GatewayDto.RegisterPartnerRequest();
        registerRequest.setName("New Partner");
        registerRequest.setEmail("new@partner.com");
        registerRequest.setSecret("secret123");
        registerRequest.setRoles(Set.of("USER"));

        when(partnerRepository.existsByName(anyString())).thenReturn(false);
        when(partnerRepository.existsByEmail(anyString())).thenReturn(false);
        when(partnerRepository.existsByApiKey(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_secret");
        when(partnerRepository.save(any(Partner.class))).thenReturn(testPartner);

        // When
        GatewayDto.PartnerResponse response = authenticationService.registerPartner(registerRequest);

        // Then
        assertNotNull(response);
        verify(partnerRepository).save(any(Partner.class));
    }

    @Test
    void testRegisterDuplicatePartner() {
        // Given
        GatewayDto.RegisterPartnerRequest registerRequest = new GatewayDto.RegisterPartnerRequest();
        registerRequest.setName("Existing Partner");
        registerRequest.setEmail("test@example.com");
        registerRequest.setSecret("secret123");

        when(partnerRepository.existsByName(anyString())).thenReturn(true);

        // When & Then
        assertThrows(Exceptions.DuplicatePartnerException.class, () -> {
            authenticationService.registerPartner(registerRequest);
        });
        
        verify(partnerRepository, never()).save(any());
    }
}
