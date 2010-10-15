package org.onebusaway.transit_data_federation.impl.realtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.Min;
import org.onebusaway.collections.tuple.T2;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockVehicleLocationListener;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocationService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationRecordCache;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/**
 * Implementation for {@link BlockLocationService}. Keeps a recent cache of
 * {@link BlockLocationRecord} records for current queries and can access
 * database persisted records for queries in the past.
 * 
 * @author bdferris
 * @see BlockLocationService
 */
@Component
@ManagedResource("org.onebusaway.transit_data_federation.impl.realtime:name=BlockLocationServiceImpl")
public class BlockLocationServiceImpl implements BlockLocationService,
    BlockVehicleLocationListener {

  private BlockLocationRecordCache _cache;

  private BlockLocationRecordDao _blockLocationRecordDao;

  private TransitGraphDao _transitGraphDao;

  private ScheduledBlockLocationService _scheduledBlockLocationService;

  private BlockCalendarService _blockCalendarService;

  /**
   * By default, we keep around 20 minutes of cache entries
   */
  private int _blockLocationRecordCacheWindowSize = 20 * 60;

  private int _predictionCacheMaxOffset = 5 * 60;

  private int _blockInstanceMatchingWindow = 60 * 60 * 1000;

  /**
   * Should block location records be stored to the database?
   */
  private boolean _persistBlockLocationRecords = false;

  /**
   * We queue up block location records so they can be bulk persisted to the
   * database
   */
  private List<BlockLocationRecord> _recordPersistenceQueue = new ArrayList<BlockLocationRecord>();

  /**
   * Used to schedule periodic flushes to the database of the block location
   * records queue
   */
  private ScheduledExecutorService _executor = Executors.newSingleThreadScheduledExecutor();

  /**
   * Block location record persistence stats - last record insert duration
   */
  private volatile long _lastInsertDuration = 0;

  /**
   * Block location record persistence stats - last record insert count
   */
  private volatile long _lastInsertCount = 0;

  /**
   * Records the number of times block location record cache requests fall
   * through to the database
   */
  private AtomicInteger _blockLocationRecordPersistentStoreAccessCount = new AtomicInteger();

  private boolean _scheduleDeviationComputationEnabled = false;

  @Autowired
  public void setBlockLocationRecordCache(BlockLocationRecordCache cache) {
    _cache = cache;
  }

  @Autowired
  public void setBlockLocationRecordDao(
      BlockLocationRecordDao blockLocationRecordDao) {
    _blockLocationRecordDao = blockLocationRecordDao;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setScheduledBlockLocationService(
      ScheduledBlockLocationService scheduleBlockLocationService) {
    _scheduledBlockLocationService = scheduleBlockLocationService;
  }

  @Autowired
  public void setBlockCalendarService(BlockCalendarService blockCalendarService) {
    _blockCalendarService = blockCalendarService;
  }

  /**
   * Controls how far back in time we include records in the
   * {@link BlockLocationRecordCollection} for each active trip.
   * 
   * @param windowSize in seconds
   */
  public void setBlockLocationRecordCacheWindowSize(int windowSize) {
    _blockLocationRecordCacheWindowSize = windowSize;
  }

  /**
   * Should we persist {@link BlockLocationRecord} records to an underlying
   * datastore. Useful if you wish to query trip status for historic analysis.
   * 
   * @param persistTripTimePredictions
   */
  public void setPersistBlockLocationRecords(boolean persistBlockLocationRecords) {
    _persistBlockLocationRecords = persistBlockLocationRecords;
  }

  public void setScheduleDeviationComputationEnabled(
      boolean scheduleDeviationComputationEnabled) {
    _scheduleDeviationComputationEnabled = scheduleDeviationComputationEnabled;
  }

  /****
   * JMX Attributes
   ****/

  @ManagedAttribute
  public long getLastInsertDuration() {
    return _lastInsertDuration;
  }

  @ManagedAttribute
  public long getLastInsertCount() {
    return _lastInsertCount;
  }

  @ManagedAttribute
  public long getBlockLocationRecordPersistentStoreAccessCount() {
    return _blockLocationRecordPersistentStoreAccessCount.get();
  }

  /****
   * Setup and Teardown
   ****/

  @PostConstruct
  public void start() {
    if (_persistBlockLocationRecords)
      _executor.scheduleAtFixedRate(new PredictionWriter(), 0, 1,
          TimeUnit.SECONDS);
  }

  @PreDestroy
  public void stop() {
    _executor.shutdownNow();
  }

  /****
   * {@link BlockVehicleLocationListener} Interface
   ****/

  @Override
  public void handleVehicleLocationRecord(VehicleLocationRecord record) {
    T2<BlockInstance, BlockLocationRecord> tuple = getVehicleLocationRecordAsBlockLocationRecord(record);
    if (tuple != null)
      putBlockLocationRecord(tuple.getFirst(), tuple.getSecond());
  }

  @Override
  public void resetVehicleLocation(AgencyAndId vehicleId) {
    _cache.clearRecordsForVehicleId(vehicleId);
  }

  /****
   * {@link BlockLocationService} Interface
   ****/

  @Override
  public BlockLocation getLocationForBlockInstance(BlockInstance blockInstance,
      long targetTime) {

    List<BlockLocationRecordCollection> records = getBlockLocationRecordCollectionForBlock(
        blockInstance, targetTime);

    // TODO : find a better way to pick?
    return getBlockLocation(blockInstance, records.get(0), targetTime);
  }

  @Override
  public List<BlockLocation> getLocationsForBlockInstance(
      BlockInstance blockInstance, long targetTime) {

    List<BlockLocationRecordCollection> records = getBlockLocationRecordCollectionForBlock(
        blockInstance, targetTime);

    List<BlockLocation> locations = new ArrayList<BlockLocation>();
    for (BlockLocationRecordCollection collection : records) {
      BlockLocation location = getBlockLocation(blockInstance, collection,
          targetTime);
      if (location != null && location.isInService())
        locations.add(location);
    }

    return locations;
  }

  @Override
  public BlockLocation getLocationForVehicleAndTime(AgencyAndId vehicleId,
      long targetTime) {

    List<BlockLocationRecordCollection> collections = getBlockLocationRecordCollectionForVehicle(
        vehicleId, targetTime);

    // TODO : We might take a bit more care in picking the collection if
    // multiple collections are returned
    for (BlockLocationRecordCollection collection : collections) {
      if (collection.isEmpty())
        continue;
      BlockInstance blockInstance = collection.getBlockInstance();
      BlockLocation location = getBlockLocation(blockInstance, collection,
          targetTime);
      if (location != null)
        return location;
    }

    return null;
  }

  /****
   * Private Methods
   ****/

  private T2<BlockInstance, BlockLocationRecord> getVehicleLocationRecordAsBlockLocationRecord(
      VehicleLocationRecord record) {

    AgencyAndId blockId = record.getBlockId();

    if (blockId == null) {
      AgencyAndId tripId = record.getTripId();
      if (tripId == null)
        throw new IllegalArgumentException(
            "at least one of blockId or tripId must be specified for VehicleLocationRecord");
      TripEntry tripEntry = _transitGraphDao.getTripEntryForId(tripId);
      if (tripEntry == null)
        throw new IllegalArgumentException("trip not found with id=" + tripId);
      BlockEntry block = tripEntry.getBlock();
      blockId = block.getId();
    }

    if (record.getServiceDate() == 0)
      throw new IllegalArgumentException("you must specify a serviceDate");

    if (record.getTimeOfRecord() == 0)
      throw new IllegalArgumentException("you must specify a record time");

    BlockInstance blockInstance = getBestBlockForRecord(blockId,
        record.getServiceDate(), record.getTimeOfRecord());

    if (blockInstance == null)
      return null;

    BlockLocationRecord.Builder builder = BlockLocationRecord.builder();
    builder.setBlockId(blockId);
    builder.setTime(record.getTimeOfRecord());
    builder.setServiceDate(record.getServiceDate());

    if (record.isScheduleDeviationSet())
      builder.setScheduleDeviation(record.getScheduleDeviation());

    if (record.isDistanceAlongBlockSet()) {
      builder.setDistanceAlongBlock(record.getDistanceAlongBlock());

      if (!record.isScheduleDeviationSet()
          && _scheduleDeviationComputationEnabled) {

        BlockConfigurationEntry blockConfig = blockInstance.getBlock();
        List<BlockStopTimeEntry> stopTimes = blockConfig.getStopTimes();
        double distanceAlongBlock = record.getDistanceAlongBlock();
        ScheduledBlockLocation scheduledBlockLocation = _scheduledBlockLocationService.getScheduledBlockLocationFromDistanceAlongBlock(
            stopTimes, distanceAlongBlock);
        int deviation = (int) ((record.getTimeOfRecord() - record.getServiceDate()) / 1000 - scheduledBlockLocation.getScheduledTime());
        builder.setScheduleDeviation(deviation);
      }
    }

    if (record.isCurrentLocationSet()) {
      builder.setLocationLat(record.getCurrentLocationLat());
      builder.setLocationLon(record.getCurrentLocationLon());
    }

    builder.setStatus(record.getStatus());
    builder.setVehicleId(record.getVehicleId());

    return Tuples.tuple(blockInstance, builder.create());
  }

  private BlockInstance getBestBlockForRecord(AgencyAndId blockId,
      long serviceDate, long timeOfRecord) {

    long timeFrom = timeOfRecord - _blockInstanceMatchingWindow;
    long timeTo = timeOfRecord + _blockInstanceMatchingWindow;

    List<BlockInstance> blocks = _blockCalendarService.getActiveBlocks(blockId,
        timeFrom, timeTo);

    if (blocks.isEmpty())
      return null;
    else if (blocks.size() == 1)
      return blocks.get(0);

    Min<BlockInstance> m = new Min<BlockInstance>();

    for (BlockInstance block : blocks) {
      long delta = Math.abs(block.getServiceDate() - serviceDate);
      m.add(delta, block);
    }

    return m.getMinElement();
  }

  /**
   * We add the {@link BlockPositionRecord} to the local cache and persist it to
   * a back-end data-store if necessary
   */
  private void putBlockLocationRecord(BlockInstance blockInstance,
      BlockLocationRecord record) {

    // Cache the result
    _cache.addRecord(blockInstance, record);

    if (_persistBlockLocationRecords)
      addPredictionToPersistenceQueue(record);
  }

  private BlockLocation getBlockLocation(BlockInstance blockInstance,
      BlockLocationRecordCollection records, long targetTime) {

    BlockLocation location = new BlockLocation();

    location.setBlockInstance(blockInstance);

    boolean predicted = !records.isEmpty();
    if (predicted) {
      location.setPredicted(true);
      long lastUpdateTime = records.getLastUpdateTime(targetTime);
      location.setLastUpdateTime(lastUpdateTime);
    }

    double scheduleDeviation = records.getScheduleDeviationForTargetTime(targetTime);
    location.setScheduleDeviation(scheduleDeviation);

    double distanceAlongBlock = records.getDistanceAlongBlockForTargetTime(targetTime);
    location.setDistanceAlongBlock(distanceAlongBlock);

    CoordinatePoint point = records.getLastLocationForTargetTime(targetTime);
    location.setLastKnownLocation(point);

    String status = records.getStatusForTargetTime(targetTime);
    location.setStatus(status);

    location.setVehicleId(records.getVehicleId());

    ScheduledBlockLocation scheduledLocation = getScheduledBlockLocation(
        location, targetTime);

    if (scheduledLocation == null) {
      location.setInService(false);
      return location;
    }

    location.setInService(true);
    location.setActiveTrip(scheduledLocation.getActiveTrip());
    location.setLocation(scheduledLocation.getLocation());
    location.setScheduledDistanceAlongBlock(scheduledLocation.getDistanceAlongBlock());
    location.setClosestStop(scheduledLocation.getClosestStop());
    location.setClosestStopTimeOffset(scheduledLocation.getClosestStopTimeOffset());
    location.setNextStop(scheduledLocation.getNextStop());
    location.setNextStopTimeOffset(scheduledLocation.getNextStopTimeOffset());

    return location;
  }

  private ScheduledBlockLocation getScheduledBlockLocation(
      BlockLocation blockLocation, long targetTime) {

    BlockInstance blockInstance = blockLocation.getBlockInstance();
    BlockConfigurationEntry blockConfig = blockInstance.getBlock();
    long serviceDate = blockInstance.getServiceDate();

    int scheduledTime = (int) ((targetTime - serviceDate) / 1000);

    if (blockLocation.hasScheduleDeviation()) {

      /**
       * Effective scheduled time is the point that a transit vehicle is at on
       * its schedule, with schedule deviation taken into account. So if it's
       * 100 minutes into the current service date and the bus is running 10
       * minutes late, it's actually at the 90 minute point in its scheduled
       * operation.
       */
      int effectiveScheduledTime = (int) (scheduledTime - blockLocation.getScheduleDeviation());

      return _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
          blockConfig.getStopTimes(), effectiveScheduledTime);
    }

    if (blockLocation.hasDistanceAlongBlock()) {
      return _scheduledBlockLocationService.getScheduledBlockLocationFromDistanceAlongBlock(
          blockConfig.getStopTimes(), blockLocation.getDistanceAlongBlock());
    }

    return _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
        blockConfig.getStopTimes(), scheduledTime);
  }

  private List<BlockLocationRecordCollection> getBlockLocationRecordCollectionForBlock(
      BlockInstance blockInstance, long targetTime) {
    return getBlockLocationRecordCollections(new BlockInstanceStrategy(
        blockInstance), targetTime);
  }

  private List<BlockLocationRecordCollection> getBlockLocationRecordCollectionForVehicle(
      AgencyAndId vehicleId, long targetTime) {
    return getBlockLocationRecordCollections(new VehicleIdRecordStrategy(
        vehicleId), targetTime);
  }

  private List<BlockLocationRecordCollection> getBlockLocationRecordCollections(
      RecordStrategy strategy, long targetTime) {

    List<BlockLocationRecordCollection> collections = strategy.getRecordsFromCache();

    if (!collections.isEmpty()) {
      List<BlockLocationRecordCollection> inRange = new ArrayList<BlockLocationRecordCollection>();
      long offset = _predictionCacheMaxOffset * 1000;
      for (BlockLocationRecordCollection entry : collections) {
        if (entry.getFromTime() - offset <= targetTime
            && targetTime <= entry.getToTime() + offset)
          inRange.add(entry);
      }
      if (!inRange.isEmpty())
        return inRange;
    }

    long offset = _blockLocationRecordCacheWindowSize * 1000 / 2;
    long fromTime = targetTime - offset;
    long toTime = targetTime + offset;

    // We only consult persisted cache entries if the requested target time is
    // not within our current cache window
    if (targetTime + offset < System.currentTimeMillis()
        && _persistBlockLocationRecords) {

      _blockLocationRecordPersistentStoreAccessCount.incrementAndGet();

      List<BlockLocationRecord> predictions = strategy.getRecordsFromDao(
          fromTime, toTime);

      if (!predictions.isEmpty()) {

        Map<BlockLocationRecordKey, List<BlockLocationRecord>> recordsByKey = groupRecord(predictions);

        List<BlockLocationRecordCollection> allCollections = new ArrayList<BlockLocationRecordCollection>();
        for (Map.Entry<BlockLocationRecordKey, List<BlockLocationRecord>> entry : recordsByKey.entrySet()) {
          BlockLocationRecordKey key = entry.getKey();
          List<BlockLocationRecord> records = entry.getValue();
          BlockLocationRecordCollection collection = BlockLocationRecordCollection.createFromRecords(
              key.getBlockInstance(), records);
          allCollections.add(collection);
        }

        return allCollections;
      }
    }

    return Arrays.asList(new BlockLocationRecordCollection(fromTime, toTime));
  }

  private Map<BlockLocationRecordKey, List<BlockLocationRecord>> groupRecord(
      List<BlockLocationRecord> predictions) {

    Map<BlockLocationRecordKey, List<BlockLocationRecord>> recordsByKey = new FactoryMap<BlockLocationRecordKey, List<BlockLocationRecord>>(
        new ArrayList<BlockLocationRecord>());

    for (BlockLocationRecord record : predictions) {
      AgencyAndId blockId = record.getBlockId();
      long serviceDate = record.getServiceDate();
      BlockInstance blockInstance = _blockCalendarService.getBlockInstance(
          blockId, serviceDate);
      if (blockInstance != null) {
        BlockLocationRecordKey key = new BlockLocationRecordKey(blockInstance,
            record.getVehicleId());
        recordsByKey.get(key).add(record);
      }
    }

    return recordsByKey;
  }

  private void addPredictionToPersistenceQueue(BlockLocationRecord prediction) {
    synchronized (_recordPersistenceQueue) {
      _recordPersistenceQueue.add(prediction);
    }
  }

  private List<BlockLocationRecord> getPredictionPersistenceQueue() {
    synchronized (_recordPersistenceQueue) {
      List<BlockLocationRecord> queue = new ArrayList<BlockLocationRecord>(
          _recordPersistenceQueue);
      _recordPersistenceQueue.clear();
      return queue;
    }
  }

  private class PredictionWriter implements Runnable {

    @Override
    public void run() {

      List<BlockLocationRecord> queue = getPredictionPersistenceQueue();

      if (queue.isEmpty())
        return;

      long t1 = System.currentTimeMillis();
      _blockLocationRecordDao.saveBlockLocationRecords(queue);
      long t2 = System.currentTimeMillis();
      _lastInsertDuration = t2 - t1;
      _lastInsertCount = queue.size();
    }
  }

  private interface RecordStrategy {

    public List<BlockLocationRecordCollection> getRecordsFromCache();

    public List<BlockLocationRecord> getRecordsFromDao(long fromTime,
        long toTime);
  }

  private class BlockInstanceStrategy implements RecordStrategy {

    private BlockInstance _blockInstance;

    public BlockInstanceStrategy(BlockInstance blockInstance) {
      _blockInstance = blockInstance;
    }

    @Override
    public List<BlockLocationRecordCollection> getRecordsFromCache() {
      return _cache.getRecordsForBlockInstance(_blockInstance);
    }

    @Override
    public List<BlockLocationRecord> getRecordsFromDao(long fromTime,
        long toTime) {
      BlockConfigurationEntry blockConfig = _blockInstance.getBlock();
      BlockEntry block = blockConfig.getBlock();
      return _blockLocationRecordDao.getBlockLocationRecordsForBlockServiceDateAndTimeRange(
          block.getId(), _blockInstance.getServiceDate(), fromTime, toTime);
    }
  }

  private class VehicleIdRecordStrategy implements RecordStrategy {

    private AgencyAndId _vehicleId;

    public VehicleIdRecordStrategy(AgencyAndId vehicleId) {
      _vehicleId = vehicleId;
    }

    public List<BlockLocationRecordCollection> getRecordsFromCache() {
      return _cache.getRecordsForVehicleId(_vehicleId);
    }

    public List<BlockLocationRecord> getRecordsFromDao(long fromTime,
        long toTime) {
      return _blockLocationRecordDao.getBlockLocationRecordsForVehicleAndTimeRange(
          _vehicleId, fromTime, toTime);
    }
  }

}
