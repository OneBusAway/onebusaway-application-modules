package org.onebusaway.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot JPA configuration for OneBusAway API webapp.
 * 
 * Enables transaction management and relies on Spring Boot auto-configuration
 * for JPA/Hibernate setup using properties from application.yml.
 * 
 * The existing XML-based Hibernate configuration is still active through
 * imported XML contexts for backward compatibility.
 */
@Configuration
@EnableTransactionManagement
public class JpaConfiguration {
    
    /*
     * Spring Boot will auto-configure JPA based on the properties in application.yml
     * 
     * Key configurations handled automatically:
     * - EntityManagerFactory creation using primary dataSource
     * - JpaTransactionManager setup
     * - Hibernate properties configuration
     * - Entity scanning from classpath
     * 
     * This approach maintains compatibility with existing XML-based configurations
     * while providing Spring Boot's auto-configuration benefits.
     */
}