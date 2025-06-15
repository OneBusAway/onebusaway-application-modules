package org.onebusaway.api.config;

import org.onebusaway.api.services.ApiIntervalFactory;
import org.onebusaway.container.spring.PropertyOverrideConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class ApiServiceConfiguration {

    @Bean
    public ApiIntervalFactory apiIntervalFactory() {
        return new ApiIntervalFactory();
    }

    @Bean
    public PropertyOverrideConfigurer propertyOverrideConfigurer() {
        PropertyOverrideConfigurer configurer = new PropertyOverrideConfigurer();
        Properties properties = new Properties();
        properties.setProperty("cacheManager.cacheManagerName", "org.onebusaway.api_webapp.cacheManager");
        configurer.setProperties(properties);
        return configurer;
    }
}