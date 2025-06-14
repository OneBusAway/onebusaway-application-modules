package org.onebusaway.api;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import org.onebusaway.users.impl.CreateApiKeyAction;
import org.onebusaway.users.services.validation.KeyValidationService;
import org.onebusaway.users.impl.validation.SaltedPasswordValidationProviderV1Impl;
import org.onebusaway.users.services.validation.KeyValidationProvider;
import org.onebusaway.api.impl.ConsolidatedStopIdModificationStrategy;

/**
 * Spring Boot configuration to replace application-context-webapp.xml
 * 
 * Migrates XML-based Spring configuration to Java-based @Configuration
 * while maintaining backward compatibility with existing functionality.
 */
@Configuration
@EnableAspectJAutoProxy
@EnableScheduling
@ComponentScan(basePackages = {
    "org.onebusaway.api.impl",
    "org.onebusaway.presentation.impl.realtime",
    "org.onebusaway.presentation.services.cachecontrol",
    "org.onebusaway.agency_metadata",
    "org.onebusaway.api.actions.siri.impl"
})
@ImportResource({
    "classpath:org/onebusaway/agency_metadata/application-context.xml",
    "classpath:org/onebusaway/users/application-context.xml",
    "classpath:org/onebusaway/util/application-context.xml"
})
public class WebappConfiguration {

    /**
     * Thread pool task scheduler for asynchronous task execution
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("oba-api-task-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        return scheduler;
    }

    /**
     * API key validation service with salted password providers
     * 
     * Uses reflection to create the package-private KeyValidationServiceImpl,
     * matching the behavior of the original XML configuration.
     */
    @Bean
    public KeyValidationService apiKeyValidationService() {
        try {
            // Create the package-private KeyValidationServiceImpl using reflection
            Class<?> implClass = Class.forName("org.onebusaway.users.impl.validation.KeyValidationServiceImpl");
            Object service = implClass.getDeclaredConstructor().newInstance();
            
            // Set the providers using reflection
            var method = implClass.getMethod("setProviders", List.class);
            method.invoke(service, List.of(new SaltedPasswordValidationProviderV1Impl()));
            
            return (KeyValidationService) service;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create KeyValidationService", e);
        }
    }

    /**
     * Creates default API key "OBAKEY" on application startup
     */
    @Bean
    public CreateApiKeyAction createAPIKeyOnInitAction() {
        CreateApiKeyAction action = new CreateApiKeyAction();
        action.setKey("OBAKEY");
        return action;
    }

    /**
     * Stop ID modification strategy for consolidated stop handling
     */
    @Bean
    public ConsolidatedStopIdModificationStrategy consolidatedStopIdModificationStrategy() {
        return new ConsolidatedStopIdModificationStrategy();
    }
}