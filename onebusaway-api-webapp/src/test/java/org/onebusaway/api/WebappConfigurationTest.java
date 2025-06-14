package org.onebusaway.api;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import org.onebusaway.users.impl.CreateApiKeyAction;
import org.onebusaway.users.services.validation.KeyValidationService;
import org.onebusaway.api.impl.ConsolidatedStopIdModificationStrategy;

import static org.junit.Assert.*;

/**
 * Unit tests for WebappConfiguration class
 * 
 * Tests individual bean creation methods without full Spring context integration.
 * This ensures the configuration class works correctly in isolation.
 */
public class WebappConfigurationTest {

    private WebappConfiguration configuration;

    @Before
    public void setUp() {
        configuration = new WebappConfiguration();
    }

    @Test
    public void testTaskSchedulerBean() {
        ThreadPoolTaskScheduler scheduler = configuration.taskScheduler();
        
        assertNotNull("TaskScheduler bean should be created", scheduler);
        assertEquals("Pool size should be 10", 10, scheduler.getPoolSize());
        assertTrue("Thread name prefix should be set", 
                   scheduler.getThreadNamePrefix().startsWith("oba-api-task-"));
    }

    @Test
    public void testApiKeyValidationServiceBean() {
        KeyValidationService service = configuration.apiKeyValidationService();
        
        assertNotNull("ApiKeyValidationService bean should be created", service);
        
        // Test the service functionality to ensure it's properly configured
        assertTrue("Should be able to validate test keys", service.isValidKey("TEST"));
        assertFalse("Should reject invalid keys", service.isValidKey("INVALID_KEY"));
        
        // Test key generation functionality
        String generatedKey = service.generateKeyWithDefaultProvider("test-input");
        assertNotNull("Should generate keys", generatedKey);
        
        // Note: Generated salted hash keys are validated with the original input, not standalone
        // This is expected behavior for the salted password validation provider
        assertTrue("Generated key should be valid with original input", 
                   service.isValidKey(generatedKey, "test-input"));
    }

    @Test
    public void testCreateAPIKeyOnInitActionBean() {
        CreateApiKeyAction action = configuration.createAPIKeyOnInitAction();
        
        assertNotNull("CreateApiKeyAction bean should be created", action);
        assertEquals("Key should be set to OBAKEY", "OBAKEY", action.getKey());
    }

    @Test
    public void testConsolidatedStopIdModificationStrategyBean() {
        ConsolidatedStopIdModificationStrategy strategy = configuration.consolidatedStopIdModificationStrategy();
        
        assertNotNull("ConsolidatedStopIdModificationStrategy bean should be created", strategy);
    }
}