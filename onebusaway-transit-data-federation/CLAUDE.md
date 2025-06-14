# CLAUDE.md - OneBusAway Transit Data Federation

This file provides guidance to Claude Code when working with the OneBusAway real-time transit processing engine.

## Module Overview

The onebusaway-transit-data-federation is the core real-time processing engine that transforms raw GTFS static data and live feeds into arrival predictions and vehicle tracking information. It handles thousands of vehicles in real-time while maintaining sub-second response times.

## Key Commands

### Development
- `mvn test` - Run real-time processing tests
- `mvn test -Dtest=*IntegrationTest` - Run integration tests with sample GTFS-RT feeds
- `mvn package` - Build processing engine JAR

### Testing Real-time Processing
- Use sample GTFS-RT feeds in `src/test/resources/org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/`
- Test vehicle matching algorithms with mock data

## Architecture Overview

### Real-Time Processing Pipeline

1. **Input Layer:** GTFS-realtime feeds (Vehicle Positions, Trip Updates, Alerts)
2. **Matching Layer:** Vehicle-to-block matching using fuzzy algorithms
3. **Processing Layer:** Schedule deviation calculation and prediction generation
4. **Cache Layer:** Multi-level caching with configurable TTLs
5. **Output Layer:** Processed data for API consumption

### Core Processing Engine

**Block-Based Architecture:**
- Vehicles tracked as part of "blocks" (sequences of trips)
- Enables proper handling of interlining and layovers
- Block sequences maintain service continuity across multiple routes

**Data Flow:**
```
GTFS-RT Feed → GtfsRealtimeServiceImpl → BlockLocationServiceImpl → ArrivalAndDepartureServiceImpl → API
```

## Core Services

### Real-Time Data Processing (`impl/realtime/`)

**Primary Services:**
- `GtfsRealtimeServiceImpl` - Main GTFS-RT processor converting feeds to OneBusAway format
- `BlockLocationServiceImpl` - Central vehicle tracking service managing real-time locations
- `VehicleStatusServiceImpl` - Vehicle status management and lifecycle
- `VehicleLocationRecordCacheImpl` - High-performance in-memory vehicle cache

**Key Algorithms:**
- `GtfsRealtimeTripLibrary` - Core library for matching real-time data to static schedule
- `BlockFinder` - Fuzzy matching algorithm for finding blocks from partial data
- `BlockLibrary` - Block manipulation and sequence management algorithms

### Arrival Prediction Engine (`impl/`)

**Core Components:**
- `ArrivalAndDepartureServiceImpl` - Main prediction engine combining static and real-time data
- `StopTimeServiceImpl` - Stop time scheduling logic and interpolation
- `DeviationModel` - Statistical model for schedule adherence using historical data

**Prediction Strategy:**
1. **Vehicle Position Matching:** Match vehicles to scheduled blocks using trip ID, vehicle ID, and proximity
2. **Schedule Deviation Calculation:** Calculate how far ahead/behind schedule each vehicle is
3. **Prediction Propagation:** Apply deviations to future stops along the block sequence
4. **Uncertainty Modeling:** Factor in historical accuracy and real-time data quality

### Bundle Management (`impl/bundle/`)

**Bundle Services:**
- `BundleManagementServiceImpl` - Hot-swapping of GTFS data bundles without service interruption
- `BundleSearchServiceImpl` - Bundle discovery, validation, and versioning

**Storage Backends:**
- `LocalBundleStoreImpl` - Local file system storage
- `S3BundleStoreImpl` - AWS S3 cloud storage
- `HttpBundleStoreImpl` - HTTP remote storage with caching

## Development Patterns

### Adding Real-Time Data Sources

1. **Create Feed Processor:**
```java
@Component
public class CustomRealtimeSource implements VehicleLocationListener {
    @Override
    public void handleVehicleLocationRecord(VehicleLocationRecord record) {
        // Process custom real-time data format
        _blockLocationService.updateVehicleLocation(record);
    }
}
```

2. **Register with Processing Engine:**
```xml
<bean class="org.onebusaway.transit_data_federation.impl.realtime.CustomRealtimeSource">
    <property name="blockLocationService" ref="blockLocationService" />
</bean>
```

### Extending Prediction Algorithms

**Custom Prediction Models:**
- Extend `PredictionEngine` interface for agency-specific algorithms
- Override `ArrivalAndDepartureServiceImpl` methods for custom logic
- Use `DeviationModel` framework for historical learning

