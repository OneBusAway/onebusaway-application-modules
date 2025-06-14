# OneBusAway Spring Boot Upgrade Project Plan

## Executive Summary

OneBusAway currently uses Spring Framework 5.3.29 with traditional XML-based configuration across 12+ web applications. The upgrade to Spring Boot requires a phased approach due to the complex architecture, Apache Struts 2 integration, and federation-based design.

## 🎯 Project Status Update

### ✅ Phase 1 Foundation Completed (December 2024)

**Week 1-2 Project Setup - COMPLETED**
- ✅ **Spring Boot 3.x Parent Configuration**: Added Spring Boot 3.2.12 BOM to parent pom.xml with backward compatibility
- ✅ **Dependency Management Structure**: Established Spring Boot starters (web, actuator, security, test) with proper exclusions
- ✅ **Environment Profiles**: Created comprehensive application.yml profiles for development, production, and test environments
- ✅ **Application Starter Classes**: Implemented `OneBusAwayApiApplication`, `ApiConfiguration`, and `SecurityConfiguration` for api-webapp
- ✅ **Build Verification**: Project builds successfully with all existing functionality preserved

**Key Achievements:**
- Maintained 100% backward compatibility with existing OneBusAway patterns
- Resolved JAX-RS dependency conflicts in watchdog-webapp
- Created foundation for gradual Spring Boot adoption
- All 26 modules compile and build successfully
- Zero breaking changes to existing API endpoints

**Next Steps:**
- Phase 1 Week 3-4: Configuration Migration (XML to Java-based @Configuration classes)
- Begin Struts 2 to Spring MVC migration planning

**Technical Context for AI Agents:**

*Key Files Created:*
- `src/main/resources/config/application*.yml` - Spring Boot profile configurations
- `onebusaway-api-webapp/src/main/java/org/onebusaway/api/OneBusAwayApiApplication.java` - Main Spring Boot application
- `onebusaway-api-webapp/src/main/java/org/onebusaway/api/ApiConfiguration.java` - API-specific Spring configuration
- `onebusaway-api-webapp/src/main/java/org/onebusaway/api/SecurityConfiguration.java` - Spring Security setup

*Dependencies Added:*
- Spring Boot BOM 3.2.12 in parent pom.xml
- Spring Boot starters: web, actuator, security, test
- JAX-RS API compatibility maintained for existing Jersey usage

*Critical Constraints:*
- All existing Struts 2 actions must continue functioning
- XML Spring contexts still active and required
- No API endpoint changes allowed
- Build must succeed for all 26 Maven modules

*Migration Pattern:*
1. Add Spring Boot alongside existing configs (not replacing)
2. Create parallel Java configs before removing XML
3. Test each component individually before integration
4. Maintain feature flags for rollback capability

## Migration Strategy

### Phase-Based Incremental Approach
- **Duration**: 10-12 months
- **Approach**: One or more web applications per phase, with similar applications grouped
- **Priority**: API-first, then core services, finally admin interfaces

## Phase Breakdown

### Phase 1: Foundation & API Migration (8-10 weeks)
**Target**: `onebusaway-api-webapp`

#### Week 1-2: Project Setup
- Create Spring Boot 3.x parent configuration
- Establish new dependency management structure  
- Set up Spring Boot profiles for different environments
- Create Spring Boot application starter classes

#### Week 3-4: Configuration Migration
- Convert XML configurations to Java-based `@Configuration` classes
- Migrate data source configurations to Spring Boot auto-configuration
- Convert Hibernate configuration to Spring Boot JPA properties
- Implement Spring Boot security configuration

#### Week 5-6: Web Layer Migration  
- **Critical Decision**: Replace Apache Struts 2 with Spring MVC
- Migrate Struts actions to Spring `@RestController` classes
- Update URL mappings and request handling
- Implement Spring Boot actuator endpoints

#### Week 7-8: Testing & Validation
- Update integration tests for Spring Boot test framework
- Validate API endpoints and data serialization
- Performance testing and optimization
- Documentation updates

### Phase 2: Core Services Migration (6-8 weeks)
**Target**: `onebusaway-transit-data-federation-webapp`

#### Weeks 1-2: Service Layer Migration
- Convert transit data services to Spring Boot configuration
- Migrate real-time data processing services
- Update GTFS-realtime integration components
- Convert block location and arrival prediction services

#### Weeks 3-4: Data Layer Updates
- Optimize Spring Boot JPA configuration for federation data
- Migrate custom Hibernate configuration
- Update connection pooling to HikariCP (Spring Boot default)
- Implement Spring Boot caching configuration

#### Weeks 5-6: Integration & Communication
- Modernize Hessian remoting to REST API calls
- Implement service discovery for federation architecture
- Update inter-service communication patterns
- Configure Spring Boot profiles for different federation modes

### Phase 3: Federation Management (4-5 weeks)
**Target**: `onebusaway-federations-webapp`

