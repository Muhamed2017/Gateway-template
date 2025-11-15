package com.enterprise.gateway.util;

import com.enterprise.gateway.model.Partner;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT token utility for generating and validating tokens
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:3600000}") // 1 hour default
    private Long expiration;

    @Value("${jwt.refresh.expiration:86400000}") // 24 hours default
    private Long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Partner partner) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("partnerId", partner.getId());
        claims.put("partnerName", partner.getName());
        claims.put("email", partner.getEmail());
        claims.put("roles", partner.getRoles());
        claims.put("status", partner.getStatus().name());

        return createToken(claims, partner.getApiKey(), expiration);
    }

    public String generateRefreshToken(Partner partner) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("partnerId", partner.getId());
        claims.put("type", "refresh");

        return createToken(claims, partner.getApiKey(), refreshExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey(), Jwts.SIG.HS256)
            .compact();
    }

    public String extractApiKey(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractPartnerId(String token) {
        return extractClaim(token, claims -> claims.get("partnerId", Long.class));
    }

    public String extractPartnerName(String token) {
        return extractClaim(token, claims -> claims.get("partnerName", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException e) {
            log.error("Failed to parse JWT token: {}", e.getMessage());
            throw e;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    public boolean validateToken(String token, Partner partner) {
        try {
            final String apiKey = extractApiKey(token);
            return (apiKey.equals(partner.getApiKey()) && !isTokenExpired(token));
        } catch (JwtException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
