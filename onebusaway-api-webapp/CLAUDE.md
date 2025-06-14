# CLAUDE.md - OneBusAway API WebApp

This file provides guidance to Claude Code when working with the OneBusAway REST API module.

## Module Overview

The onebusaway-api-webapp is the primary REST API module that exposes transit data through multiple protocols:
- REST API (`/api/where/`) - Core transit data queries
- GTFS-Realtime API (`/api/gtfs_realtime/`) - Real-time feed exports  
- SIRI API (`/siri/`) - European transit standard interface

## Key Commands

### Development
- `mvn test` - Run API endpoint tests
- `mvn jetty:run` - Start development server on port 8080
- `mvn package` - Build WAR file for deployment

### Testing API Endpoints
- http://localhost:8080/onebusaway-api-webapp/api/where/config.json?key=test
- http://localhost:8080/onebusaway-api-webapp/api/where/agencies-with-coverage.json?key=test
- http://localhost:8080/onebusaway-api-webapp/api/gtfs_realtime/vehicle-positions?key=test&agencyId=1

## Architecture

### Action-Based REST Framework (Struts 2)

**Base Action Classes:**
- `ApiActionSupport` - Main base class for `/api/where/` endpoints
- `GtfsRealtimeActionSupport` - Base for Protocol Buffer responses
- `SiriAction` - Base for SIRI XML endpoints

**URL Mapping Pattern:**
```
/api:rest → org.onebusaway.api.actions.api.where.*
/siri:rest → org.onebusaway.api.actions.siri.*
```

### Multi-Format Response Support

**Supported Formats:** `.json`, `.xml`, `.csv`, `.pb` (Protocol Buffer), `.pbtext`

**Content Type Handlers:**
- `CustomJsonLibHandler` - JSON with JSONP callback support
- `CustomXStreamHandler` - XML serialization
- `CustomProtocolBufferHandler` - Binary Protocol Buffer format
- `CustomCsvHandler` - CSV data export

### Key API Endpoints

#### Core Transit Data (`/api/where/`)
- `StopsForLocationAction` - Geographic stop search with radius filtering
- `ArrivalsAndDeparturesForStopAction` - Real-time arrival predictions
- `RouteAction`, `StopAction`, `TripAction` - Individual entity queries
- `AgenciesWithCoverageAction` - Available transit agencies

#### GTFS-Realtime Feeds (`/api/gtfs_realtime/`)
- `VehiclePositionsForAgencyAction` - Live vehicle locations
- `TripUpdatesForAgencyAction` - Trip delay predictions
- `AlertsForAgencyAction` - Service disruption alerts

#### SIRI Interface (`/siri/`)
- `StopMonitoringAction` - Real-time stop information
- `VehicleMonitoringAction` - Vehicle tracking
- `StopPointsV2Action` - Stop discovery service

## Configuration Files

### Struts Configuration (`src/main/resources/struts.xml`)
- **Convention-based routing:** Actions automatically mapped based on class name and package
- **Multi-protocol support:** Different URL prefixes route to different action packages
- **Format negotiation:** File extensions determine response format

### Spring Context (`src/main/resources/data-sources.xml`)
**Remote Service Integration:**
```xml
<bean id="transitDataService" class="org.springframework.remoting.caucho.HessianProxyFactoryBean">
    <property name="serviceUrl" value="http://localhost:8080/onebusaway-transit-data-federation-webapp/remoting/transit-data-service" />
</bean>
```

**Database Configuration:**
- `dataSource` - JNDI-based primary database connection
- `archiveDataSource` - MySQL-based real-time data archival
- `agencyDataSource` - Agency-specific configuration storage

### Web Application (`src/main/webapp/WEB-INF/web.xml`)
- **CORS Support:** Cross-domain API access for web applications  
- **URL Rewriting:** Consistent endpoint formatting via `urlrewrite.xml`
- **JNDI Resources:** Database connection pooling configuration

## Development Patterns

### Adding New API Endpoints

1. **Create Action Class:**
```java
public class MyNewAction extends ApiActionSupport {
    @Override
    public DefaultHttpHeaders show() throws Exception {
        // Implementation using _transitDataService
        return setOkResponse(result);
    }
}
```

2. **URL Convention:** 
   - Class: `MyNewAction` → URL: `/api/where/my-new.json`
   - Method: `show()` → HTTP GET
   - Use `_transitDataService` for data access

3. **Response Handling:**
   - Return domain objects (beans) - serialization is automatic
   - Use `setOkResponse()`, `setNotFoundResponse()`, `setExceptionResponse()`

### API Versioning Strategy
- Version-specific serializers in `src/main/java/org/onebusaway/api/impl/`
- `DefaultV*BeanFactoryImpl` classes handle version-appropriate responses
- Legacy API support with deprecation warnings

### Error Handling Pattern
```java
try {
    // API logic
    return setOkResponse(result);
} catch (ServiceException ex) {
    return setExceptionResponse();
}
```

### Security and API Keys
- `ApiKeyInterceptor` validates all requests
- Rate limiting configurable per API key
- Agency-specific permission filtering

## Testing Approach

### Action-Level Testing
```java
@Test
public void testActionMethod() {
    MyAction action = new MyAction();
    action.setTransitDataService(mockService);
    DefaultHttpHeaders response = action.show();
    assertEquals(HttpStatus.SC_OK, response.getStatusCode());
}
```

### Mock Service Integration
- Use Mockito to mock `TransitDataService`
- Test response structure and HTTP status codes
- Validate Protocol Buffer serialization for GTFS-RT endpoints

### Test Data Patterns
- Mock beans in `src/test/resources/org/onebusaway/api/`
- Sample GTFS-RT Protocol Buffer responses
- Agency configuration test fixtures

## External Dependencies

### Service Integration
- **Primary:** `TransitDataService` via Hessian remoting to federation webapp
- **Database:** Multiple data sources for different data types
- **Cache:** Named cache managers for performance optimization

### Protocol Support
- **GTFS-Realtime:** Google Protocol Buffers for binary feeds
- **SIRI:** XML-based European transit standard
- **JSON/XML:** Standard REST API formats

## Common Issues

### Cross-Domain Access
- CORS headers configured in web.xml
- JSONP callback support for legacy browser compatibility

### Protocol Buffer Responses
- Binary format requires specific content-type headers
- Use `.pb` extension or `Accept: application/x-protobuf` header

### URL Rewriting
- Endpoint format enforced via `urlrewrite.xml`
- Parameter extraction from URL paths for RESTful design