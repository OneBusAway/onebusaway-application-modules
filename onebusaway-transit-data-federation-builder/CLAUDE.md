# CLAUDE.md - OneBusAway Transit Data Federation Builder

This file provides guidance to Claude Code when working with the OneBusAway GTFS data processing and bundle creation module.

## Module Overview

The onebusaway-transit-data-federation-builder transforms raw GTFS data into optimized runtime bundles for the OneBusAway system. It creates search indices, geospatial structures, and pre-computed data to enable fast real-time transit queries.

## Key Commands

### Building Bundles
- `java -jar target/onebusaway-transit-data-federation-builder-*-withAllDependencies.jar gtfs.zip output/` - Create bundle from GTFS
- `bin/build_bundle` - Docker-based bundle creation (from project root)
- `mvn package` - Build the bundle creator JAR with dependencies

### Testing
- `mvn test` - Run unit tests for individual task components
- `mvn test -Dtest=*IntegrationTest` - Run full bundle creation integration tests

### Debug Options
- Add `-Dtask.skip=task1,task2` to skip specific tasks
- Add `-Dtask.only=gtfs,transit_graph` to run only specified tasks
- Use `-Xmx4g` for large GTFS datasets

## Architecture Overview

### Bundle Creation Pipeline

**Task Execution Order:**
```
start → gtfs → calendar_service → transit_graph → block_indices → 
narratives → route_search_index → stop_search_index → 
shape_geospatial_index → block_location_history → 
canonical_shape_index → stop_direction_swap → pre_cache → gtfs_stats
```

**Main Entry Point:**
`FederatedTransitDataBundleCreatorMain.java` - Command-line interface with Spring context management

**Core Orchestrator:**
`FederatedTransitDataBundleCreator.java` - Task dependency resolution and execution pipeline

### Key Processing Tasks

#### GTFS Data Loading (`LoadGtfsTask`)
- Multi-feed support with entity replacement strategies
- Parallel processing for large datasets
- Agency ID mapping and duplicate resolution
- **Output:** Loaded GTFS entities in memory

#### Transit Graph Building (`TransitGraphTask`)
**Factory Pattern Implementation:**
- `AgencyEntriesFactory` - Transit agency processing
- `StopEntriesFactory` - Stop hierarchy and accessibility
- `RouteEntriesFactory` - Route patterns and collections
- `TripEntriesFactory` - Trip scheduling and patterns
- `BlockEntriesFactory` - Operational block groupings
- `FrequencyEntriesFactory` - Frequency-based services

**Output:** `TransitGraph.obj` - Complete network graph

#### Calendar Processing (`CalendarServiceDataTask`)
- Pre-computes service calendars for 180-day window (configurable)
- Processes calendar.txt and calendar_dates.txt
- **Output:** `CalendarServiceData.obj`

#### Block Indices (`BlockIndicesTask`)
**Creates Multiple Indices:**
- `BlockTripIndices.obj` - Trip-to-block mappings
- `BlockLayoverIndices.obj` - Layover time calculations
- `FrequencyBlockTripIndices.obj` - Frequency service patterns

#### Search Index Generation
**Stop Search (`GenerateStopSearchIndexTask`):**
- Apache Lucene full-text index for stop names/codes
- **Output:** `StopSearchIndex/` directory

**Route Search (`GenerateRouteCollectionSearchIndexTask`):**
- Lucene index for route name/short name searching
- **Output:** `RouteSearchIndex/` directory

#### Geospatial Indexing (`ShapeGeospatialIndexTask`)
- Spatial grid index with 500m cells (configurable)
- Shape point interpolation for long segments
- **Output:** `ShapeGeospatialIndexData.obj.gz`

## Development Patterns

### Adding New Bundle Tasks

1. **Create Task Class:**
```java
@Component
public class MyCustomTask implements Runnable {
    
    @Autowired
    private FederatedTransitDataBundle _bundle;
    
    @Override
    public void run() {
        // Task implementation
        processData();
        
        // Save output to bundle
        saveToBundle();
    }
}
```

2. **Configure Dependencies:**
```xml
<bean id="myCustomTask" class="org.onebusaway.MyCustomTask">
    <property name="dependencies">
        <set>
            <value>gtfs</value>
            <value>transit_graph</value>
        </set>
    </property>
</bean>
```

### Data Processing Patterns

**Factory Pattern for Graph Building:**
```java
public class MyEntriesFactory {
    
    public void processEntries(TransitGraphImpl graph) {
        for (MyEntity entity : entities) {
            MyEntry entry = processEntity(entity);
            graph.putMyEntry(entry);
        }
    }
}
```

**Bundle Output Pattern:**
```java
// Save serialized objects
File outputFile = _bundle.getPath("MyData.obj");
ObjectSerializationLibrary.writeObject(outputFile, myData);

// Save text/CSV files  
File csvFile = _bundle.getPath("stats.csv");
PrintWriter writer = new PrintWriter(csvFile);
writeCsvData(writer);
```

