package org.onebusaway.api;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Boot configuration for OneBusAway API webapp
 * 
 * Maintains compatibility with existing XML configuration while
 * adding Spring Boot capabilities
 */
@Configuration
@ImportResource({
    "classpath:org/onebusaway/api/actions/api/where/data-sources.xml",
    "classpath:org/onebusaway/api/application-context.xml",
    "classpath:org/onebusaway/container/application-context.xml",
    "classpath:org/onebusaway/users/application-context.xml"
})
public class ApiConfiguration {

    /**
     * OneBusAway API specific configuration properties
     */
    @Bean
    @ConfigurationProperties(prefix = "onebusaway.api")
    public ApiProperties apiProperties() {
        return new ApiProperties();
    }

    /**
     * CORS configuration for API endpoints
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                ApiProperties props = apiProperties();
                if (props.getCors().isEnabled()) {
                    registry.addMapping("/api/**")
                            .allowedOrigins(props.getCors().getAllowedOrigins().split(","))
                            .allowedMethods(props.getCors().getAllowedMethods().split(","))
                            .allowedHeaders("*")
                            .allowCredentials(true);
                            
                    registry.addMapping("/siri/**")
                            .allowedOrigins(props.getCors().getAllowedOrigins().split(","))
                            .allowedMethods(props.getCors().getAllowedMethods().split(","))
                            .allowedHeaders("*")
                            .allowCredentials(true);
                }
            }
        };
    }

    /**
     * Configuration properties class for API settings
     */
    public static class ApiProperties {
        private Cors cors = new Cors();
        private Key key = new Key();

        public Cors getCors() {
            return cors;
        }

        public void setCors(Cors cors) {
            this.cors = cors;
        }

        public Key getKey() {
            return key;
        }

        public void setKey(Key key) {
            this.key = key;
        }

        public static class Cors {
            private boolean enabled = true;
            private String allowedOrigins = "*";
            private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getAllowedOrigins() {
                return allowedOrigins;
            }

            public void setAllowedOrigins(String allowedOrigins) {
                this.allowedOrigins = allowedOrigins;
            }

            public String getAllowedMethods() {
                return allowedMethods;
            }

            public void setAllowedMethods(String allowedMethods) {
                this.allowedMethods = allowedMethods;
            }
        }

        public static class Key {
            private boolean required = false;

            public boolean isRequired() {
                return required;
            }

            public void setRequired(boolean required) {
                this.required = required;
            }
        }
    }
}