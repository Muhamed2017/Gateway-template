package com.enterprise.gateway.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a partner/client that consumes our APIs
 */
@Entity
@Table(name = "partners", indexes = {
    @Index(name = "idx_partner_api_key", columnList = "apiKey"),
    @Index(name = "idx_partner_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String apiKey;

    @Column(nullable = false, length = 255)
    private String secretHash;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PartnerStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "partner_roles", joinColumns = @JoinColumn(name = "partner_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "partner_allowed_ips", joinColumns = @JoinColumn(name = "partner_id"))
    @Column(name = "ip_address")
    @Builder.Default
    private Set<String> allowedIpAddresses = new HashSet<>();

    @Column(name = "rate_limit_per_minute")
    private Integer rateLimitPerMinute;

    @Column(name = "rate_limit_per_hour")
    private Integer rateLimitPerHour;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "login_count")
    @Builder.Default
    private Long loginCount = 0L;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 500)
    private String description;

    public enum PartnerStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        PENDING
    }
}
