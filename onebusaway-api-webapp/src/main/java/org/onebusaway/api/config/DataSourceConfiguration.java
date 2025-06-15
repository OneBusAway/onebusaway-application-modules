package org.onebusaway.api.config;

import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfiguration {

    @Value("${onebusaway.datasource.jndi-name:java:comp/env/jdbc/appDB}")
    private String jndiName;

    @Value("${onebusaway.datasource.archive.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String archiveDriverClassName;

    @Value("${onebusaway.datasource.archive.url:jdbc:mysql://localhost:3306/onebusaway}")
    private String archiveUrl;

    @Value("${onebusaway.datasource.archive.username:root}")
    private String archiveUsername;

    @Value("${onebusaway.datasource.archive.password:root}")
    private String archivePassword;

    @Value("${onebusaway.datasource.agency.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String agencyDriverClassName;

    @Value("${onebusaway.datasource.agency.url:jdbc:mysql://localhost:3306/onebusaway}")
    private String agencyUrl;

    @Value("${onebusaway.datasource.agency.username:root}")
    private String agencyUsername;

    @Value("${onebusaway.datasource.agency.password:root}")
    private String agencyPassword;

    @Value("${onebusaway.federation.service-url:http://localhost:8080/onebusaway-transit-data-federation-webapp/remoting/transit-data-service}")
    private String transitDataServiceUrl;

    @Bean
    @Primary
    public DataSource dataSource() {
        JndiObjectFactoryBean jndiFactory = new JndiObjectFactoryBean();
        jndiFactory.setJndiName(jndiName);
        jndiFactory.setLookupOnStartup(true);
        jndiFactory.setCache(true);
        jndiFactory.setProxyInterface(DataSource.class);
        jndiFactory.setResourceRef(true);
        try {
            jndiFactory.afterPropertiesSet();
            return (DataSource) jndiFactory.getObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create JNDI DataSource", e);
        }
    }

    @Bean
    public DataSource archiveDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(archiveDriverClassName);
        dataSource.setUrl(archiveUrl);
        dataSource.setUsername(archiveUsername);
        dataSource.setPassword(archivePassword);
        return dataSource;
    }

    @Bean
    public DataSource agencyDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(agencyDriverClassName);
        dataSource.setUrl(agencyUrl);
        dataSource.setUsername(agencyUsername);
        dataSource.setPassword(agencyPassword);
        return dataSource;
    }

    @Bean
    public TransitDataService transitDataService() {
        HessianProxyFactoryBean hessianProxy = new HessianProxyFactoryBean();
        hessianProxy.setServiceUrl(transitDataServiceUrl);
        hessianProxy.setServiceInterface(TransitDataService.class);
        hessianProxy.afterPropertiesSet();
        return (TransitDataService) hessianProxy.getObject();
    }
}