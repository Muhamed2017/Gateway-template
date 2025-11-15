package com.enterprise.gateway.config;

import com.enterprise.gateway.filter.IpValidationFilter;
import com.enterprise.gateway.filter.RateLimitingFilter;
import com.enterprise.gateway.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for registering custom filters
 */
@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final IpValidationFilter ipValidationFilter;

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration() {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtAuthenticationFilter);
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<IpValidationFilter> ipValidationFilterRegistration() {
        FilterRegistrationBean<IpValidationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(ipValidationFilter);
        registration.setOrder(2);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration() {
        FilterRegistrationBean<RateLimitingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(rateLimitingFilter);
        registration.setOrder(3);
        return registration;
    }
}
