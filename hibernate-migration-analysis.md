# OneBusAway Hibernate 6 Migration Analysis

## Executive Summary

OneBusAway currently uses Hibernate 5.6.15.Final and needs to migrate to Hibernate 6.x. This analysis identifies all modules with Hibernate dependencies, files requiring migration, and recommended migration strategy.

## Current Hibernate Usage

**Hibernate Version**: 5.6.15.Final (defined in parent POM)  
**EhCache Version**: 2.10.3 (hibernate-ehcache dependency)  
**Spring Version**: 5.3.29 (needs coordination with Hibernate 6)

## Module Dependency Analysis

### Direct Hibernate Dependencies

1. **onebusaway-container** - Core Hibernate integration
   - Dependencies: hibernate-ehcache (5.6.15.Final)
   - Purpose: Base container with caching and persistence support
   - Impact: HIGH - Foundation module used by many others

2. **onebusaway-gtfs-hibernate-spring** - GTFS data persistence
   - Dependencies: onebusaway-gtfs-hibernate (external)
   - Purpose: GTFS data loading and Spring integration
   - Impact: HIGH - Critical for data bundle creation

3. **onebusaway-hibernate-compat** - Compatibility layer ✅ (IN PROGRESS)
   - Dependencies: hibernate-core (6.6.2.Final)
   - Purpose: Migration compatibility support
   - Status: Foundation classes implemented:
     - `CriteriaAdapter` - JPA Criteria API wrapper
     - `QueryAdapter` - Native query migration helper  
     - `UserTypeAdapter` - Hibernate 6 UserType base class

### Modules with Transitive Hibernate Dependencies

4. **onebusaway-transit-data-federation-builder**
   - Via: onebusaway-gtfs-hibernate-spring
   - Purpose: Data bundle creation and processing
   - Impact: HIGH - Critical build tool

5. **onebusaway-users** - User management
   - Via: onebusaway-container (inherits SessionFactory)
   - Purpose: User authentication and profile management
   - Impact: MEDIUM - Contains Criteria API usage

6. **onebusaway-gtfs-realtime-archiver** - Real-time data archival
   - Via: onebusaway-container
   - Purpose: Stores GTFS-realtime feeds for analysis
   - Impact: MEDIUM - Contains Criteria API usage

7. **onebusaway-transit-data-federation** - Core federation logic
   - Via: onebusaway-container
   - Purpose: Real-time transit data processing
   - Impact: HIGH - Core functionality module

8. **onebusaway-alerts-persistence** - Service alerts storage
   - Via: onebusaway-container
   - Purpose: Manages transit service alerts
   - Impact: MEDIUM - Contains native SQL queries

9. **onebusaway-agency-metadata** - Agency configuration
   - Via: onebusaway-container
   - Purpose: Agency-specific settings and metadata
   - Impact: LOW - Simple entity storage

10. **onebusaway-geocoder** - Address geocoding cache
    - Via: onebusaway-container
    - Purpose: Caches geocoding results
    - Impact: LOW - Simple caching layer

### Deprecated/Disabled Modules (Analysis for completeness)
- onebusaway-admin-webapp (disabled)
- onebusaway-enterprise-webapp (disabled)

## Migration Impact Analysis

### 1. Hibernate Criteria API Usage (HIGH IMPACT)

**Files requiring migration:**
- `/Users/aaron/repos/oba-app-mods/onebusaway-users/src/main/java/org/onebusaway/users/impl/UserDaoImpl.java`
  - Lines 172-180: `createCriteria()` with Projections and Restrictions
  - Lines 186-193: Complex criteria with joins
- `/Users/aaron/repos/oba-app-mods/onebusaway-gtfs-realtime-archiver/src/main/java/org/onebusaway/gtfs_realtime/archiver/service/TripUpdateDaoImpl.java`
- `/Users/aaron/repos/oba-app-mods/onebusaway-gtfs-realtime-archiver/src/main/java/org/onebusaway/gtfs_realtime/archiver/service/VehiclePositionDaoImpl.java`
- Several DAO implementations in admin-webapp and transit-data-federation modules

**Migration Strategy**: Replace with JPA Criteria API or JPQL

### 2. createSQLQuery() Usage (MEDIUM IMPACT)

**Files requiring migration:**
- `/Users/aaron/repos/oba-app-mods/onebusaway-alerts-persistence/src/main/java/org/onebusaway/alerts/impl/ServiceAlertsPersistenceDB.java`
- `/Users/aaron/repos/oba-app-mods/onebusaway-admin-webapp/src/main/java/org/onebusaway/admin/service/bundle/task/GtfsArchiveTask.java`

**Migration Strategy**: Replace with `createNativeQuery()`

### 3. Custom UserType Implementations (HIGH IMPACT)

**Files requiring migration:**
- `/Users/aaron/repos/oba-app-mods/onebusaway-container/src/main/java/org/onebusaway/container/hibernate/EnumUserType.java`
  - Custom enum mapping UserType
  - Uses deprecated `EnhancedUserType` interface
  - Method signatures changed in Hibernate 6

