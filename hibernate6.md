# Hibernate 6 Migration Plan

## Executive Summary

This document outlines a phased approach to migrate OneBusAway from Hibernate 5.6.15.Final to Hibernate 6.x while minimizing system disruption. The migration will be coordinated with the ongoing Spring Boot 3.x upgrade and follows a similar incremental, backward-compatible approach.

## Current State Analysis

### Version Information
- **Current Version**: Hibernate 5.6.15.Final
- **Target Version**: Hibernate 6.6.x (latest stable)
- **Spring Integration**: Spring ORM 5.3.29
- **Spring Boot**: Migrating to 3.2.12 (includes Hibernate 6.4.x by default)

### Critical Dependencies
- hibernate-core: 5.6.15.Final
- hibernate-ehcache: 5.6.15.Final
- onebusaway-gtfs-hibernate: Uses Hibernate through onebusaway-gtfs

## Migration Challenges & Risks

### High-Impact Breaking Changes
1. **Legacy Criteria API Removal** (9 files affected)
   - Complete removal of `org.hibernate.Criteria`
   - Must migrate to JPA Criteria API
   - Affects core DAO implementations

2. **Custom UserType API Changes** (1 critical file)
   - `EnumUserType.java` requires significant refactoring
   - Method signatures changed in UserType interface

3. **EhCache Migration** (cache infrastructure)
   - EhCache 2.x support removed
   - Must migrate to JCache with EhCache 3.x

4. **SQLQuery to NativeQuery** (multiple files)
   - `createSQLQuery()` replaced with `createNativeQuery()`
   - API changes in query result handling

### Medium-Impact Changes
- Deprecated Hibernate annotations need updates
- Dialect class name changes
- Type registration mechanism changes
- Some HQL syntax updates required

## Phased Migration Strategy

### Phase 0: Preparation (1-2 weeks)
**Goal**: Set up parallel testing environment and compatibility layer

1. **Create Compatibility Module**
   ```
   onebusaway-hibernate-compat/
   ├── pom.xml
   ├── src/main/java/
   │   └── org/onebusaway/hibernate/compat/
   │       ├── CriteriaAdapter.java
   │       ├── QueryAdapter.java
   │       └── UserTypeAdapter.java
   ```

2. **Establish Testing Framework**
   - Create comprehensive test suite for all Hibernate operations
   - Set up dual-version testing (Hibernate 5 and 6)
   - Document current behavior for regression testing

3. **Dependency Analysis**
   - Create dependency tree of all Hibernate-using modules
   - Identify transitive dependencies that might conflict
   - Plan module upgrade order

### Phase 1: Core Infrastructure (2-3 weeks)
**Goal**: Update base configuration while maintaining backward compatibility

1. **Update Maven Dependencies**
   ```xml
   <!-- Add version property -->
   <hibernate.version>6.6.2.Final</hibernate.version>
   
   <!-- Update dependencies with compatibility checks -->
   <dependency>
     <groupId>org.hibernate.orm</groupId>
     <artifactId>hibernate-core</artifactId>
     <version>${hibernate.version}</version>
   </dependency>
   ```

2. **Create Dialect Compatibility Layer**
   - Map old dialect names to new ones
   - Handle MySQL8Dialect → MySQLDialect transition
   - Support environment-specific dialect selection

3. **Update SessionFactory Configuration**
   - Maintain dual configuration support
   - Update Spring's LocalSessionFactoryBean usage
   - Ensure transaction management compatibility

### Phase 2: Cache Migration (1-2 weeks)
**Goal**: Migrate from EhCache 2.x to JCache with EhCache 3.x

1. **Update Cache Dependencies**
   ```xml
   <dependency>
     <groupId>org.hibernate.orm</groupId>
     <artifactId>hibernate-jcache</artifactId>
     <version>${hibernate.version}</version>
   </dependency>
   <dependency>
     <groupId>org.ehcache</groupId>
     <artifactId>ehcache</artifactId>
     <version>3.10.8</version>
   </dependency>
   ```

2. **Migrate Cache Configuration**
   - Convert ehcache.xml to ehcache3 format
   - Update custom EhCacheRegionFactory
   - Test cache performance and behavior

3. **Update Cache Annotations**
   - Review all @Cache usage
   - Update cache region configurations
   - Validate cache strategies still appropriate

### Phase 3: Criteria API Migration (3-4 weeks)
**Goal**: Replace legacy Criteria API with JPA Criteria API

1. **Create Migration Utilities**
   ```java
   public class CriteriaMigrationHelper {
       public static <T> CriteriaQuery<T> migrateCriteria(
           Class<T> entityClass, 
           Consumer<CriteriaBuilder> builderConfig) {
           // Helper to ease migration
       }
   }
   ```

2. **Migrate DAO Implementations** (Priority Order)
   - UserDaoImpl.java
   - UserIndexDaoImpl.java
   - UserPropertiesDaoImpl.java
   - ServiceAlertsPersistenceDB.java
   - Other affected DAOs

