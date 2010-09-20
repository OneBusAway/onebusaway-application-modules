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
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.impl.time.StopTimeSearchOperations;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeOp;
import org.onebusaway.transit_data_federation.model.ServiceDateAndId;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocationService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationRecordCache;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.onebusaway.transit_data_federation.services.realtime.StopRealtimeService;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;
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
    StopRealtimeService, VehicleLocationListener {

  /**
   * The radius of the search window we'll use for finding
   * {@link BlockLocationRecord} records for a given vehicle id and time
   */
  private static final int BLOCK_RECORD_FOR_VEHICLE_SEARCH_WINDOW = 20 * 60 * 1000;

  private BlockLocationRecordCache _cache;

  private BlockLocationRecordDao _blockLocationRecordDao;

  private TransitGraphDao _transitGraphDao;

  private ScheduledBlockLocationService _scheduledBlockLocationService;

  private BlockCalendarService _activeCalendarService;

  /**
   * By default, we keep around 20 minutes of cache entries
   */
  private int _blockLocationRecordCacheWindowSize = 20 * 60;

  private int _predictionCacheMaxOffset = 5 * 60;

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

  /**
   * A count of the number of stop times queried for real-time arrival
   * information
   */
  private AtomicInteger _stopTimesTotal = new AtomicInteger();

  /**
   * A count of the number of stop times queried for real-time arrival
   * information that actually had data
   */
  private AtomicInteger _stopTimesWithPredictions = new AtomicInteger();

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
  public void setActiveCalendarService(
      BlockCalendarService activeCalendarService) {
    _activeCalendarService = activeCalendarService;
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

  @ManagedAttribute
  public int getStopTimesTotal() {
    return _stopTimesTotal.intValue();
  }

  @ManagedAttribute
  public int getStopTimesWithPredictions() {
    return _stopTimesWithPredictions.intValue();
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
    if (_persistBlockLocationRecords)
      _executor.shutdownNow();
  }

  /****
   * {@link VehicleLocationListener} Interface
   ****/

  @Override
  public void handleVehicleLocationRecord(VehicleLocationRecord record) {
    BlockLocationRecord blockLocationRecord = getVehicleLocationRecordAsBlockLocationRecord(record);
    putBlockLocationRecord(blockLocationRecord);
  }

  @Override
  public void handleVehicleLocationRecords(List<VehicleLocationRecord> records) {

    for (VehicleLocationRecord record : records) {
      BlockLocationRecord blockLocationRecord = getVehicleLocationRecordAsBlockLocationRecord(record);
      putBlockLocationRecord(blockLocationRecord);
    }
  }

  @Override
  public void resetVehicleLocation(AgencyAndId vehicleId) {
    _cache.clearRecordsForVehicleId(vehicleId);
  }

  /****
   * {@link StopRealtimeService} Interface
   ****/

  public void applyRealtimeData(List<StopTimeInstanceProxy> stopTimes,
      long targetTime) {

    _stopTimesTotal.addAndGet(stopTimes.size());

    Map<ServiceDateAndId, List<StopTimeInstanceProxy>> stisByBlockId = getStopTimeInstancesByBlockId(stopTimes);

    for (Map.Entry<ServiceDateAndId, List<StopTimeInstanceProxy>> entry : stisByBlockId.entrySet()) {

      ServiceDateAndId serviceDateAndBlockId = entry.getKey();
      List<BlockLocationRecordCollection> collections = getBlockLocationRecordCollectionForBlock(
          serviceDateAndBlockId, targetTime);

      if (collections.isEmpty())
        continue;

      BlockLocationRecordCollection collection = collections.get(0);

      boolean predicted = false;

      // Only apply real-time data if there is data to apply
      if (collection.hasScheduleDeviations()) {

        predicted = true;

        int scheduleDeviation = collection.getScheduleDeviationForTargetTime(targetTime);

        long serviceDate = serviceDateAndBlockId.getServiceDate();
        int effectiveScheduleTime = (int) (((targetTime - serviceDate) / 1000) - scheduleDeviation);

        StopTimeEntry nextStopTime = getNextStopTime(serviceDateAndBlockId,
            collection, effectiveScheduleTime);

        for (StopTimeInstanceProxy sti : entry.getValue()) {

          StopTimeEntry stopTime = sti.getStopTime();
          int arrivalDeviation = calculateArrivalDeviation(collection,
              nextStopTime, stopTime, effectiveScheduleTime, scheduleDeviation);
          int departureDeviation = calculateDepartureDeviation(collection,
              nextStopTime, stopTime, effectiveScheduleTime, scheduleDeviation);

          sti.setPredictedArrivalOffset(arrivalDeviation);
          sti.setPredictedDepartureOffset(departureDeviation);
          _stopTimesWithPredictions.incrementAndGet();
        }
      }

      if (collection.hasDistancesAlongBlock()) {

        predicted = true;
        double distanceAlongBlock = collection.getDistanceAlongBlockForTargetTime(targetTime);

        for (StopTimeInstanceProxy sti : entry.getValue()) {
          double distanceFromStop = sti.getStopTime().getDistaceAlongBlock()
              - distanceAlongBlock;
          sti.setDistanceFromStop(distanceFromStop);
        }

      } else {

        for (StopTimeInstanceProxy sti : entry.getValue()) {
          TripEntry tripEntry = sti.getTrip();
          BlockEntry blockEntry = tripEntry.getBlock();

          int scheduleTime = (int) ((targetTime - sti.getServiceDate()) / 1000);
          ScheduledBlockLocation location = _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
              blockEntry.getStopTimes(), scheduleTime);

          if (location != null) {
            double distanceAlongBlock = location.getDistanceAlongBlock();
            double distanceFromStop = sti.getStopTime().getDistaceAlongBlock()
                - distanceAlongBlock;
            sti.setDistanceFromStop(distanceFromStop);
          }
        }
      }

      // Set prediction status and last update time as applicable
      if (predicted && !collection.isEmpty()) {
        long lastUpdateTime = collection.getLastUpdateTime(targetTime);
        for (StopTimeInstanceProxy sti : entry.getValue()) {
          sti.setLastUpdateTime(lastUpdateTime);
          sti.setPredicted(true);
        }
      }
    }
  }

  private StopTimeEntry getNextStopTime(ServiceDateAndId serviceDateAndBlockId,
      BlockLocationRecordCollection collection, int effectiveScheduleTime) {

    AgencyAndId blockId = serviceDateAndBlockId.getId();

    BlockEntry blockEntry = _transitGraphDao.getBlockEntryForId(blockId);
    List<StopTimeEntry> stopTimes = blockEntry.getStopTimes();
    int index = StopTimeSearchOperations.searchForStopTime(stopTimes,
        effectiveScheduleTime, StopTimeOp.DEPARTURE);
    if (0 <= index && index < stopTimes.size())
      return stopTimes.get(index);
    return null;
  }

  private int calculateArrivalDeviation(
      BlockLocationRecordCollection collection, StopTimeEntry nextStopTime,
      StopTimeEntry targetStopTime, int effectiveScheduleTime,
      int scheduleDeviation) {

    if (targetStopTime.getArrivalTime() < effectiveScheduleTime)
      return collection.getScheduleDeviationForScheduleTime(targetStopTime.getArrivalTime());

    // TargetStopTime
    if (nextStopTime == null
        || nextStopTime.getBlockSequence() > targetStopTime.getBlockSequence()) {
      return scheduleDeviation;
    }

    double slack = targetStopTime.getAccumulatedSlackTime()
        - nextStopTime.getAccumulatedSlackTime();

    if (nextStopTime.getArrivalTime() <= effectiveScheduleTime
        && effectiveScheduleTime <= nextStopTime.getDepartureTime()) {
      slack -= (effectiveScheduleTime - nextStopTime.getArrivalTime());
    }

    slack = Math.max(slack, 0);

    if (slack > 0 && scheduleDeviation > 0)
      scheduleDeviation -= Math.min(scheduleDeviation, slack);

    return scheduleDeviation;
  }

  private int calculateDepartureDeviation(
      BlockLocationRecordCollection collection, StopTimeEntry nextStopTime,
      StopTimeEntry targetStopTime, int effectiveScheduleTime,
      int scheduleDeviation) {

    if (targetStopTime.getDepartureTime() < effectiveScheduleTime)
      return collection.getScheduleDeviationForScheduleTime(targetStopTime.getDepartureTime());

    // TargetStopTime
    if (nextStopTime == null
        || nextStopTime.getBlockSequence() > targetStopTime.getBlockSequence()) {
      return scheduleDeviation;
    }

    double slack = targetStopTime.getAccumulatedSlackTime()
        - nextStopTime.getAccumulatedSlackTime();

    slack += targetStopTime.getSlackTime();

    if (nextStopTime.getArrivalTime() <= effectiveScheduleTime
        && effectiveScheduleTime <= nextStopTime.getDepartureTime()) {
      slack -= (effectiveScheduleTime - nextStopTime.getArrivalTime());
    }

    slack = Math.max(slack, 0);

    if (slack > 0 && scheduleDeviation > 0)
      scheduleDeviation -= Math.min(scheduleDeviation, slack);

    return scheduleDeviation;
  }

  /****
   * {@link BlockLocationService} Interface
   ****/

  @Override
  public BlockLocation getLocationForBlockInstance(BlockInstance blockInstance,
      long targetTime) {

    BlockEntry block = blockInstance.getBlock();
    ServiceDateAndId serviceDateAndId = new ServiceDateAndId(
        blockInstance.getServiceDate(), block.getId());

    List<BlockLocationRecordCollection> records = getBlockLocationRecordCollectionForBlock(
        serviceDateAndId, targetTime);

    return getBlockLocation(blockInstance, records.get(0), targetTime);
  }

  @Override
  public List<BlockLocation> getLocationsForBlockInstance(
      BlockInstance blockInstance, long targetTime) {

    BlockEntry block = blockInstance.getBlock();
    ServiceDateAndId serviceDateAndId = new ServiceDateAndId(
        blockInstance.getServiceDate(), block.getId());

    List<BlockLocationRecordCollection> records = getBlockLocationRecordCollectionForBlock(
        serviceDateAndId, targetTime);

    List<BlockLocation> locations = new ArrayList<BlockLocation>();
    for (BlockLocationRecordCollection collection : records) {
      BlockLocation location = getBlockLocation(blockInstance, collection,
          targetTime);
      if (location != null)
        locations.add(location);
    }

    return locations;
  }

  @Override
  public BlockLocation getLocationForVehicleAndTime(AgencyAndId vehicleId,
      long targetTime) {

    long fromTime = targetTime - BLOCK_RECORD_FOR_VEHICLE_SEARCH_WINDOW;
    long toTime = targetTime + BLOCK_RECORD_FOR_VEHICLE_SEARCH_WINDOW;

    List<BlockLocationRecord> records = _blockLocationRecordDao.getBlockLocationRecordsForVehicleAndTimeRange(
        vehicleId, fromTime, toTime);

    if (records.isEmpty())
      return null;

    Min<BlockLocationRecord> closest = new Min<BlockLocationRecord>();
    for (BlockLocationRecord record : records) {
      closest.add(Math.abs(record.getTime() - targetTime), record);
    }

    BlockLocationRecord representative = closest.getMinElement();
    AgencyAndId blockId = representative.getBlockId();
    long serviceDate = representative.getServiceDate();

    BlockInstance blockInstance = _activeCalendarService.getActiveBlock(
        blockId, serviceDate, targetTime);
    BlockLocationRecordCollection collection = BlockLocationRecordCollection.createFromRecords(records);

    return getBlockLocation(blockInstance, collection, targetTime);
  }

  /****
   * Private Methods
   ****/

  private BlockLocationRecord getVehicleLocationRecordAsBlockLocationRecord(
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

    BlockLocationRecord.Builder builder = BlockLocationRecord.builder();
    builder.setBlockId(blockId);
    builder.setTime(record.getTimeOfRecord());
    builder.setServiceDate(record.getServiceDate());

    if (record.isScheduleDeviationSet())
      builder.setScheduleDeviation(record.getScheduleDeviation());

    if (record.isDistanceAlongBlockSet()) {
      builder.setDistanceAlongBlock(record.getDistanceAlongBlock());

      if (!record.isScheduleDeviationSet()) {
        BlockEntry block = _transitGraphDao.getBlockEntryForId(blockId);
        List<StopTimeEntry> stopTimes = block.getStopTimes();
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

    builder.setVehicleId(record.getVehicleId());

    return builder.create();
  }

  private Map<ServiceDateAndId, List<StopTimeInstanceProxy>> getStopTimeInstancesByBlockId(
      List<StopTimeInstanceProxy> stopTimes) {

    Map<ServiceDateAndId, List<StopTimeInstanceProxy>> r = new FactoryMap<ServiceDateAndId, List<StopTimeInstanceProxy>>(
        new ArrayList<StopTimeInstanceProxy>());

    for (StopTimeInstanceProxy stopTime : stopTimes) {
      long serviceDate = stopTime.getServiceDate();
      TripEntry trip = stopTime.getTrip();
      BlockEntry block = trip.getBlock();
      AgencyAndId blockId = block.getId();
      ServiceDateAndId id = new ServiceDateAndId(serviceDate, blockId);
      r.get(id).add(stopTime);
    }
    return r;
  }

  /**
   * We add the {@link TripPositionRecord} to the local cache and persist it to
   * a back-end data-store if necessary
   */
  private void putBlockLocationRecord(BlockLocationRecord record) {

    // Cache the result
    _cache.addRecord(record);

    if (_persistBlockLocationRecords)
      addPredictionToPersistenceQueue(record);
  }

  private BlockLocation getBlockLocation(BlockInstance blockInstance,
      BlockLocationRecordCollection records, long targetTime) {

    BlockEntry blockEntry = blockInstance.getBlock();
    long serviceDate = blockInstance.getServiceDate();

    BlockLocation location = new BlockLocation();

    location.setBlockInstance(blockInstance);

    boolean predicted = !records.isEmpty();
    if (predicted) {
      location.setPredicted(true);
      long lastUpdateTime = records.getLastUpdateTime(targetTime);
      location.setLastUpdateTime(lastUpdateTime);
    }

    if (records.hasScheduleDeviations()) {
      int scheduleDeviation = records.getScheduleDeviationForTargetTime(targetTime);
      location.setScheduleDeviation(scheduleDeviation);
    }

    if (records.hasDistancesAlongBlock()) {
      double distanceAlongBlock = records.getDistanceAlongBlockForTargetTime(targetTime);
      location.setDistanceAlongBlock(distanceAlongBlock);
    }

    if (records.hasLocations()) {
      CoordinatePoint point = records.getLastLocationForTargetTime(targetTime);
      location.setLastKnownLocation(point);
    }

    location.setVehicleId(records.getVehicleId());

    ScheduledBlockLocation scheduledLocation = getScheduledBlockLocation(
        blockEntry, serviceDate, targetTime, location);

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

    return location;
  }

  private ScheduledBlockLocation getScheduledBlockLocation(
      BlockEntry blockEntry, long serviceDate, long targetTime,
      BlockLocation blockLocation) {

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
          blockEntry.getStopTimes(), effectiveScheduledTime);
    }

    if (blockLocation.hasDistanceAlongBlock()) {
      return _scheduledBlockLocationService.getScheduledBlockLocationFromDistanceAlongBlock(
          blockEntry.getStopTimes(), blockLocation.getDistanceAlongBlock());
    }

    return _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
        blockEntry.getStopTimes(), scheduledTime);
  }

  private List<BlockLocationRecordCollection> getBlockLocationRecordCollectionForBlock(
      ServiceDateAndId serviceDateAndBlockId, long targetTime) {

    List<BlockLocationRecordCollection> collections = _cache.getRecordsForBlockInstance(serviceDateAndBlockId);

    if (!collections.isEmpty()) {
      List<BlockLocationRecordCollection> inRange = new ArrayList<BlockLocationRecordCollection>();
      for (BlockLocationRecordCollection entry : collections) {
        long offset = _predictionCacheMaxOffset * 1000;
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

      List<BlockLocationRecord> predictions = _blockLocationRecordDao.getBlockLocationRecordsForBlockServiceDateAndTimeRange(
          serviceDateAndBlockId.getId(),
          serviceDateAndBlockId.getServiceDate(), fromTime, toTime);

      if (!predictions.isEmpty()) {
        Map<BlockLocationRecordKey, List<BlockLocationRecord>> recordsByKey = groupRecord(predictions);

        List<BlockLocationRecordCollection> allCollections = new ArrayList<BlockLocationRecordCollection>();
        for (List<BlockLocationRecord> records : recordsByKey.values()) {
          BlockLocationRecordCollection collection = BlockLocationRecordCollection.createFromRecords(records);
          allCollections.add(collection);
        }

        return allCollections;
      }
    }

    return Arrays.asList(new BlockLocationRecordCollection(fromTime, toTime));
  }

  private Map<BlockLocationRecordKey, List<BlockLocationRecord>> groupRecord(
      List<BlockLocationRecord> predictions) {

    // TODO Auto-generated method stub
    return null;
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

}
