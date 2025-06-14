package org.onebusaway.api;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;

import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.api.services.ApiIntervalFactory;
import org.onebusaway.container.spring.PropertyOverrideConfigurer;
import java.util.Properties;

/**
 * Spring Boot configuration for data sources and related services.
 * 
 * Replaces the XML-based data-sources.xml configuration with Java-based configuration
 * while maintaining backward compatibility.
 */
@Configuration
public class DataSourceConfiguration {

    @Value("${onebusaway.datasource.jndi-name:java:comp/env/jdbc/appDB}")
    private String jndiName;

    @Value("${onebusaway.federation.service-url:http://localhost:8080/onebusaway-transit-data-federation-webapp/remoting/transit-data-service}")
    private String federationServiceUrl;

    /**
     * Primary data source using JNDI lookup (for production environments)
     */
    @Primary
    @Bean(name = "dataSource")
    public DataSource primaryDataSource() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName(jndiName);
        jndiObjectFactoryBean.setLookupOnStartup(true);
        jndiObjectFactoryBean.setCache(true);
        jndiObjectFactoryBean.setProxyInterface(DataSource.class);
        jndiObjectFactoryBean.setResourceRef(true);
        
        try {
            jndiObjectFactoryBean.afterPropertiesSet();
            return (DataSource) jndiObjectFactoryBean.getObject();
        } catch (Exception e) {
            // Fallback to direct JDBC configuration if JNDI lookup fails
            return createFallbackDataSource();
        }
    }

    /**
     * Archive data source for MySQL-based archival data
     */
    @Bean(name = "archiveDataSource")
    @ConfigurationProperties(prefix = "onebusaway.datasource.archive")
    public DataSource archiveDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .url("jdbc:mysql://localhost:3306/onebusaway")
                .username("${DATABASE_USER:root}")
                .password("${DATABASE_PASSWORD:root}")
                .build();
    }

    /**
     * Agency data source for agency-specific configuration
     */
    @Bean(name = "agencyDataSource")
    @ConfigurationProperties(prefix = "onebusaway.datasource.agency")
    public DataSource agencyDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .url("jdbc:mysql://localhost:3306/onebusaway")
                .username("${DATABASE_USER:root}")
                .password("${DATABASE_PASSWORD:root}")
                .build();
    }

    /**
     * Fallback data source when JNDI is not available (development/testing)
     */
    private DataSource createFallbackDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .url("jdbc:mysql://localhost:3306/onebusaway_dev?useSSL=false&serverTimezone=UTC")
                .username("${DATABASE_USER:oba_dev}")
                .password("${DATABASE_PASSWORD:oba_dev}")
                .build();
    }

    /**
     * Transit data service via Hessian remoting to federation webapp
     */
    @Bean(name = "transitDataService")
    public TransitDataService transitDataService() {
        HessianProxyFactoryBean hessianProxy = new HessianProxyFactoryBean();
        hessianProxy.setServiceUrl(federationServiceUrl);
        hessianProxy.setServiceInterface(TransitDataService.class);
        hessianProxy.afterPropertiesSet();
        return (TransitDataService) hessianProxy.getObject();
    }

    /**
     * API key validation service - delegated to XML configuration for now
     * This bean is defined in the imported XML context
     */

    /**
     * API interval factory service
     */
    @Bean
    public ApiIntervalFactory apiIntervalFactory() {
        return new ApiIntervalFactory();
    }

    /**
     * Property override configurer for cache manager naming
     */
    @Bean
    public PropertyOverrideConfigurer propertyOverrideConfigurer() {
        PropertyOverrideConfigurer configurer = new PropertyOverrideConfigurer();
        Properties properties = new Properties();
        properties.setProperty("cacheManager.cacheManagerName", "org.onebusaway.api_webapp.cacheManager");
        configurer.setProperties(properties);
        return configurer;
    }

    /**
     * Custom route sort configuration - delegated to XML configuration for now
     * This bean is defined in the imported XML context
     */
}