3. **Update Query Patterns**
   ```java
   // Old pattern
   Criteria criteria = getSession().createCriteria(User.class);
   criteria.add(Restrictions.eq("username", username));
   
   // New pattern
   CriteriaBuilder cb = getSession().getCriteriaBuilder();
   CriteriaQuery<User> query = cb.createQuery(User.class);
   Root<User> root = query.from(User.class);
   query.where(cb.equal(root.get("username"), username));
   ```

### Phase 4: UserType and Query Updates (2-3 weeks)
**Goal**: Update custom types and SQL queries

1. **Migrate EnumUserType**
   - Update to new UserType interface
   - Implement new method signatures
   - Test with all enum mappings

2. **Update SQL Queries**
   - Replace createSQLQuery with createNativeQuery
   - Update result transformers
   - Test query performance

3. **Update Type Annotations**
   - Review all @Type usage
   - Update type registration
   - Validate custom type behavior

### Phase 5: Spring Boot Integration (1-2 weeks)
**Goal**: Align with Spring Boot 3.x Hibernate configuration

1. **Leverage Spring Boot Auto-configuration**
   - Review spring.jpa properties
   - Align with Spring Boot defaults
   - Remove redundant configurations

2. **Update Transaction Management**
   - Ensure @Transactional compatibility
   - Update transaction boundaries if needed
   - Test rollback scenarios

3. **Finalize Dual Configuration**
   - Ensure XML and Java configs both work
   - Document configuration precedence
   - Plan XML configuration retirement

### Phase 6: Testing and Stabilization (2-3 weeks)
**Goal**: Comprehensive testing and performance validation

1. **Regression Testing**
   - Run full test suite
   - Compare Hibernate 5 vs 6 behavior
   - Document any behavioral changes

2. **Performance Testing**
   - Benchmark query performance
   - Validate cache effectiveness
   - Check memory usage patterns

3. **Integration Testing**
   - Test with real GTFS data
   - Validate real-time data processing
   - Check federated deployments

### Phase 7: Rollout and Monitoring (1-2 weeks)
**Goal**: Safe production deployment

1. **Staged Rollout**
   - Deploy to development environment
   - Progressive rollout to staging
   - Production deployment with rollback plan

2. **Monitoring**
   - Set up Hibernate 6 metrics
   - Monitor query performance
   - Track cache hit rates

3. **Documentation**
   - Update developer guides
   - Document new patterns
   - Create troubleshooting guide

## Risk Mitigation Strategies

### 1. Compatibility Layer Approach
- Create adapter classes for smooth transition
- Minimize code changes during migration
- Allow gradual migration of components

### 2. Feature Flags
```java
@Configuration
public class HibernateConfig {
    @Value("${hibernate.use-legacy-criteria:false}")
    private boolean useLegacyCriteria;
    
    // Configuration based on flag
}
```

### 3. Parallel Testing
- Run tests against both Hibernate versions
- Automated comparison of results
- Early detection of behavioral changes

### 4. Rollback Plan
- Maintain ability to revert to Hibernate 5
- Keep compatibility layer until fully stable
- Document rollback procedures

## Coordination with Spring Boot Upgrade

### Dependencies
- Spring Boot 3.2.12 includes Hibernate 6.4.x
- Coordinate Hibernate upgrade with Spring Boot Phase 2
- Leverage Spring Boot's Hibernate auto-configuration

### Timing
- Begin Hibernate preparation during Spring Boot Phase 2
- Complete Hibernate migration before Spring Boot Phase 3
- Test integrated stack thoroughly

## Success Metrics

1. **Zero Functional Regression**
   - All existing features work identically
   - No data loss or corruption
   - API compatibility maintained

2. **Performance Targets**
   - Query performance within 5% of baseline
   - Cache hit rates maintained or improved
   - Memory usage stable or reduced

3. **Code Quality**
   - Reduced technical debt
   - Modern JPA patterns adopted
   - Improved maintainability

## Timeline Summary

- **Total Duration**: 12-16 weeks
- **Phase 0-2**: 4-6 weeks (Infrastructure)
- **Phase 3-4**: 5-7 weeks (Core Migration)
- **Phase 5-7**: 3-4 weeks (Integration & Rollout)

## Next Steps

1. **Immediate Actions**
   - Create compatibility module structure
   - Set up Hibernate 6 test environment
   - Begin comprehensive test suite creation

2. **Team Preparation**
   - Train team on JPA Criteria API
   - Review Hibernate 6 documentation
   - Assign module owners for migration

3. **Tooling Setup**
   - Configure IDE for Hibernate 6
   - Set up migration tracking dashboard
   - Prepare performance benchmarking tools

## Appendix: Breaking Changes Reference

