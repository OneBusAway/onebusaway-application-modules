# OneBusAway Hibernate 6 Migration - Quick Reference

## File Counts by Migration Type

### Criteria API Usage (HIGH PRIORITY)
**10 files** require migration from `createCriteria()` to JPA Criteria API:

1. `/Users/aaron/repos/oba-app-mods/onebusaway-users/src/main/java/org/onebusaway/users/impl/UserDaoImpl.java`
2. `/Users/aaron/repos/oba-app-mods/onebusaway-gtfs-realtime-archiver/src/main/java/org/onebusaway/gtfs_realtime/archiver/service/TripUpdateDaoImpl.java`
3. `/Users/aaron/repos/oba-app-mods/onebusaway-gtfs-realtime-archiver/src/main/java/org/onebusaway/gtfs_realtime/archiver/service/VehiclePositionDaoImpl.java`
4. `/Users/aaron/repos/oba-app-mods/onebusaway-admin-webapp/src/main/java/org/onebusaway/admin/service/bundle/impl/BundleBuildResponseDaoImpl.java` (disabled)
5. `/Users/aaron/repos/oba-app-mods/onebusaway-admin-webapp/src/main/java/org/onebusaway/admin/service/bundle/impl/GtfsArchiveDaoImpl.java` (disabled)
6. `/Users/aaron/repos/oba-app-mods/onebusaway-admin-webapp/src/main/java/org/onebusaway/admin/service/assignments/impl/AssignmentConfigDaoImpl.java` (disabled)
7. `/Users/aaron/repos/oba-app-mods/onebusaway-admin-webapp/src/main/java/org/onebusaway/admin/service/impl/UserManagementServiceImpl.java` (disabled)
8. `/Users/aaron/repos/oba-app-mods/onebusaway-transit-data-federation/src/main/java/org/onebusaway/transit_data_federation/impl/realtime/BlockLocationRecordDaoImpl.java`
9. `/Users/aaron/repos/oba-app-mods/onebusaway-transit-data-federation/src/main/java/org/onebusaway/transit_data_federation/impl/reporting/UserReportingDaoImpl.java`
10. `/Users/aaron/repos/oba-app-mods/onebusaway-hibernate-compat/src/test/java/org/onebusaway/hibernate/compat/HibernateUsageAnalyzer.java` (test file)

### createSQLQuery Usage (MEDIUM PRIORITY)
**4 files** require migration from `createSQLQuery()` to `createNativeQuery()`:

1. `/Users/aaron/repos/oba-app-mods/onebusaway-alerts-persistence/src/main/java/org/onebusaway/alerts/impl/ServiceAlertsPersistenceDB.java`
2. `/Users/aaron/repos/oba-app-mods/onebusaway-admin-webapp/src/main/java/org/onebusaway/admin/service/bundle/task/GtfsArchiveTask.java` (disabled)
3. `/Users/aaron/repos/oba-app-mods/onebusaway-hibernate-compat/src/test/java/org/onebusaway/hibernate/compat/HibernateUsageAnalyzer.java` (test file)
4. `/Users/aaron/repos/oba-app-mods/onebusaway-hibernate-compat/src/main/java/org/onebusaway/hibernate/compat/QueryAdapter.java` (compatibility layer)

### UserType Implementations (HIGH PRIORITY)
**8 files** contain custom UserType implementations:

1. `/Users/aaron/repos/oba-app-mods/onebusaway-container/src/main/java/org/onebusaway/container/hibernate/EnumUserType.java`
2. `/Users/aaron/repos/oba-app-mods/onebusaway-transit-data-federation/src/main/java/org/onebusaway/transit_data_federation/impl/realtime/BlockLocationRecord.java`
3. `/Users/aaron/repos/oba-app-mods/onebusaway-transit-data-federation/src/main/java/org/onebusaway/transit_data_federation/impl/realtime/history/BlockLocationArchiveRecord.java`
4. `/Users/aaron/repos/oba-app-mods/onebusaway-transit-data-federation/src/main/java/org/onebusaway/transit_data_federation/impl/reporting/StopProblemReportRecord.java`
5. `/Users/aaron/repos/oba-app-mods/onebusaway-transit-data-federation/src/main/java/org/onebusaway/transit_data_federation/impl/reporting/TripProblemReportRecord.java`
6. `/Users/aaron/repos/oba-app-mods/onebusaway-hibernate-compat/src/test/java/org/onebusaway/hibernate/compat/HibernateUsageAnalyzer.java` (test file)
7. `/Users/aaron/repos/oba-app-mods/onebusaway-hibernate-compat/src/test/java/org/onebusaway/hibernate/compat/HibernateMigrationTestFramework.java` (test file)
8. `/Users/aaron/repos/oba-app-mods/onebusaway-hibernate-compat/src/main/java/org/onebusaway/hibernate/compat/UserTypeAdapter.java` (compatibility layer)

