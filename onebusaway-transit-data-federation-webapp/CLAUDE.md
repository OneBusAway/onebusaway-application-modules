# CLAUDE.md - OneBusAway Transit Data Federation WebApp

This file provides guidance to Claude Code when working with the OneBusAway federation service gateway module.

## Module Overview

The onebusaway-transit-data-federation-webapp is a Spring-based web application that acts as a service gateway, exposing the federation engine's capabilities through Hessian binary RPC services. It serves as the data backend for API webapps and handles real-time data ingestion.

## Key Commands

### Development
- `mvn jetty:run` - Start development server on port 8080
- `mvn package` - Build WAR file for deployment
- `mvn test` - Run service integration tests

### Service Endpoints
- http://localhost:8080/onebusaway-transit-data-federation-webapp/remoting/transit-data-service - Main data service
- http://localhost:8080/onebusaway-transit-data-federation-webapp/remoting/vehicle-location-listener - Real-time data ingestion
- http://localhost:8080/onebusaway-transit-data-federation-webapp/gtfs-realtime/ - GTFS-RT feed generation

## Architecture Overview

### Service Gateway Role

**Core Function:**
Acts as a **service facade** bridging the federation engine to external consumers through network protocols.

**Data Flow:**
```
External Data → Federation Engine → Federation WebApp (Hessian) → API WebApp → Clients
```

**Key Benefits:**
- Cross-JVM communication between federation engine and API layers
- Multiple API webapp instances can connect to single federation backend
- Clean separation between data processing and presentation layers

### Hessian Binary RPC Services

**Primary Service Exports** (`remoting-servlet.xml`):

**TransitDataService** (`/remoting/transit-data-service`)
- Main interface for schedule data, real-time information, trip planning
- Exposes complete federation engine capabilities to API layer
- Binary protocol for efficient cross-network communication

**VehicleLocationListener** (`/remoting/vehicle-location-listener`)
- Real-time vehicle position ingestion endpoint
- Handles incoming feeds from AVL systems and GTFS-realtime sources
- Processes and distributes real-time updates

**Configuration Pattern:**
```xml
<bean name="/service-name" class="org.springframework.remoting.caucho.HessianServiceExporter">
    <property name="service" ref="actualServiceBean" />
    <property name="serviceInterface" value="org.onebusaway.ServiceInterface" />
</bean>
```

### Web Controllers and Endpoints

#### Bundle Management (`BundleManagementController`)
- **Purpose:** GTFS data bundle administration
- **Endpoints:** Bundle upload, validation, activation
- **Security:** Protected by interceptors for administrative operations

#### GTFS-Realtime Export (`GtfsRealtimeController`)
- **Purpose:** Generate GTFS-RT Protocol Buffer feeds
- **Endpoints:** `/gtfs-realtime/trip-updates`, `/gtfs-realtime/vehicle-positions`, `/gtfs-realtime/alerts`
- **Output:** Binary Protocol Buffer format for third-party consumption

#### Vehicle Position Ingestion (`VehicleLocationRecordController`)
- **Purpose:** Accept real-time vehicle location data
- **Formats:** JSON, XML, custom formats
- **Processing:** Validates and routes to federation engine

#### Playback Controller (`PlaybackController`)
- **Purpose:** Historical data replay for testing and debugging
- **Features:** Time-based data simulation, scenario testing

## Development Patterns

### Adding New Remote Services

1. **Define Service Interface:**
```java
public interface MyCustomService {
    MyResult performOperation(MyRequest request);
}
```

2. **Implement Service:**
```java
@Component
public class MyCustomServiceImpl implements MyCustomService {
    
    @Autowired
    private TransitDataService _transitDataService;
    
    @Override
    public MyResult performOperation(MyRequest request) {
        // Implementation using federation services
        return result;
    }
}
```

3. **Export via Hessian:**
```xml
<bean name="/my-custom-service" class="org.springframework.remoting.caucho.HessianServiceExporter">
    <property name="service" ref="myCustomServiceImpl" />
    <property name="serviceInterface" value="com.example.MyCustomService" />
</bean>
```

### Real-time Data Integration

**Incoming Data Handler:**
```java
@Controller
public class CustomDataController {
    
    @Autowired
    private VehicleLocationListener _vehicleLocationListener;
    
    @RequestMapping("/custom-feed")
    public void handleCustomFeed(@RequestBody CustomFeedData data) {
        VehicleLocationRecord record = convertToVehicleRecord(data);
        _vehicleLocationListener.handleVehicleLocationRecord(record);
    }
}
```

**GTFS-RT Feed Generation:**
```java
@RequestMapping("/custom-gtfs-rt")
public void generateCustomFeed(HttpServletResponse response) {
    GtfsRealtime.FeedMessage.Builder feedBuilder = GtfsRealtime.FeedMessage.newBuilder();
    
    // Build feed content
    populateFeed(feedBuilder);
    
    response.setContentType("application/x-protobuf");
    feedBuilder.build().writeTo(response.getOutputStream());
}
```