**Vehicle Matching Customization:**
- Implement custom `BlockFinder` strategies for unique vehicle ID formats
- Add agency-specific matching rules in `GtfsRealtimeTripLibrary`

### Performance Optimization

**Cache Configuration:**
```java
// Configure cache TTL based on vehicle type
@Value("${cache.vehicle.bus.ttl:30}")
private int busCacheTtl;

@Value("${cache.vehicle.rail.ttl:10}")  
private int railCacheTtl;
```

**Batch Processing:**
```java
// Process multiple vehicle updates in batch
List<VehicleLocationRecord> batch = collectBatch();
_blockLocationService.updateVehicleLocations(batch);
```

## Configuration

### Real-Time Feed Configuration

**GTFS-Realtime Sources:**
```xml
<bean class="org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource">
    <property name="tripUpdatesUrl" value="http://example.com/tripupdates.pb" />
    <property name="vehiclePositionsUrl" value="http://example.com/vehiclepositions.pb" />
    <property name="alertsUrl" value="http://example.com/alerts.pb" />
    <property name="refreshInterval" value="30" />
    <property name="agencyId" value="AGENCY" />
</bean>
```

**Cache Configuration:**
```xml
<bean id="cacheManager" class="org.springframework.cache.ehcache.EhCacheManagerFactoryBean">
    <property name="configLocation" value="classpath:ehcache.xml" />
</bean>
```

### Performance Tuning

**Memory Settings:**
- Vehicle cache size: Configure based on fleet size (typically 10-50MB per 1000 vehicles)
- Block index size: Scales with number of routes and service patterns
- Historical data retention: Balance accuracy vs. memory usage

**Threading Configuration:**
```xml
<task:executor id="realtimeExecutor" 
               pool-size="5-20" 
               queue-capacity="100" 
               rejection-policy="CALLER_RUNS"/>
```

## Testing Approach

### Real-Time Processing Tests

**Integration Testing:**
```java
@Test
public void testGtfsRealtimeProcessing() {
    // Load sample GTFS-RT feed
    GtfsRealtime.FeedMessage feed = loadSampleFeed();
    
    // Process through engine
    _gtfsRealtimeService.handleTripUpdates(feed);
    
    // Verify predictions generated
    List<ArrivalAndDepartureBean> predictions = getArrivals(stopId);
    assertThat(predictions).isNotEmpty();
}
```

**Mock Data Patterns:**
- Sample Protocol Buffer feeds in test resources
- Mock vehicle trajectories for testing prediction accuracy
- Synthetic schedule deviation scenarios

### Performance Testing

**Load Testing:**
```java
@Test
public void testHighVolumeProcessing() {
    // Simulate 1000 vehicles updating every 30 seconds
    for (int i = 0; i < 1000; i++) {
        VehicleLocationRecord record = createMockVehicle(i);
        _blockLocationService.updateVehicleLocation(record);
    }
    
    // Verify response times remain acceptable
    long startTime = System.currentTimeMillis();
    List<ArrivalAndDepartureBean> results = getArrivals(stopId);
    long duration = System.currentTimeMillis() - startTime;
    assertThat(duration).isLessThan(1000); // Sub-second response
}
```

## Common Issues and Solutions

### Vehicle Matching Problems
- **Symptom:** Vehicles not appearing in real-time feeds
- **Solution:** Check trip ID format matching between GTFS and GTFS-RT
- **Debug:** Enable debug logging for `GtfsRealtimeTripLibrary`

### Memory Usage
- **Symptom:** Out of memory errors with large fleets
- **Solution:** Tune cache sizes and implement cache eviction policies
- **Monitor:** JVM heap usage and cache hit rates

### Prediction Accuracy
- **Symptom:** Inaccurate arrival predictions
- **Solution:** Calibrate deviation models with historical data
- **Optimize:** Adjust prediction algorithms for local traffic patterns

## Key Data Structures

### Vehicle Location Record
```java
public class VehicleLocationRecord {
    private String vehicleId;
    private String tripId;
    private String blockId;
    private double lat, lon;
    private long timestamp;
    private double scheduleDeviation;
}
```

### Block Instance
```java
public class BlockInstance {
    private BlockConfigurationEntry block;
    private long serviceDate;
    private String vehicleId;
    private List<BlockTripEntry> trips;
}
```

These data structures form the foundation of OneBusAway's real-time processing capabilities, enabling sophisticated prediction algorithms and efficient vehicle tracking.