## Priority Modules (Active in build)

### Phase 1 - Critical Foundation
1. **onebusaway-container** - Contains EnumUserType and EhCache integration
2. **onebusaway-hibernate-compat** - Already partially migrated

### Phase 2 - High Impact Active Modules
1. **onebusaway-users** - User management with complex Criteria API usage
2. **onebusaway-gtfs-realtime-archiver** - Real-time data storage
3. **onebusaway-transit-data-federation** - Core federation logic
4. **onebusaway-alerts-persistence** - Service alerts with native SQL

### Phase 3 - Supporting Modules
1. **onebusaway-gtfs-hibernate-spring** - GTFS data persistence
2. **onebusaway-transit-data-federation-builder** - Build tool
3. **onebusaway-agency-metadata** - Simple entity storage
4. **onebusaway-geocoder** - Geocoding cache

## Configuration Files to Update

### EhCache Configurations
1. `/Users/aaron/repos/oba-app-mods/onebusaway-container/src/main/resources/org/onebusaway/container/ehcache.xml`
2. `/Users/aaron/repos/oba-app-mods/onebusaway-container/src/main/resources/org/onebusaway/container/ehcache-terracotta.xml`
3. `/Users/aaron/repos/oba-app-mods/onebusaway-container/src/test/resources/org/onebusaway/container/cache/ehcache-test.xml`

### Hibernate Properties
1. `docker_app_server/config/onebusaway-api-webapp-data-sources.xml`

## Estimated Migration Effort by File

### High Complexity (2-4 hours each)
- **EnumUserType.java** - Complete interface migration
- **UserDaoImpl.java** - Multiple complex Criteria API queries

### Medium Complexity (1-2 hours each)
- **TripUpdateDaoImpl.java** - Criteria API migration
- **VehiclePositionDaoImpl.java** - Criteria API migration
- **BlockLocationRecordDaoImpl.java** - Criteria API migration
- **ServiceAlertsPersistenceDB.java** - Native SQL migration

### Low Complexity (30 minutes each)
- Entity classes with UserType annotations
- Simple DAO implementations
- Configuration file updates

## Quick Start Migration Commands

```bash
# 1. Create migration branch
git checkout -b hibernate-6-migration

# 2. Update parent POM Hibernate version
sed -i 's/5.6.15.Final/6.6.2.Final/g' pom.xml

# 3. Update container module dependencies
cd onebusaway-container
# Edit pom.xml to replace hibernate-ehcache with hibernate-jcache

# 4. Run build to identify compilation issues
cd ..
./make.sh --test
```

## Testing Strategy

### Unit Tests (Immediate)
```bash
# Test specific modules
mvn test -pl onebusaway-container
mvn test -pl onebusaway-users
mvn test -pl onebusaway-hibernate-compat
```

### Integration Tests (Post-migration)
```bash
# Full integration test suite
mvn integration-test
```

### Performance Validation
```bash
# Monitor query performance
# Setup JProfiler or similar APM tool
# Compare before/after metrics
```

## Rollback Plan

1. **Git branch strategy** - Keep main branch stable
2. **Configuration rollback** - Maintain separate config files
3. **Dependency rollback** - Use Maven profiles for version selection
4. **Database compatibility** - Ensure schema remains compatible

## Success Metrics

- [ ] All 26 modules compile successfully
- [ ] Zero test failures in existing test suite
- [ ] No performance regression > 10%
- [ ] Zero runtime Hibernate-related exceptions
- [ ] Successful data bundle creation
- [ ] Real-time data processing operational