### Memory Management for Large Datasets

**Streaming Processing:**
```java
// Process in chunks rather than loading all at once
GtfsReader reader = new GtfsReader();
reader.setEntityHandler(Agency.class, new GtfsEntityHandler<Agency>() {
    @Override
    public void handleEntity(Agency agency) {
        processAgency(agency);
    }
});
```

**Cache Management:**
```java
// Configure cache directories with randomization
@Value("${bundle.tmpdir:#{systemProperties['java.io.tmpdir']}}")
private String tmpDir;

// Use configurable cache sizes
@Value("${bundle.cache.maxEntries:10000}")  
private int maxCacheEntries;
```

## Configuration

### Task Configuration (`application-context-bundle-creator.xml`)

**Task Dependencies:**
```xml
<bean id="transitGraphTask" class="org.onebusaway.transit_data_federation.bundle.tasks.transit_graph.TransitGraphTask">
    <property name="depends">
        <set>
            <value>gtfs</value>
            <value>calendarServiceData</value>
        </set>
    </property>
</bean>
```

**Bundle Configuration:**
```xml
<bean id="bundle" class="org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle">
    <property name="path" value="${bundle.path}" />
    <property name="cacheDirectory" value="${bundle.cacheDir}" />
</bean>
```

### Performance Tuning

**Memory Settings:**
- `-Xmx4g` or higher for large transit systems
- `-Xss4m` for deep recursion in graph algorithms
- Consider `-XX:+UseG1GC` for large heap management

**Parallelization:**
```xml
<bean id="gtfsReadingSupport" class="org.onebusaway.gtfs.services.GtfsReadingSupport">
    <property name="trimValues" value="true" />
    <property name="internStrings" value="true" />
</bean>
```

## Output Bundle Structure

```
bundle/
├── TransitGraph.obj                 # Core network representation
├── CalendarServiceData.obj          # Service calendar cache
├── BlockTripIndices.obj            # Block-trip mappings
├── BlockLayoverIndices.obj         # Layover calculations
├── FrequencyBlockTripIndices.obj   # Frequency patterns
├── NarrativeProvider.obj           # Human-readable text
├── ShapeGeospatialIndexData.obj.gz # Spatial index
├── StopSearchIndex/                # Lucene stop search
├── RouteSearchIndex/               # Lucene route search
├── gtfs-out/                       # Processed GTFS files
├── gtfs_pristine.zip              # Original GTFS
├── gtfs_tidied.zip                # Cleaned GTFS
└── *.log, *.properties            # Build logs and metadata
```

## Testing Approach

### Unit Testing Task Components

```java
@Test
public void testTransitGraphTask() {
    TransitGraphTask task = new TransitGraphTask();
    task.setBundle(mockBundle);
    task.setGtfsReader(mockReader);
    
    task.run();
    
    // Verify graph structure
    TransitGraph graph = task.getTransitGraph();
    assertThat(graph.getStops()).hasSize(expectedStopCount);
}
```

### Integration Testing Bundle Creation

```java
@Test
public void testFullBundleCreation() {
    FederatedTransitDataBundleCreator creator = new FederatedTransitDataBundleCreator();
    creator.setGtfsPath("src/test/resources/sample-gtfs.zip");
    creator.setBundlePath("target/test-bundle");
    
    creator.run();
    
    // Verify bundle outputs exist
    File bundle = new File("target/test-bundle");
    assertThat(new File(bundle, "TransitGraph.obj")).exists();
    assertThat(new File(bundle, "StopSearchIndex")).exists();
}
```

### Performance Testing

**Large Dataset Testing:**
- Use GTFS feeds with 10,000+ stops for performance validation
- Monitor heap usage during graph building phase
- Verify index creation times remain reasonable

**Memory Profiling:**
```bash
java -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/tmp/bundle-heap.dump \
     -jar bundle-builder.jar large-gtfs.zip output/
```

## Quality Assurance

### Bundle Validation

**Statistical Analysis (`GtfsStatisticsTask`):**
- Route, trip, stop counts per agency
- Service date ranges validation
- Output format: CSV reports

**Data Quality Checks:**
- `StopVerificationTask` - Stop location validation
- `CheckShapeIdTask` - Shape reference integrity
- `StopConsolidationSuggestionsTask` - Duplicate detection

### Common Issues and Solutions

**Memory Errors:**
- Increase heap size (-Xmx)
- Enable streaming processing for large agencies
- Reduce cache sizes if needed

**Missing Shape Data:**
- Use `gtfstidy` preprocessing to clean GTFS
- Implement shape interpolation for missing segments

**Index Corruption:**
- Clear cache directories between builds
- Verify Lucene version compatibility
- Check disk space during index creation

**Performance Degradation:**
- Profile task execution times
- Optimize factory processing order
- Consider parallel task execution for independent operations