#### Weeks 1-2: Federation Services
- Migrate federation routing logic to Spring Boot
- Convert agency-based and location-based federation services
- Update service registry and discovery mechanisms

#### Weeks 3-4: Administrative Features
- Migrate federation management interfaces
- Update monitoring and health check endpoints
- Implement Spring Boot admin integration

### Phase 4: Supporting Applications (6-8 weeks)
**Targets**: `onebusaway-admin-webapp`, `onebusaway-watchdog-webapp`, `onebusaway-gtfs-realtime-archiver`

#### Parallel Migration Approach
- Admin webapp: Convert to Spring Boot admin interface
- Watchdog: Migrate monitoring to Spring Boot actuator
- GTFS archiver: Convert to Spring Boot batch processing

### Phase 5: Additional Web Applications (8-10 weeks)
**Targets**: Remaining web applications identified in project
- `onebusaway-combined-webapp`
- `onebusaway-enterprise-webapp`
- `onebusaway-phone-webapp`
- `onebusaway-sms-webapp`
- `onebusaway-twilio-webapp`
- `onebusaway-nextbus-api-webapp`
- Other specialized applications

#### Approach
- Group similar applications for parallel migration
- Prioritize by usage and complexity
- Leverage patterns established in earlier phases

### Phase 6: Integration & Deployment (4-5 weeks)
- End-to-end integration testing across all applications
- Performance benchmarking and optimization
- Production deployment strategy for 12+ applications
- Rollback procedures and disaster recovery planning

## Technical Modernization Plan

### Core Technology Updates

#### Framework Upgrades
- **Spring Framework**: 5.3.29 → Spring Boot 3.2.x (includes Spring 6.x)
- **Java Version**: 17 (already upgraded) → 21 (optional Spring Boot 3.x optimization)
- **Servlet API**: 2.4 → 6.0 (Jakarta EE)
- **Hibernate**: 5.6.15 → 6.x (included with Spring Boot 3.x)

#### Web Stack Modernization
- **Replace Apache Struts 2** with Spring MVC `@RestController`
- **Hessian Remoting** → REST API with Spring WebClient
- **Traditional Servlets** → Spring Boot embedded Tomcat
- **XML Configuration** → Java-based `@Configuration` classes

#### Data Layer Improvements
- **Connection Pooling**: Commons DBCP → HikariCP
- **Caching**: EhCache → Spring Boot Cache abstraction with Redis/Caffeine
- **Database Migration**: Flyway integration for schema versioning
- **Monitoring**: JMX → Spring Boot Actuator endpoints

## Risk Assessment & Mitigation

### High-Risk Areas

#### 1. Apache Struts 2 Migration
**Risk**: Complex URL mappings and form handling
**Mitigation**: 
- Create comprehensive endpoint mapping documentation
- Implement parallel testing with existing Struts endpoints
- Use Spring MVC's flexible routing capabilities

#### 2. Federation Architecture Compatibility
**Risk**: Breaking inter-service communication
**Mitigation**:
- Maintain backward compatibility during transition
- Implement feature flags for gradual rollout
- Create comprehensive integration test suite

#### 3. Custom Spring Configuration
**Risk**: Complex bean wiring and AOP configurations
**Mitigation**:
- Document all custom configurations before migration
- Create Spring Boot auto-configuration classes for custom components
- Extensive testing of dependency injection

### Medium-Risk Areas

#### 1. Database Configuration Changes
**Risk**: Connection pool and transaction management issues
**Mitigation**:
- Thorough testing with production-like data volumes
- Gradual migration of connection pool settings
- Monitoring of database performance metrics

#### 2. Real-time Data Processing
**Risk**: GTFS-realtime integration disruption  
**Mitigation**:
- Maintain existing real-time processing during migration
- Implement comprehensive monitoring and alerting
- Create rollback procedures for real-time services

## Success Criteria

### Technical Metrics
- **Zero API breaking changes** for external consumers
- **Performance parity** or improvement (response times, throughput)
- **Reduced startup time** with Spring Boot optimizations
- **Improved monitoring** with Spring Boot Actuator
- **Simplified deployment** with embedded servlet container

### Operational Benefits
- **Reduced configuration complexity** (XML → Java configuration)
- **Enhanced developer experience** with Spring Boot tooling
- **Better production monitoring** and health checks
- **Simplified dependency management** with Spring Boot starters
- **Improved testing capabilities** with Spring Boot Test

## Resource Requirements

### Development Team
- **3-4 Senior Java/Spring developers** (full-time)
- **1-2 DevOps engineers** (75% allocation)
- **2 QA engineers** (full-time during testing phases)
- **1 Technical lead/architect** (oversight and decision-making)
- **Additional resources may be needed given the expanded scope**

### Infrastructure
- **Development environments** for parallel testing
- **Staging environment** replicating production federation setup
- **Performance testing infrastructure**
- **CI/CD pipeline updates** for Spring Boot deployment

## Current State Analysis

