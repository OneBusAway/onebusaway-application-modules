package org.onebusaway.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Spring Boot application class for OneBusAway API webapp
 * 
 * This class provides the entry point for Spring Boot while maintaining 
 * compatibility with existing OneBusAway patterns and configurations.
 * 
 * Note: JPA auto-configuration is excluded to maintain compatibility with
 * existing Hibernate/Spring configuration patterns used in OneBusAway
 */
@SpringBootApplication(exclude = {
    JpaRepositoriesAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@ComponentScan(basePackages = {
    "org.onebusaway.api",
    "org.onebusaway.transit_data",
    "org.onebusaway.users",
    "org.onebusaway.util",
    "org.onebusaway.presentation"
})
@Import({
    ApiConfiguration.class,
    SecurityConfiguration.class
})
public class OneBusAwayApiApplication {

    public static void main(String[] args) {
        // Set default profile if none specified
        if (System.getProperty("spring.profiles.active") == null) {
            System.setProperty("spring.profiles.active", "development");
        }
        
        SpringApplication.run(OneBusAwayApiApplication.class, args);
    }
}