# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

OneBusAway Application Modules - a multi-module Maven project providing real-time public transit information. The suite powers REST APIs, web interfaces, and integrations for transit agencies worldwide.

**Version:** 2.7.0
**Java:** 11
**Framework:** Spring 5.2.24 (not Spring Boot), Struts 2.5.33, Hibernate 5.4.24

## Build Commands

```bash
# Full build (skip tests for speed)
mvn clean install -DskipTests=true -Dmaven.javadoc.skip=true

# Build with tests
mvn clean install

# Build single module with dependencies
mvn clean package -pl onebusaway-api-key-cli -am

# Run tests for a single module
mvn test -pl onebusaway-api-webapp

# Run a single test
mvn test -Dtest=ConfigActionTest

# Run a single test method
mvn test -Dtest=ConfigActionTest#testIndex

# Verify build (includes license checks)
mvn verify
```

**Note:** If tests fail with protobuf-related errors (e.g., `NoSuchMethodError` on `ServiceAlerts` classes), run `mvn clean install -DskipTests` first to regenerate protobuf classes, then run tests.

### Docker Development (Recommended)

```bash
docker compose up builder          # Start build container + MySQL

# In another terminal:
bin/make                           # Full Maven build
bin/make --test                    # Build with tests
bin/build_bundle                   # Create GTFS data bundle
bin/copy_and_relaunch              # Deploy WARs to Tomcat
bin/shell                          # Shell into container
```

**Access points when running:**
- API: http://localhost:8080/onebusaway-api-webapp/api/where/config.json?key=test
- Tomcat Manager: http://localhost:8080/manager/html (admin/admin)
- Debug port: 5005

## Module Architecture

### Core Layers (bottom-up)

**Infrastructure:**
- `onebusaway-core` - Base classes, constants
- `onebusaway-util` - General utilities
- `onebusaway-container` - Spring context bootstrap
- `onebusaway-geospatial` - GIS utilities

**Data Layer:**
- `onebusaway-transit-data` - Domain models (stops, routes, trips)
- `onebusaway-gtfs-hibernate-spring` - GTFS database ORM
- `onebusaway-gtfs-realtime-model` - GTFS-realtime parsing
- `onebusaway-transit-data-federation` - Core data aggregation engine
- `onebusaway-transit-data-federation-builder` - Builds data bundles from GTFS

**Service Layer:**
- `onebusaway-api-core` - REST API implementation logic
- `onebusaway-realtime-api` - Real-time data interfaces
- `onebusaway-users` - User management, API keys
- `onebusaway-alerts-persistence` - Service alerts storage

**Web Applications (WAR):**
- `onebusaway-api-webapp` - Main REST API (most commonly modified)
- `onebusaway-transit-data-federation-webapp` - Admin/federation UI
- `onebusaway-gtfs-realtime-archiver` - Archives realtime data

**CLI Tools:**
- `onebusaway-api-key-cli` - Command-line API key management

### Data Flow

1. GTFS static data → `federation-builder` → MySQL database
2. GTFS-realtime feeds → `gtfs-realtime-model` → merged with static data
3. API requests → `api-webapp` (Struts actions) → `api-core` → `transit-data-federation` → response

## Key Configuration Files

**GTFS Realtime sources:**
`docker_app_server/config/onebusaway-transit-data-federation-webapp-data-sources.xml`
- Configure `tripUpdatesUrl`, `vehiclePositionsUrl`, `alertsUrl`, `agencyId`

**Docker environment:**
`docker-compose.yml`
- `GTFS_URL` - Static GTFS download URL
- `TZ` - Timezone matching GTFS data

## Testing

- Unit tests: `src/test/java/`
- Integration tests: `org.onebusaway.integration.*` (excluded by default)
- Test database: HSQLDB (in-memory)
- Test ports: 9900 (main), 9901 (AJP), 9902 (RMI)

## Contributing Notes

- Fork the repo and submit Pull Requests
- Reference GitHub issue numbers in commits: `Issue #5: Description`
- Run `mvn verify` before submitting to ensure license headers and tests pass
- Code style: https://github.com/OneBusAway/onebusaway/wiki/Code-Style
- License: Apache 2.0 (ICLA required for contributions)
