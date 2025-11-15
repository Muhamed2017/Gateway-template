package com.enterprise.gateway.integration;

import com.enterprise.gateway.dto.GatewayDto;
import com.enterprise.gateway.model.Partner;
import com.enterprise.gateway.repository.PartnerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for authentication endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthenticationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.cache.type", () -> "simple");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        partnerRepository.deleteAll();
    }

    @Test
    void testSuccessfulLogin() throws Exception {
        // Given: A partner exists in the database
        Partner partner = Partner.builder()
            .name("Test Partner")
            .apiKey("TEST_API_KEY")
            .secretHash(passwordEncoder.encode("test123"))
            .email("test@example.com")
            .status(Partner.PartnerStatus.ACTIVE)
            .roles(Set.of("USER"))
            .rateLimitPerMinute(60)
            .rateLimitPerHour(1000)
            .build();
        partnerRepository.save(partner);

        // When: Login request is sent
        GatewayDto.LoginRequest loginRequest = new GatewayDto.LoginRequest();
        loginRequest.setApiKey("TEST_API_KEY");
        loginRequest.setSecret("test123");

        // Then: Should return JWT token
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.token").exists())
            .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.data.partnerInfo.name").value("Test Partner"));
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        // Given: A partner exists
        Partner partner = Partner.builder()
            .name("Test Partner")
            .apiKey("TEST_API_KEY")
            .secretHash(passwordEncoder.encode("test123"))
            .email("test@example.com")
            .status(Partner.PartnerStatus.ACTIVE)
            .roles(Set.of("USER"))
            .build();
        partnerRepository.save(partner);

        // When: Login with wrong password
        GatewayDto.LoginRequest loginRequest = new GatewayDto.LoginRequest();
        loginRequest.setApiKey("TEST_API_KEY");
        loginRequest.setSecret("wrongpassword");

        // Then: Should return unauthorized
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testRegisterNewPartner() throws Exception {
        // When: Register a new partner
        GatewayDto.RegisterPartnerRequest registerRequest = new GatewayDto.RegisterPartnerRequest();
        registerRequest.setName("New Partner");
        registerRequest.setEmail("new@partner.com");
        registerRequest.setSecret("secret123");
        registerRequest.setRoles(Set.of("USER"));
        registerRequest.setRateLimitPerMinute(100);

        // Then: Should successfully register
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("New Partner"))
            .andExpect(jsonPath("$.data.apiKey").exists())
            .andExpect(jsonPath("$.data.email").value("new@partner.com"));
    }

    @Test
    void testRegisterDuplicatePartner() throws Exception {
        // Given: A partner already exists
        Partner existingPartner = Partner.builder()
            .name("Existing Partner")
            .apiKey("EXISTING_KEY")
            .secretHash(passwordEncoder.encode("test123"))
            .email("existing@partner.com")
            .status(Partner.PartnerStatus.ACTIVE)
            .roles(Set.of("USER"))
            .build();
        partnerRepository.save(existingPartner);

        // When: Try to register with same name
        GatewayDto.RegisterPartnerRequest registerRequest = new GatewayDto.RegisterPartnerRequest();
        registerRequest.setName("Existing Partner");
        registerRequest.setEmail("different@email.com");
        registerRequest.setSecret("secret123");

        // Then: Should return conflict error
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false));
    }
}