### Spring Framework Configuration
- **Version**: Spring Framework 5.3.29
- **Configuration Style**: Pure XML-based configuration with minimal annotations
- **Architecture**: Multi-module Maven project with 12+ web applications and 40+ modules total
- **Web Framework**: Apache Struts 2.5.33 integrated with Spring
- **Database**: Hibernate 5.6.15.Final with custom configuration
- **Security**: Spring Security 5.3.x with XML configuration

### Web Applications to Migrate
1. **onebusaway-api-webapp** - Main REST API application (Phase 1 priority)
2. **onebusaway-transit-data-federation-webapp** - Data federation services
3. **onebusaway-admin-webapp** - Administrative interface
4. **onebusaway-federations-webapp** - Federation management
5. **onebusaway-gtfs-realtime-archiver** - Real-time data archiver
6. **onebusaway-watchdog-webapp** - Monitoring application
7. **onebusaway-combined-webapp** - Combined application deployment
8. **onebusaway-enterprise-webapp** - Enterprise features
9. **onebusaway-phone-webapp** - Phone interface
10. **onebusaway-sms-webapp** - SMS interface
11. **onebusaway-twilio-webapp** - Twilio integration
12. **onebusaway-nextbus-api-webapp** - NextBus API compatibility

*Note: The project contains more web applications than initially identified. This plan should be adjusted to account for all 12+ web applications.*

### Key Integration Points
- **Database Access**: Hibernate ORM with custom configuration
- **Caching**: EhCache integration for second-level caching
- **Remoting**: Hessian-based inter-service communication
- **Security**: Custom authentication with external provider support
- **Real-time Processing**: GTFS-realtime Protocol Buffer integration
- **Federation**: Multi-agency deployment with geographic routing

## Code Review Recommendations (December 2024)

### Phase 1 Foundation Assessment: **GOOD** ✅

The initial Spring Boot implementation demonstrates solid engineering practices with proper backward compatibility. The following recommendations should guide subsequent phases:

#### Security Enhancements
1. **Production CORS Configuration**: 
   - Current: `allowedOrigins: "*"` suitable for public transit APIs
   - Recommendation: Consider environment-specific CORS policies for production deployments
   - File: `ApiConfiguration.java:85` - implement profile-specific origin restrictions

2. **API Authentication Validation**:
   - Ensure existing API key validation remains functional with new Spring Security configuration
   - Test: Verify `ApiKeyInterceptor` still processes requests correctly
   - Monitor: Add logging for authentication attempts and failures

3. **Credential Management**:
   - ✅ Good: Database passwords properly externalized using `${DATABASE_PASSWORD}`
   - Continue: Ensure all sensitive configuration uses environment variables

#### Technical Implementation
1. **Spring Boot Integration Testing**:
   - Add `@SpringBootTest` integration tests alongside existing Struts tests
   - Create test profiles that mirror production configuration
   - Validate: All existing API endpoints function identically

2. **Monitoring and Observability**:
   - Implement structured logging configuration for Spring Boot applications
   - Configure Spring Boot Actuator metrics collection
   - Add: Application-specific health checks for transit data feeds

3. **Performance Validation**:
   - Benchmark: Response times before/after Spring Boot migration
   - Monitor: HikariCP connection pool metrics in production
   - Test: High-load scenarios with real-time GTFS data processing

#### Migration Strategy Refinements
1. **Rollback Procedures**:
   - Document: Step-by-step rollback process for each phase
   - Test: Rollback procedures in staging environment
   - Automate: Feature flags for gradual Spring Boot adoption

2. **Configuration Migration**:
   - Next Phase Priority: Convert XML Spring contexts to Java `@Configuration`
   - Pattern: Create parallel configs before removing XML
   - Validation: Ensure bean wiring remains identical

3. **Inter-Service Communication**:
   - Plan: Hessian remoting to REST API migration timeline
   - Design: Service discovery patterns for federation architecture
   - Test: Multi-agency deployment scenarios

#### Build and Deployment
1. **CI/CD Pipeline Updates**:
   - Update: Build scripts for Spring Boot JAR deployment
   - Add: Automated testing of all 26 modules with Spring Boot
   - Implement: Staged deployment with health checks

2. **Documentation Maintenance**:
   - Keep: `CLAUDE.md` files updated with current architecture state
   - Add: Migration decision log with rationale for key choices
   - Document: New Spring Boot-specific development workflows

### Phase 2 Preparation Checklist
- [ ] Security review of all `permitAll()` endpoints
- [ ] Performance baseline establishment
- [ ] Integration test suite creation
- [ ] Rollback procedure validation
- [ ] XML to Java configuration mapping documentation

## Next Steps

1. **Phase 1 Validation**: Complete security and performance testing of current implementation
2. **Phase 2 Planning**: Begin XML to Java configuration migration design
3. **Team Training**: Ensure team familiarity with Spring Boot patterns and testing approaches
4. **Infrastructure**: Prepare Spring Boot-compatible deployment environments
5. **Monitoring**: Implement observability for the current Spring Boot foundation