package org.onebusaway.api;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Traditional Spring configuration class for OneBusAway API webapp
 * 
 * This class provides minimal Spring configuration for WAR deployment in 
 * external servlet containers. No Spring Boot dependencies.
 */
@Configuration
@ComponentScan(basePackages = {
    "org.onebusaway.api.impl",
    "org.onebusaway.api.services"
})
public class OneBusAwayApiApplication {
    // Traditional Spring configuration - no main method needed for WAR deployment
}