### Service Client Integration

**API WebApp Client Configuration:**
```xml
<bean id="transitDataService" class="org.springframework.remoting.caucho.HessianProxyFactoryBean">
    <property name="serviceUrl" value="http://federation-host:8080/remoting/transit-data-service" />
    <property name="serviceInterface" value="org.onebusaway.transit_data.services.TransitDataService" />
    <property name="readTimeout" value="60000" />
</bean>
```

## Configuration

### Spring Context (`application-context-webapp.xml`)

**Federation Engine Integration:**
```xml
<!-- Import core federation services -->
<import resource="classpath:org/onebusaway/transit_data_federation/application-context.xml"/>

<!-- Override with webapp-specific configurations -->
<bean id="cacheManager" class="org.springframework.cache.ehcache.EhCacheManagerFactoryBean">
    <property name="configLocation" value="classpath:ehcache-webapp.xml" />
</bean>
```

**JMX Configuration:**
```xml
<!-- Avoid naming conflicts in multi-webapp deployments -->
<bean id="mbeanServer" class="org.springframework.jmx.support.MBeanServerFactoryBean">
    <property name="defaultDomain" value="org.onebusaway.federation_webapp" />
</bean>
```

### Web Application (`web.xml`)

**Servlet Configuration:**
```xml
<!-- Spring context initialization -->
<listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>

<!-- Hessian service servlet -->
<servlet>
    <servlet-name>remoting</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
</servlet>

<servlet-mapping>
    <servlet-name>remoting</servlet-name>
    <url-pattern>/remoting/*</url-pattern>
</servlet-mapping>
```

**Database Configuration:**
```xml
<!-- JNDI data source -->
<resource-ref>
    <res-ref-name>jdbc/appDB</res-ref-name>
    <res-type>javax.sql.DataSource</res-type>
    <res-auth>Container</res-auth>
</resource-ref>
```

### Performance Tuning

**Hessian Configuration:**
- Configure connection pooling for client connections
- Set appropriate read/write timeouts based on operation complexity
- Enable compression for large data transfers

**Caching Strategy:**
```xml
<cache name="stops" maxElementsInMemory="10000" eternal="false" 
       timeToLiveSeconds="3600" timeToIdleSeconds="1800" />

<cache name="serviceDates" maxElementsInMemory="1000" eternal="false"
       timeToLiveSeconds="86400" />
```

**JVM Tuning:**
- Configure heap size based on federation engine requirements
- Use G1GC for better handling of large object graphs
- Monitor Hessian serialization performance

## Testing Approach

### Service Integration Testing

```java
@Test
public void testTransitDataServiceRemoting() {
    // Create Hessian client
    HessianProxyFactory factory = new HessianProxyFactory();
    TransitDataService service = (TransitDataService) factory.create(
        TransitDataService.class, 
        "http://localhost:8080/remoting/transit-data-service"
    );
    
    // Test service calls
    AgenciesWithCoverageBean agencies = service.getAgenciesWithCoverage();
    assertThat(agencies.getAgencies()).isNotEmpty();
}
```

### Real-time Data Testing

```java
@Test
public void testVehicleLocationIngestion() {
    VehicleLocationRecord record = new VehicleLocationRecord();
    record.setVehicleId("TEST_VEHICLE");
    record.setTimestamp(System.currentTimeMillis());
    
    // Send via controller
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setContent(serializeRecord(record));
    
    vehicleController.handleVehicleUpdate(request);
    
    // Verify processing
    VehicleStatus status = vehicleStatusService.getVehicleStatus("TEST_VEHICLE");
    assertThat(status).isNotNull();
}
```

### GTFS-RT Feed Validation

```java
@Test
public void testGtfsRealtimeFeedGeneration() {
    MockHttpServletResponse response = new MockHttpServletResponse();
    
    gtfsRealtimeController.generateTripUpdates(response);
    
    // Parse Protocol Buffer response
    GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(
        response.getContentAsByteArray()
    );
    
    assertThat(feed.getEntityCount()).isGreaterThan(0);
}
```

## Common Issues and Solutions

### Connection Timeouts
- **Symptom:** HessianRuntimeException on slow operations
- **Solution:** Increase read timeout in client configuration
- **Monitoring:** Log slow service calls for optimization

### Memory Usage
- **Symptom:** OutOfMemoryError during large data transfers
- **Solution:** Implement pagination for large result sets
- **Optimization:** Use streaming for bulk data operations

### Service Availability
- **Symptom:** Service unavailable during bundle updates
- **Solution:** Implement graceful degradation and health checks
- **Strategy:** Use circuit breaker pattern for external dependencies

## Security Considerations

### Service Access Control
- Configure IP-based access restrictions for administrative endpoints
- Implement API key validation for real-time data ingestion
- Use HTTPS for production deployments

### Data Validation
- Validate all incoming real-time data before processing
- Sanitize user inputs in controller endpoints
- Implement rate limiting for data ingestion endpoints