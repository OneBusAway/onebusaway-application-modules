# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Important: there are other CLAUDE.md files in subdirectories. When working on repository-wide changes, be sure to read all of them before starting work.

## 🚀 Active Project: Spring Boot Upgrade

**CRITICAL**: This repository is undergoing a Spring Boot 3.x upgrade. See `spring-boot-upgrade.md` for the complete migration plan and current status.

**Current State**: Phase 1 Foundation completed - Spring Boot infrastructure in place with backward compatibility
**Next Phase**: Configuration Migration (XML to Java-based @Configuration classes)
**Key Files**:
- `spring-boot-upgrade.md` - Complete migration plan and status
- `src/main/resources/config/application*.yml` - Spring Boot profiles
- `onebusaway-api-webapp/src/main/java/org/onebusaway/api/` - Spring Boot application classes

## Project Overview

OneBusAway is a comprehensive transit information system that provides real-time public transit data through multiple interfaces (web, mobile apps, REST API). The system is built with a modular, federated architecture using Java, Spring Framework, Apache Struts 2, and Maven for build management.

**Architecture Note**: Currently migrating from Spring Framework 5.3.29 + Struts 2 to Spring Boot 3.2.12 + Spring MVC in a phased approach to maintain stability.

### Spring Boot Upgrade Guidelines for AI Agents

**Context for Continuation**:
1. **Approach**: Incremental, backward-compatible migration preserving all existing functionality
2. **Phase 1 Completed**: Spring Boot foundation, profiles, and basic application classes established
3. **Key Principle**: No breaking changes - existing XML configs and Struts actions must continue working
4. **Testing Required**: Run `./make.sh` after each change to ensure 26 modules still build successfully
5. **Critical Files to Preserve**: All existing `/WEB-INF/web.xml`, `struts.xml`, and Spring XML context files

**Next Phase Tasks** (in order):
1. Create Java `@Configuration` classes parallel to existing XML configs
2. Gradually migrate Spring bean definitions from XML to Java
3. Maintain dual XML/Java config until complete
4. Convert Struts 2 actions to Spring MVC `@RestController` classes
5. Update URL mappings and request handling

**Migration Patterns Established**:
- Spring Boot auto-configuration excluded where OneBusAway patterns exist
- Properties externalized to `application-{profile}.yml` files
- CORS and security configured in Spring Boot style
- JAX-RS dependencies maintained for backward compatibility

## Development Commands

### Building the Project
- `./make.sh` - Build the entire project with default settings (skips tests and javadoc)
- `./make.sh --clean` - Clean build from scratch
- `./make.sh --test` - Build and run tests
- `./make.sh --help` - Show all build options

### Docker Development Environment
- `docker compose up builder` - Start the development environment
- `bin/make` - Build project inside Docker container
- `bin/build_bundle` - Build GTFS data bundle for testing
- `bin/copy_and_relaunch` - Deploy built artifacts to Tomcat
- `bin/shell` - Enter Docker container for debugging

### Testing
- `mvn test` - Run unit tests (excludes integration tests in org.onebusaway.integration)
- `mvn integration-test` - Run integration tests only
- Tests use JUnit 4 and Mockito for mocking

### Local Development URLs
- http://localhost:8080/manager/html - Tomcat Manager (admin/admin)
- http://localhost:8080/onebusaway-api-webapp/api/where/config.json?key=test - API health check

## Architecture Overview

### Core Module Structure

**onebusaway-transit-data** - Core data model and service interfaces
- `TransitDataService.java` - Primary service interface separating UI from data providers
- Bean-based data model: `StopBean`, `RouteBean`, `TripBean`, `ArrivalAndDepartureBean`
- Federation support via `@FederatedBy...` annotations

**onebusaway-transit-data-federation** - Real-time data processing engine
- `BlockLocationService` - Vehicle location and block assignment management
- `ArrivalAndDepartureService` - Real-time arrival prediction generation
- GTFS-realtime integration in `impl/realtime/gtfs_realtime/`

