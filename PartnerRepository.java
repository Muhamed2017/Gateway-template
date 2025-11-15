package com.enterprise.gateway.repository;

import com.enterprise.gateway.model.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Partner entity operations
 */
@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {

    Optional<Partner> findByApiKey(String apiKey);

    Optional<Partner> findByEmail(String email);

    Optional<Partner> findByName(String name);

    List<Partner> findByStatus(Partner.PartnerStatus status);

    boolean existsByApiKey(String apiKey);

    boolean existsByEmail(String email);

    boolean existsByName(String name);

    @Modifying
    @Query("UPDATE Partner p SET p.lastLogin = :loginTime, p.loginCount = p.loginCount + 1 WHERE p.id = :partnerId")
    void updateLastLogin(@Param("partnerId") Long partnerId, @Param("loginTime") LocalDateTime loginTime);

    @Query("SELECT p FROM Partner p WHERE p.status = 'ACTIVE' AND p.rateLimitPerMinute IS NOT NULL")
    List<Partner> findAllActiveWithRateLimits();
}
