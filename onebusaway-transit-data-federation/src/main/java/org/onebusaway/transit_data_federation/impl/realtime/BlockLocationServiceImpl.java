package org.onebusaway.transit_data_federation.impl.realtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.Min;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockVehicleLocationListener;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocationService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.onebusaway.transit_data_federation.services.realtime.VehicleLocationCacheRecord;
import org.onebusaway.transit_data_federation.services.realtime.VehicleLocationRecordCache;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static Logger _log = LoggerFactory.getLogger(BlockLocationServiceImpl.class);

  private VehicleLocationRecordCache _cache;

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
  public void setVehicleLocationRecordCache(VehicleLocationRecordCache cache) {
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
    BlockInstance instance = getVehicleLocationRecordAsBlockInstance(record);
    if (instance != null)
      putBlockLocationRecord(instance, record);
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

    List<VehicleLocationCacheRecord> records = getBlockLocationRecordCollectionForBlock(
        blockInstance, targetTime);

    VehicleLocationRecord record = null;
    if (!records.isEmpty())
      record = records.get(0).getRecord();

    // TODO : find a better way to pick?
    return getBlockLocation(blockInstance, record, targetTime);
  }

  @Override
  public List<BlockLocation> getLocationsForBlockInstance(
      BlockInstance blockInstance, long targetTime) {

    List<VehicleLocationCacheRecord> records = getBlockLocationRecordCollectionForBlock(
        blockInstance, targetTime);

    List<BlockLocation> locations = new ArrayList<BlockLocation>();
    for (VehicleLocationCacheRecord cacheRecord : records) {
      BlockLocation location = getBlockLocation(blockInstance,
          cacheRecord.getRecord(), targetTime);
      if (location != null)
        locations.add(location);
    }

    return locations;
  }

  @Override
  public BlockLocation getScheduledLocationForBlockInstance(
      BlockInstance blockInstance, long targetTime) {
    return getBlockLocation(blockInstance, null, targetTime);
  }

  @Override
  public BlockLocation getLocationForVehicleAndTime(AgencyAndId vehicleId,
      long targetTime) {

    List<VehicleLocationCacheRecord> cacheRecords = getBlockLocationRecordCollectionForVehicle(
        vehicleId, targetTime);

    // TODO : We might take a bit more care in picking the collection if
    // multiple collections are returned
    for (VehicleLocationCacheRecord cacheRecord : cacheRecords) {
      BlockInstance blockInstance = cacheRecord.getBlockInstance();
      BlockLocation location = getBlockLocation(blockInstance,
          cacheRecord.getRecord(), targetTime);
      if (location != null)
        return location;
    }

    return null;
  }

  /****
   * Private Methods
   ****/

  private BlockInstance getVehicleLocationRecordAsBlockInstance(
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

    if (!record.isScheduleDeviationSet()
        && _scheduleDeviationComputationEnabled) {

      BlockConfigurationEntry blockConfig = blockInstance.getBlock();
      double distanceAlongBlock = record.getDistanceAlongBlock();
      ScheduledBlockLocation scheduledBlockLocation = _scheduledBlockLocationService.getScheduledBlockLocationFromDistanceAlongBlock(
          blockConfig, distanceAlongBlock);
      int deviation = (int) ((record.getTimeOfRecord() - record.getServiceDate()) / 1000 - scheduledBlockLocation.getScheduledTime());
      record.setScheduleDeviation(deviation);
    }

    return blockInstance;
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
      VehicleLocationRecord record) {

    // Cache the result
    _cache.addRecord(blockInstance, record);

    if (_persistBlockLocationRecords) {
      List<BlockLocationRecord> blockLocationRecords = getVehicleLocationRecordAsBlockLocationRecord(record);
      addPredictionToPersistenceQueue(blockLocationRecords);
    }
  }

  /**
   * 
   * @param blockInstance
   * @param record
   * @param targetTime
   * @return null if the effective scheduled block location cannot be determined
   */
  private BlockLocation getBlockLocation(BlockInstance blockInstance,
      VehicleLocationRecord record, long targetTime) {

    BlockLocation location = new BlockLocation();

    location.setBlockInstance(blockInstance);

    if (record != null) {

      location.setPredicted(true);
      location.setLastUpdateTime(record.getTimeOfRecord());
      location.setScheduleDeviation(record.getScheduleDeviation());
      location.setDistanceAlongBlock(record.getDistanceAlongBlock());
      if (record.isCurrentLocationSet()) {
        CoordinatePoint p = new CoordinatePoint(record.getCurrentLocationLat(),
            record.getCurrentLocationLon());
        location.setLastKnownLocation(p);
      }
      location.setOrientation(record.getCurrentOrientation());
      location.setPhase(record.getPhase());
      location.setStatus(record.getStatus());
      location.setVehicleId(record.getVehicleId());

      List<TimepointPredictionRecord> timepointPredictions = record.getTimepointPredictions();
      if (timepointPredictions != null && !timepointPredictions.isEmpty()) {

        SortedMap<Integer, Double> scheduleDeviations = new TreeMap<Integer, Double>();

        BlockConfigurationEntry blockConfig = blockInstance.getBlock();

        for (TimepointPredictionRecord tpr : timepointPredictions) {
          AgencyAndId stopId = tpr.getTimepointId();
          long predictedTime = tpr.getTimepointPredictedTime();
          if (stopId == null || predictedTime == 0)
            continue;

          for (BlockStopTimeEntry blockStopTime : blockConfig.getStopTimes()) {
            StopTimeEntry stopTime = blockStopTime.getStopTime();
            StopEntry stop = stopTime.getStop();
            if (stopId.equals(stop.getId())) {
              int arrivalTime = stopTime.getArrivalTime();
              int deviation = (int) ((tpr.getTimepointPredictedTime() - blockInstance.getServiceDate()) / 1000 - arrivalTime);
              scheduleDeviations.put(arrivalTime, (double) deviation);
            }
          }
        }

        location.setScheduleDeviations(scheduleDeviations);
      }
    }

    ScheduledBlockLocation scheduledLocation = getScheduledBlockLocation(
        location, targetTime);

    /**
     * Will be null in the following cases:
     * 
     * 1) When the effective schedule time is beyond the last scheduled stop
     * time for the block.
     * 
     * 2) When the effective distance along block is outside the range of the
     * block's shape.
     */
    if (scheduledLocation == null)
      return null;

    location.setInService(scheduledLocation.isInService());
    location.setActiveTrip(scheduledLocation.getActiveTrip());
    location.setLocation(scheduledLocation.getLocation());
    location.setOrientation(scheduledLocation.getOrientation());
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

    if (blockLocation.isScheduleDeviationSet()) {

      /**
       * Effective scheduled time is the point that a transit vehicle is at on
       * its schedule, with schedule deviation taken into account. So if it's
       * 100 minutes into the current service date and the bus is running 10
       * minutes late, it's actually at the 90 minute point in its scheduled
       * operation.
       */
      int effectiveScheduledTime = (int) (scheduledTime - blockLocation.getScheduleDeviation());

      return _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
          blockConfig, effectiveScheduledTime);
    }

    if (blockLocation.isDistanceAlongBlockSet()) {
      return _scheduledBlockLocationService.getScheduledBlockLocationFromDistanceAlongBlock(
          blockConfig, blockLocation.getDistanceAlongBlock());
    }

    return _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
        blockConfig, scheduledTime);
  }

  private List<VehicleLocationCacheRecord> getBlockLocationRecordCollectionForBlock(
      BlockInstance blockInstance, long targetTime) {
    return getBlockLocationRecordCollections(new BlockInstanceStrategy(
        blockInstance), targetTime);
  }

  private List<VehicleLocationCacheRecord> getBlockLocationRecordCollectionForVehicle(
      AgencyAndId vehicleId, long targetTime) {
    return getBlockLocationRecordCollections(new VehicleIdRecordStrategy(
        vehicleId), targetTime);
  }

  private List<VehicleLocationCacheRecord> getBlockLocationRecordCollections(
      RecordStrategy strategy, long targetTime) {

    List<VehicleLocationCacheRecord> records = strategy.getRecordsFromCache();

    if (!records.isEmpty()) {
      List<VehicleLocationCacheRecord> inRange = new ArrayList<VehicleLocationCacheRecord>();
      long offset = _predictionCacheMaxOffset * 1000;
      for (VehicleLocationCacheRecord entry : records) {
        VehicleLocationRecord record = entry.getRecord();
        if (record.getTimeOfRecord() - offset <= targetTime
            && targetTime <= record.getTimeOfRecord() + offset)
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

        List<VehicleLocationCacheRecord> allCollections = new ArrayList<VehicleLocationCacheRecord>();
        for (Map.Entry<BlockLocationRecordKey, List<BlockLocationRecord>> entry : recordsByKey.entrySet()) {
          BlockLocationRecordKey key = entry.getKey();
          List<BlockLocationRecord> blockLocationRecords = entry.getValue();
          List<VehicleLocationCacheRecord> someRecords = getBlockLocationRecordsAsVehicleLocationRecords(
              key.getBlockInstance(), blockLocationRecords);
          allCollections.addAll(someRecords);
        }

        return allCollections;
      }
    }

    return Collections.emptyList();
  }

  private List<BlockLocationRecord> getVehicleLocationRecordAsBlockLocationRecord(
      VehicleLocationRecord record) {

    BlockLocationRecord.Builder builder = BlockLocationRecord.builder();
    builder.setBlockId(record.getBlockId());
    builder.setTripId(record.getTripId());
    builder.setTime(record.getTimeOfRecord());
    builder.setServiceDate(record.getServiceDate());

    if (record.isScheduleDeviationSet())
      builder.setScheduleDeviation(record.getScheduleDeviation());

    if (record.isDistanceAlongBlockSet())
      builder.setDistanceAlongBlock(record.getDistanceAlongBlock());

    if (record.isCurrentLocationSet()) {
      builder.setLocationLat(record.getCurrentLocationLat());
      builder.setLocationLon(record.getCurrentLocationLon());
    }

    if (record.isCurrentOrientationSet())
      builder.setOrientation(record.getCurrentOrientation());

    builder.setPhase(record.getPhase());
    builder.setStatus(record.getStatus());
    builder.setVehicleId(record.getVehicleId());

    List<TimepointPredictionRecord> predictions = record.getTimepointPredictions();
    if (predictions == null || predictions.isEmpty())
      return Arrays.asList(builder.create());

    List<BlockLocationRecord> results = new ArrayList<BlockLocationRecord>();
    for (TimepointPredictionRecord tpr : predictions) {
      builder.setTimepointId(tpr.getTimepointId());
      builder.setTimepointScheduledTime(tpr.getTimepointScheduledTime());
      builder.setTimepointPredictedTime(tpr.getTimepointPredictedTime());
      results.add(builder.create());
    }
    return results;
  }

  private List<VehicleLocationCacheRecord> getBlockLocationRecordsAsVehicleLocationRecords(
      BlockInstance blockInstance, List<BlockLocationRecord> records) {

    List<VehicleLocationCacheRecord> results = new ArrayList<VehicleLocationCacheRecord>();

    for (BlockLocationRecord record : records) {
      VehicleLocationRecord vlr = new VehicleLocationRecord();
      vlr.setBlockId(blockInstance.getBlock().getBlock().getId());
      vlr.setCurrentLocationLat(record.getLocationLat());
      vlr.setCurrentLocationLon(record.getLocationLon());
      vlr.setCurrentOrientation(record.getOrientation());
      vlr.setDistanceAlongBlock(record.getDistanceAlongBlock());
      vlr.setPhase(record.getPhase());
      vlr.setScheduleDeviation(record.getScheduleDeviation());
      vlr.setServiceDate(record.getServiceDate());
      vlr.setStatus(record.getStatus());
      vlr.setTimeOfRecord(record.getTime());
      vlr.setVehicleId(record.getVehicleId());

      VehicleLocationCacheRecord cacheRecord = new VehicleLocationCacheRecord(
          blockInstance, vlr);
      results.add(cacheRecord);
    }

    return results;
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

  private void addPredictionToPersistenceQueue(List<BlockLocationRecord> records) {
    synchronized (_recordPersistenceQueue) {
      _recordPersistenceQueue.addAll(records);
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

      try {
        List<BlockLocationRecord> queue = getPredictionPersistenceQueue();

        if (queue.isEmpty())
          return;

        long t1 = System.currentTimeMillis();
        _blockLocationRecordDao.saveBlockLocationRecords(queue);
        long t2 = System.currentTimeMillis();
        _lastInsertDuration = t2 - t1;
        _lastInsertCount = queue.size();
      } catch (Throwable ex) {
        _log.error("error writing block location records to dao", ex);
      }
    }
  }

  private interface RecordStrategy {

    public List<VehicleLocationCacheRecord> getRecordsFromCache();

    public List<BlockLocationRecord> getRecordsFromDao(long fromTime,
        long toTime);
  }

  private class BlockInstanceStrategy implements RecordStrategy {

    private BlockInstance _blockInstance;

    public BlockInstanceStrategy(BlockInstance blockInstance) {
      _blockInstance = blockInstance;
    }

    @Override
    public List<VehicleLocationCacheRecord> getRecordsFromCache() {
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

    public List<VehicleLocationCacheRecord> getRecordsFromCache() {
      VehicleLocationCacheRecord recordForVehicleId = _cache.getRecordForVehicleId(_vehicleId);
      if (recordForVehicleId == null)
        return Collections.emptyList();
      return Arrays.asList(recordForVehicleId);
    }

    public List<BlockLocationRecord> getRecordsFromDao(long fromTime,
        long toTime) {
      return _blockLocationRecordDao.getBlockLocationRecordsForVehicleAndTimeRange(
          _vehicleId, fromTime, toTime);
    }
  }
}