**onebusaway-api-webapp** - REST API endpoints
- Struts 2-based actions in `actions/api/where/`
- Supports JSON, XML, CSV, Protocol Buffer output formats
- CORS enabled for cross-domain access

**onebusaway-transit-data-federation-builder** - Data bundle creation
- `FederatedTransitDataBundleCreatorMain.java` - Main entry point
- Processes GTFS data into optimized runtime bundles
- Creates search indices and geospatial data structures

### Real-time Data Flow
1. `GtfsRealtimeSource` fetches GTFS-realtime feeds (Trip Updates, Vehicle Positions, Alerts)
2. Data processed through Protocol Buffer parsers
3. `BlockLocationService` updates vehicle locations
4. `ArrivalAndDepartureService` generates predictions
5. API endpoints serve real-time data to clients

### Federation Architecture
- Multiple OneBusAway instances can be federated across regions
- Routing based on agency ID and geographic bounds
- Seamless multi-agency deployments

## Configuration

### Data Sources
- Static GTFS data configured via `GTFS_URL` environment variable
- Real-time feeds configured in `docker_app_server/config/onebusaway-transit-data-federation-webapp-data-sources.xml`
- Database settings in Spring data source configurations

### Real-time Feed Configuration
Update the following properties in data-sources.xml:
- `tripUpdatesUrl` - GTFS-realtime Trip Updates feed
- `vehiclePositionsUrl` - GTFS-realtime Vehicle Positions feed
- `alertsUrl` - GTFS-realtime Alerts feed
- `agencyId` - Transit agency identifier
- `headersMap` - API keys for authenticated feeds

### Changing Data Sources
1. Delete `./docker_app_server/bundle/*`
2. Update `GTFS_URL` in docker-compose.yml
3. Update real-time feed URLs in data-sources.xml
4. Run `bin/build_bundle` to create new data bundle
5. Run `bin/copy_and_relaunch` to deploy changes

## Key Service Interfaces

### Primary Services
- **TransitDataService** - Main service interface for all transit data operations
- **BlockLocationService** - Real-time vehicle tracking and location services
- **RouteService/StopService** - Static GTFS data access
- **ArrivalAndDepartureService** - Real-time arrival predictions

### Federation Services
- Services annotated with `@FederatedByAgencyId`, `@FederatedByLocation`, etc.
- Automatic routing to appropriate backend instances
- Geographic and agency-based partitioning

## Common Development Patterns

### Adding New API Endpoints
1. Create Struts 2 Action class in `onebusaway-api-webapp/src/main/java/org/onebusaway/api/actions/api/where/`
2. Extend appropriate base action class
3. Configure URL mapping in struts.xml
4. Use `TransitDataService` for data access
5. Return appropriate model objects (beans)

### Working with Real-time Data
- Implement `VehicleLocationListener` for custom real-time processing
- Use `BlockLocationService` to query current vehicle states
- Real-time updates are processed asynchronously

### Testing Approach
- Unit tests for individual components using JUnit 4
- Integration tests in separate `org.onebusaway.integration` package
- Mock external dependencies using Mockito
- Test GTFS-realtime integration with sample feeds

## Build System Details

- Maven multi-module project with parent POM
- Java 11 target (configured in maven.compiler.source/target)
- Skip tests by default in development builds
- Integration tests run separately from unit tests
- GPG signing configured for Maven Central deployment

## Dependencies

### Key Frameworks
- Spring Framework 5.2.24.RELEASE - Dependency injection and web services
- Apache Struts 2.5.33 - Web MVC framework for API endpoints
- Hibernate 5.4.24.Final - ORM for database persistence
- Jackson for JSON serialization
- Google Protocol Buffers for GTFS-realtime parsing

### Database Support
- MySQL (primary), PostgreSQL, HSQLDB
- Connection pooling via Commons DBCP
- JPA/Hibernate for data access

### Geospatial Processing
- JTS (Java Topology Suite) for geographic calculations
- Custom UTM projection utilities
- Spatial indexing for stop and route lookups