**Migration Strategy**: 
- Upgrade to Hibernate 6 UserType interface
- Consider using `@Enumerated` annotation where possible
- Update method signatures for new SessionImplementor API

### 4. EhCache Configuration (MEDIUM IMPACT)

**Configuration files:**
- `/Users/aaron/repos/oba-app-mods/onebusaway-container/src/main/resources/org/onebusaway/container/ehcache.xml`
- `/Users/aaron/repos/oba-app-mods/onebusaway-container/src/main/resources/org/onebusaway/container/ehcache-terracotta.xml`
- `/Users/aaron/repos/oba-app-mods/onebusaway-container/src/test/resources/org/onebusaway/container/cache/ehcache-test.xml`

**Dependencies to update:**
- `hibernate-ehcache` dependency (discontinued in Hibernate 6)
- EhCache 2.x → 3.x migration required

**Migration Strategy**: 
- Replace with JCache (JSR-107) implementation
- Update cache configuration format
- Consider Caffeine or Redis as alternatives

### 5. Hibernate Properties Configuration (LOW IMPACT)

**Configuration files requiring updates:**
- `docker_app_server/config/onebusaway-api-webapp-data-sources.xml`
  - Property name changes (e.g., `hibernate.cache.provider_class`)
  - Dialect updates for newer versions

## Recommended Migration Order

### Phase 1: Foundation (CRITICAL)
1. **Update Parent POM** - Upgrade Hibernate version to 6.x
2. **Migrate onebusaway-container** - Core module with caching
   - Update EnumUserType to Hibernate 6 API
   - Replace hibernate-ehcache with JCache implementation
   - Update EhCache configuration
3. **Update onebusaway-hibernate-compat** - Already partially done
4. **Test build compatibility** - Ensure all modules compile

### Phase 2: Data Access Layer (HIGH PRIORITY)
1. **Migrate onebusaway-users** - User management DAO
   - Replace Criteria API usage with JPA Criteria or JPQL
   - Update query methods
2. **Migrate onebusaway-gtfs-hibernate-spring** - GTFS data access
   - Coordinate with external onebusaway-gtfs-hibernate dependency
   - May require upstream library updates
3. **Update configuration files** - Hibernate properties and dialects

### Phase 3: Service Modules (MEDIUM PRIORITY)
1. **onebusaway-gtfs-realtime-archiver** - Real-time data storage
2. **onebusaway-alerts-persistence** - Service alerts
3. **onebusaway-transit-data-federation** - Core federation logic
4. **onebusaway-agency-metadata** - Agency configuration
5. **onebusaway-geocoder** - Geocoding cache

### Phase 4: Build Tools (LOWER PRIORITY)
1. **onebusaway-transit-data-federation-builder** - Bundle creation tool

## Potential Blockers

### 1. External Dependencies
- **onebusaway-gtfs-hibernate** external library compatibility
- **Spring Framework** compatibility with Hibernate 6
- **Third-party libraries** using old Hibernate APIs

### 2. Complex Migrations
- **EnumUserType** requires significant refactoring
- **EhCache migration** may impact caching performance
- **Criteria API queries** need careful testing for equivalent functionality

### 3. Testing Requirements
- **Integration tests** for all DAO implementations
- **Performance testing** for caching layer changes
- **Data migration scripts** for any schema changes

## Migration Effort Estimation

- **Phase 1 (Foundation)**: 2-3 weeks
- **Phase 2 (Data Access)**: 3-4 weeks  
- **Phase 3 (Service Modules)**: 2-3 weeks
- **Phase 4 (Build Tools)**: 1 week
- **Testing & Validation**: 2-3 weeks

**Total Estimated Effort**: 10-13 weeks

## Success Criteria

1. All modules compile and build successfully
2. All existing tests pass
3. No performance regression in caching layer
4. Database schema remains compatible
5. External GTFS libraries continue to work
6. Real-time data processing maintains performance

## Detailed Technical Migration Examples

### 1. UserType Migration Example

**Current (Hibernate 5):**
```java
public class EnumUserType implements EnhancedUserType, ParameterizedType {
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
        throws HibernateException, SQLException {
        String name = rs.getString(names[0]);
        return rs.wasNull() ? null : Enum.valueOf(enumClass, name);
    }
}
```

**Migrated (Hibernate 6):**
```java
public class EnumUserType extends UserTypeAdapter<Enum> {
    @Override
    public Class<Enum> returnedClass() {
        return enumClass;
    }
    
    @Override
    protected String convertToString(Enum value) {
        return value.name();
    }
    
    @Override
    protected Enum convertFromString(String value) {
        return Enum.valueOf(enumClass, value);
    }
}
```

### 2. Criteria API Migration Example

