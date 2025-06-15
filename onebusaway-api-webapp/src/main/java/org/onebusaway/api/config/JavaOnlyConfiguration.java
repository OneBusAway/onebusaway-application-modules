package org.onebusaway.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class that enables Java-only configuration mode.
 * When 'java-only' profile is active, this configuration is loaded
 * instead of XML-based configurations to test complete Java migration.
 */
@Configuration
@Profile("java-only")
public class JavaOnlyConfiguration {
    
    // This class serves as a marker for Java-only configuration mode
    // All necessary beans are already defined in other @Configuration classes:
    // - DataSourceConfiguration: Data sources and Hessian remoting
    // - ApiServiceConfiguration: API-specific service beans
    // - OneBusAwayApiApplication: Application-level configuration
    
}