### Critical API Changes
1. Criteria API: `org.hibernate.Criteria` → JPA `CriteriaQuery`
2. SQLQuery: `createSQLQuery()` → `createNativeQuery()`
3. UserType: New method signatures and lifecycle
4. Cache: EhCache 2.x → JCache with EhCache 3.x

### Configuration Changes
1. Dialect: Class name updates
2. Properties: Some property names changed
3. Annotations: Several deprecated annotations removed

### Behavioral Changes
1. Sequence generation defaults
2. Timestamp handling precision
3. Lazy loading proxies
4. Transaction isolation handling

## Additional Considerations and Gaps

### 1. GTFS-Hibernate Transitive Dependencies
**Gap**: The onebusaway-gtfs-hibernate module dependency chain needs careful analysis.
- **Action**: Audit onebusaway-gtfs for Hibernate version requirements
- **Mitigation**: May need to fork/update onebusaway-gtfs if it blocks Hibernate 6
- **Alternative**: Create compatibility shim if immediate upgrade isn't feasible

### 2. Database Schema Migration
**Gap**: Hibernate 6 changes in schema generation and validation may impact existing databases.
- **Sequence handling**: Default allocation size changed from 50 to 1
- **Timestamp precision**: Microsecond precision now default (was millisecond)
- **Column type mappings**: Some Java-to-SQL type mappings updated
- **Action**: 
  - Add schema validation phase before each environment deployment
  - Create migration scripts for sequence and timestamp adjustments
  - Test with production database copies

### 3. Performance Baseline Metrics
**Gap**: Need specific, measurable performance targets.
- **Establish baselines**:
  - Average query response time for key operations (stops, routes, arrivals)
  - Memory usage under typical load (heap, off-heap)
  - Cache hit rates per entity type
  - Database connection pool utilization
- **Target metrics**:
  - Query performance: ≤5% degradation
  - Memory usage: ≤10% increase
  - Cache hit rate: ≥95% of current rates
  - Connection pool: No increase in wait times

### 4. Automated Testing Strategy
**Gap**: Need comprehensive automated testing for Criteria API migration.
- **Test framework additions**:
  ```java
  @TestConfiguration
  public class DualHibernateTestConfig {
      // Run same tests against both Hibernate 5 and 6
      // Compare results automatically
  }
  ```
- **Criteria API test coverage**:
  - Generate tests from existing Criteria usage
  - Validate result sets match exactly
  - Performance comparison tests
  - Edge case handling (null values, empty results)

### 5. Batch Operation Handling
**Gap**: Batch processing changes in Hibernate 6 need attention.
- **Batch insert/update**: New batching algorithms may affect performance
- **JDBC batch size**: Validate current settings still optimal
- **Bulk operations**: HQL/JPQL bulk operations have subtle changes
- **Action**: 
  - Profile batch operations in data bundle creation
  - Test with typical GTFS file sizes
  - Adjust batch configurations as needed

### 6. Query Plan Cache Implications
**Gap**: Hibernate 6's improved query plan cache needs configuration review.
- **Plan cache size**: Default increased, may need tuning
- **Cache invalidation**: Different triggers than Hibernate 5
- **Action**: Monitor query plan cache hit rates and memory usage

### 7. Custom Type Registration
**Gap**: Beyond EnumUserType, other custom types may need updates.
- **Audit all @Type annotations**: Look for custom type implementations
- **Spatial types**: If using Hibernate Spatial, significant API changes
- **JSON types**: Common custom types for JSON storage need updates
- **Action**: Complete inventory of custom types before Phase 4

### 8. Transaction Timeout Handling
**Gap**: Transaction timeout behavior changed in Hibernate 6.
- **JTA vs Resource-local**: Different timeout mechanisms
- **Spring integration**: Ensure Spring's @Transactional timeout still works
- **Action**: Test long-running transactions (bundle building, bulk updates)

### 9. Second-Level Cache Warming
**Gap**: Cache warming strategies may need adjustment.
- **Startup performance**: Cold cache impact on first requests
- **Cache preloading**: Existing strategies may not work with JCache
- **Action**: Implement cache warming for critical entities

### 10. Monitoring and Observability
**Gap**: Need Hibernate 6-specific monitoring setup.
- **Metrics to add**:
  - Hibernate Statistics MBean changes
  - New JCache metrics
  - Query execution time percentiles
  - Entity load counts by type
- **Integration points**:
  - Micrometer for Spring Boot integration
  - JMX for operational monitoring
  - Custom health checks for cache status

## Resources

- [Hibernate 6 Migration Guide](https://docs.jboss.org/hibernate/orm/6.0/migration-guide/migration-guide.html)
- [Spring Boot 3 Hibernate Configuration](https://docs.spring.io/spring-boot/docs/3.2.x/reference/html/data.html#data.sql.jpa-and-hibernate)
- [JPA Criteria API Tutorial](https://docs.oracle.com/javaee/6/tutorial/doc/gjitv.html)
- [EhCache 3 Migration Guide](https://www.ehcache.org/documentation/3.0/migration-guide.html)