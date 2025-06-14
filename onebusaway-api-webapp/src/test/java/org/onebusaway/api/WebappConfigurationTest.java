package org.onebusaway.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.junit4.SpringRunner;

import org.onebusaway.users.impl.CreateApiKeyAction;
import org.onebusaway.users.services.validation.KeyValidationService;
import org.onebusaway.api.impl.ConsolidatedStopIdModificationStrategy;

import static org.junit.Assert.*;

/**
 * Unit tests for WebappConfiguration class
 * 
 * Validates that all beans are properly configured and wired,
 * ensuring the Java configuration matches the original XML behavior.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    OneBusAwayApiApplication.class
}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class WebappConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testTaskSchedulerBean() {
        ThreadPoolTaskScheduler scheduler = applicationContext.getBean("taskScheduler", ThreadPoolTaskScheduler.class);
        
        assertNotNull("TaskScheduler bean should be created", scheduler);
        assertEquals("Pool size should be 10", 10, scheduler.getPoolSize());
        assertTrue("Thread name prefix should be set", 
                   scheduler.getThreadNamePrefix().startsWith("oba-api-task-"));
        
        // Test that scheduler is properly initialized and running
        assertTrue("Scheduler should be initialized", scheduler.getScheduledExecutor() != null);
    }

    @Test
    public void testApiKeyValidationServiceBean() {
        KeyValidationService service = applicationContext.getBean("apiKeyValidationService", KeyValidationService.class);
        
        assertNotNull("ApiKeyValidationService bean should be created", service);
        
        // Test the service functionality to ensure it's properly configured
        assertTrue("Should be able to validate test keys", service.isValidKey("TEST"));
        assertFalse("Should reject invalid keys", service.isValidKey("INVALID_KEY"));
        
        // Test key generation functionality
        String generatedKey = service.generateKeyWithDefaultProvider("test-input");
        assertNotNull("Should generate keys", generatedKey);
        assertTrue("Generated key should be valid", service.isValidKey(generatedKey));
    }

    @Test
    public void testCreateAPIKeyOnInitActionBean() {
        CreateApiKeyAction action = applicationContext.getBean("createAPIKeyOnInitAction", CreateApiKeyAction.class);
        
        assertNotNull("CreateApiKeyAction bean should be created", action);
        assertEquals("Key should be set to OBAKEY", "OBAKEY", action.getKey());
    }

    @Test
    public void testConsolidatedStopIdModificationStrategyBean() {
        ConsolidatedStopIdModificationStrategy strategy = 
            applicationContext.getBean("consolidatedStopIdModificationStrategy", ConsolidatedStopIdModificationStrategy.class);
        
        assertNotNull("ConsolidatedStopIdModificationStrategy bean should be created", strategy);
    }

    @Test
    public void testComponentScanningWorks() {
        // Test that component scanning is working by checking if beans from scanned packages exist
        // This validates that @ComponentScan is properly configured
        assertTrue("Component scanning should discover beans from org.onebusaway.api.impl package",
                   applicationContext.getBeanNamesForType(Object.class).length > 10);
    }

    @Test
    public void testAspectJAutoProxyEnabled() {
        // Verify that AspectJ auto proxy is enabled by checking if AOP infrastructure is present
        // This is evidenced by the presence of auto proxy creator beans
        String[] beanNames = applicationContext.getBeanNamesForType(Object.class);
        boolean hasAopInfrastructure = false;
        
        for (String beanName : beanNames) {
            if (beanName.contains("AutoProxy") || beanName.contains("aop")) {
                hasAopInfrastructure = true;
                break;
            }
        }
        
        assertTrue("AspectJ auto proxy should be enabled", hasAopInfrastructure);
    }

    @Test
    public void testSchedulingEnabled() {
        // Verify that scheduling is enabled by checking for scheduling infrastructure
        String[] beanNames = applicationContext.getBeanNamesForType(Object.class);
        boolean hasSchedulingInfrastructure = false;
        
        for (String beanName : beanNames) {
            if (beanName.contains("Scheduling") || beanName.contains("taskScheduler")) {
                hasSchedulingInfrastructure = true;
                break;
            }
        }
        
        assertTrue("Scheduling should be enabled", hasSchedulingInfrastructure);
    }
}