**Current (Hibernate 5):**
```java
Criteria criteria = getSession().createCriteria(User.class)
    .createCriteria("userIndices")
    .add(Restrictions.eq("id.type", keyType))
    .setProjection(Projections.rowCount());
```

**Migrated (JPA Criteria):**
```java
CriteriaAdapter adapter = new CriteriaAdapter(entityManager);
CriteriaQuery<Long> query = adapter.createQueryWithWhere(Long.class, context -> {
    Join<User, UserIndex> join = context.getRoot().join("userIndices");
    context.getQuery().select(context.getCriteriaBuilder().count(context.getRoot()));
    return context.getCriteriaBuilder().equal(join.get("id").get("type"), keyType);
});
```

### 3. createSQLQuery Migration Example

**Current (Hibernate 5):**
```java
SQLQuery query = getSession().createSQLQuery("SELECT * FROM alerts WHERE active = 1");
```

**Migrated (Hibernate 6):**
```java
QueryAdapter adapter = new QueryAdapter(getSession());
Query query = adapter.createNativeQuery("SELECT * FROM alerts WHERE active = 1");
```

## Critical Configuration Updates Required

### 1. Hibernate Properties Migration
```diff
-<prop key="hibernate.cache.provider_class">org.hibernate.cache.internal.NoCachingRegionFactory</prop>
+<prop key="hibernate.cache.region.factory_class">org.hibernate.cache.jcache.internal.JCacheRegionFactory</prop>

-<prop key="hibernate.dialect">org.hibernate.dialect.MySQLDialect</prop>
+<prop key="hibernate.dialect">org.hibernate.dialect.MySQL8Dialect</prop>
```

### 2. Dependency Updates Required
```xml
<!-- Remove Hibernate 5 EhCache integration -->
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-ehcache</artifactId>
    <version>5.6.15.Final</version>
</dependency>

<!-- Add JCache implementation -->
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-jcache</artifactId>
    <version>6.6.2.Final</version>
</dependency>
<dependency>
    <groupId>org.ehcache</groupId>
    <artifactId>ehcache</artifactId>
    <version>3.10.8</version>
</dependency>
```

## Risk Assessment Matrix

| Component | Risk Level | Impact | Mitigation Strategy |
|-----------|------------|--------|-------------------|
| EnumUserType | HIGH | Core functionality | Use compatibility layer + gradual migration |
| Criteria API | MEDIUM | Query performance | Extensive testing + fallback queries |
| EhCache 2→3 | MEDIUM | Caching performance | Staged rollout + monitoring |
| External GTFS libs | HIGH | Data bundle creation | Coordinate with upstream |
| createSQLQuery | LOW | Limited usage | Direct replacement |

## Testing Strategy

### 1. Unit Test Requirements
- All DAO implementations with Hibernate usage
- UserType implementations with various data types
- Cache configuration and performance
- Query result consistency verification

### 2. Integration Test Requirements  
- End-to-end data bundle creation process
- Real-time data processing pipeline
- User authentication and session management
- Cross-module dependency validation

### 3. Performance Test Requirements
- Database query performance comparison
- Cache hit/miss ratios and performance
- Memory usage analysis
- Real-time data processing throughput

## Migration Checklist

### Pre-Migration
- [ ] Create feature branch for Hibernate 6 migration
- [ ] Set up comprehensive test environment
- [ ] Document current performance baselines
- [ ] Identify all external library dependencies
- [ ] Plan rollback strategy

### Phase 1 - Foundation
- [ ] Update parent POM to Hibernate 6.x
- [ ] Migrate onebusaway-container module
- [ ] Update EnumUserType implementation
- [ ] Replace hibernate-ehcache with JCache
- [ ] Update all Hibernate configuration files
- [ ] Verify module compilation

### Phase 2 - Data Access Layer
- [ ] Migrate UserDaoImpl Criteria API usage
- [ ] Update onebusaway-gtfs-hibernate-spring
- [ ] Test GTFS data loading functionality
- [ ] Verify user management operations
- [ ] Performance test data access layer

### Phase 3 - Service Modules
- [ ] Migrate remaining DAO implementations
- [ ] Update native SQL query usage
- [ ] Test real-time data archival
- [ ] Verify service alerts functionality
- [ ] Test geocoding cache operations

### Phase 4 - Validation
- [ ] Full integration test suite execution
- [ ] Performance regression testing
- [ ] Cross-browser compatibility testing
- [ ] Documentation updates
- [ ] Deployment preparation

## Next Steps

1. **Immediate (Week 1-2)**
   - Create Hibernate 6 migration branch
   - Complete Phase 1 foundation migration
   - Set up automated testing pipeline

2. **Short-term (Week 3-6)**
   - Migrate core data access components
   - Implement comprehensive test coverage
   - Begin performance validation

3. **Medium-term (Week 7-10)** 
   - Complete service module migrations
   - Full integration testing
   - Performance optimization

4. **Long-term (Week 11-13)**
   - Production deployment preparation
   - Documentation and training
   - Post-migration monitoring setup