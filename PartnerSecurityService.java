package com.enterprise.gateway.security;

import com.enterprise.gateway.model.Partner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service for partner-specific security checks
 */
@Service("partnerSecurityService")
@Slf4j
public class PartnerSecurityService {

    public boolean isCurrentPartner(Long partnerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof Partner)) {
            return false;
        }

        Partner partner = (Partner) authentication.getPrincipal();
        return partner.getId().equals(partnerId);
    }

    public Partner getCurrentPartner() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof Partner) {
            return (Partner) authentication.getPrincipal();
        }
        
        return null;
    }

    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + role));
    }
}
