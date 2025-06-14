package org.onebusaway.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XContentTypeOptionsHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Spring Security configuration for OneBusAway API webapp
 * 
 * Configures security for API endpoints while maintaining backward compatibility.
 * API key authentication is handled by ApiKeyInterceptor in Struts layer.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Value("${onebusaway.api.security.hsts.enabled:true}")
    private boolean hstsEnabled;

    @Value("${onebusaway.api.security.xss-protection.enabled:true}")
    private boolean xssProtectionEnabled;

    @Value("${onebusaway.api.security.content-type-options.enabled:true}")
    private boolean contentTypeOptionsEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for API endpoints as they use API keys
            .csrf(csrf -> csrf.disable())
            
            // Disable sessions for stateless API
            .sessionManagement(session -> session
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)
            )
            
            // Configure authorization
            .authorizeHttpRequests(authz -> authz
                // API endpoints - authentication handled by ApiKeyInterceptor
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/siri/**").permitAll()
                
                // Actuator endpoints
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/actuator/**").authenticated()
                
                // Static resources and cross-domain files
                .requestMatchers("/crossdomain.xml").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                .requestMatchers("/robots.txt").permitAll()
                .requestMatchers("/").permitAll()
                
                // Administrative endpoints require authentication
                .requestMatchers("/admin/**").authenticated()
                .requestMatchers("/management/**").authenticated()
                
                // Default: require authentication
                .anyRequest().authenticated()
            )
            
            // Configure security headers
            .headers(headers -> {
                headers.frameOptions().deny();
                
                if (contentTypeOptionsEnabled) {
                    headers.contentTypeOptions().and();
                }
                
                if (xssProtectionEnabled) {
                    headers.addHeaderWriter(new XXssProtectionHeaderWriter());
                }
                
                if (hstsEnabled) {
                    headers.httpStrictTransportSecurity(hstsConfig -> hstsConfig
                        .includeSubDomains(true)
                        .maxAgeInSeconds(31536000)
                    );
                }
                
                headers.referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN);
            });

        return http.build();
    }
}