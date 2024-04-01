/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 <inf71391@gmail.com>
 * Copyright (C) 2012 Google, Inc.
 * Copyright (C) 2015 University of South Florida
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.impl.realtime;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.Min;
import org.onebusaway.collections.Range;
import org.onebusaway.container.ConfigurationParameter;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.EVehicleType;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.blocks.*;
import org.onebusaway.transit_data_federation.services.realtime.*;
import org.onebusaway.transit_data_federation.services.transit_graph.*;
import org.onebusaway.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
public class BlockLocationServiceImpl extends AbstractBlockLocationServiceImpl implements BlockLocationService, BlockVehicleLocationListener {

  private static Logger _log = LoggerFactory.getLogger(BlockLocationServiceImpl.class);

  private VehicleLocationRecordCache _cache;

  private BlockLocationRecordDao _blockLocationRecordDao;

  private TransitGraphDao _transitGraphDao;


  private BlockCalendarService _blockCalendarService;

  private RealTimeHistoryService _realTimeHistoryService;

  private DynamicHelper helper = new DynamicHelper();
  private List<BlockLocationListener> _blockLocationListeners = Collections.emptyList();

  /**
   * By default, we keep around 20 minutes of cache entries, though
   * we may not actually use all of that data.  @see _predictionCacheMaxOffset
   */
  private int _blockLocationRecordCacheWindowSize = 20 * 60;

  /**
   * How timely the cache element must be to be considered.  Offset applies
   * both into the past and future.
   */
  private int _predictionCacheMaxOffset = 5 * 60; // default 5 minute cache offset

  /**
   * For certain modes we override the _predictionCacheMaxOffset with a
   * far shorter interval as AVL data is DYNAMIC!  That is it changes
   * state rapidly and may contradict previous updates resulting in duplicates.
   */
  private int _statelessAvlOffset = 30; // high frequency AVL demands a shorter cache period
  public void setStatelessAvlOffset(int offset) {
    _statelessAvlOffset = offset;
  }
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
  public void setBlockCalendarService(BlockCalendarService blockCalendarService) {
    _blockCalendarService = blockCalendarService;
  }

  @Autowired
  public void setRealTimeHistoryService(
          RealTimeHistoryService realTimeHistoryService) {
    _realTimeHistoryService = realTimeHistoryService;
  }

  @Autowired
  public void setBlockLocationListeners(List<BlockLocationListener> listeners) {
    _blockLocationListeners = listeners;
  }

  /**
   * Controls how far back in time we include records in the
   * {@link BlockLocationRecordCollection} for each active trip.
   *
   * @param windowSize in seconds
   */
  @ConfigurationParameter
  public void setBlockLocationRecordCacheWindowSize(int windowSize) {
    _blockLocationRecordCacheWindowSize = windowSize;
  }

  /**
   * Should we persist {@link BlockLocationRecord} records to an underlying
   * datastore. Useful if you wish to query trip status for historic analysis.
   *
   * @param persistBlockLocationRecords
   */
  @ConfigurationParameter
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

    if (instance != null) {

      ScheduledBlockLocation scheduledBlockLocation = getScheduledBlockLocationForVehicleLocationRecord(
              record, instance);

      scheduledBlockLocation.setMutated(record.getMutated());

      if (!record.isScheduleDeviationSet() && scheduledBlockLocation != null ) {
        int deviation = (int) ((record.getTimeOfRecord() - record.getServiceDate()) / 1000 - scheduledBlockLocation.getScheduledTime());
        record.setScheduleDeviation(deviation);
      }

      ScheduleDeviationSamples samples = null;
      if (_sampleScheduleDeviationHistory == true) {
        samples = _realTimeHistoryService.sampleScheduleDeviationsForVehicle(
                instance, record, scheduledBlockLocation);
      }

      putBlockLocationRecord(instance, record, scheduledBlockLocation, samples);
    }
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
                                                   TargetTime time) {

    List<VehicleLocationCacheElements> records = getBlockLocationRecordCollectionForBlock(
            blockInstance, time);

    VehicleLocationCacheElements record = null;
    if (!records.isEmpty())
      record = records.get(0);

    // TODO : find a better way to pick?
    return getBlockLocation(blockInstance, record, null, time.getTargetTime());
  }

  @Override
  public BlockLocation getLocationForBlockInstanceAndScheduledBlockLocation(
          BlockInstance blockInstance, ScheduledBlockLocation scheduledLocation,
          long targetTime) {
    return getBlockLocation(blockInstance, null, scheduledLocation, targetTime);
  }

  @Override
  public List<BlockLocation> getLocationsForBlockInstance(
          BlockInstance blockInstance, TargetTime time) {
    List<VehicleLocationCacheElements> records = getBlockLocationRecordCollectionForBlock(
            blockInstance, time);

    List<BlockLocation> locations = new ArrayList<BlockLocation>();
    for (VehicleLocationCacheElements cacheRecord : records) {
      BlockLocation location = getBlockLocation(blockInstance, cacheRecord,
              null, time.getTargetTime());
      if (location != null)
        locations.add(location);
    }

    return locations;
  }

  @Override
  public Map<AgencyAndId, List<BlockLocation>> getLocationsForBlockInstance(
          BlockInstance blockInstance, List<Date> times, long currentTime) {

    Map<AgencyAndId, List<BlockLocation>> locationsByVehicleId = new FactoryMap<AgencyAndId, List<BlockLocation>>(
            new ArrayList<BlockLocation>());

    for (Date time : times) {
      TargetTime tt = new TargetTime(time.getTime(), currentTime);
      List<VehicleLocationCacheElements> records = getBlockLocationRecordCollectionForBlock(
              blockInstance, tt);
      for (VehicleLocationCacheElements cacheRecord : records) {
        BlockLocation location = getBlockLocation(blockInstance, cacheRecord,
                null, time.getTime());
        if (location != null) {
          locationsByVehicleId.get(location.getVehicleId()).add(location);
        }
      }
    }

    return locationsByVehicleId;
  }

  @Override
  public BlockLocation getScheduledLocationForBlockInstance(
          BlockInstance blockInstance, long targetTime) {
    return getBlockLocation(blockInstance, null, null, targetTime);
  }

  @Override
  public BlockLocation getLocationForVehicleAndTime(AgencyAndId vehicleId,
                                                    TargetTime targetTime) {

    List<VehicleLocationCacheElements> cacheRecords = getBlockLocationRecordCollectionForVehicle(
            vehicleId, targetTime);

    // TODO : We might take a bit more care in picking the collection if
    // multiple collections are returned
    if (cacheRecords.size() > 1) {
      _log.error("multiple cache entries for vehicle " + vehicleId);
    }
    for (VehicleLocationCacheElements cacheRecord : cacheRecords) {
      BlockInstance blockInstance = cacheRecord.getBlockInstance();
      BlockLocation location = getBlockLocation(blockInstance, cacheRecord,
              null, targetTime.getTargetTime());
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
   * We add the {@link BlockInstance} to the local cache and persist it to
   * a back-end data-store if necessary
   *
   * @param scheduledBlockLocation TODO
   * @param samples
   */
  private void putBlockLocationRecord(BlockInstance blockInstance,
                                      VehicleLocationRecord record,
                                      ScheduledBlockLocation scheduledBlockLocation,
                                      ScheduleDeviationSamples samples) {

    // Cache the result
    VehicleLocationCacheElements elements = _cache.addRecord(blockInstance,
            record, scheduledBlockLocation, samples);

    if (!CollectionsLibrary.isEmpty(_blockLocationListeners)) {

      /**
       * We only fill in the block location if we have listeners
       */
      BlockLocation location = getBlockLocation(blockInstance, elements,
              scheduledBlockLocation, record.getTimeOfRecord());
      if (location != null) {
        for (BlockLocationListener listener : _blockLocationListeners) {
          listener.handleBlockLocation(location);
        }
      }
    }

    if (_persistBlockLocationRecords) {
      List<BlockLocationRecord> blockLocationRecords = getVehicleLocationRecordAsBlockLocationRecord(
              blockInstance, record, scheduledBlockLocation);
      addPredictionToPersistenceQueue(blockLocationRecords);
    }
  }


  /****
   * {@link ScheduledBlockLocation} Methods
   ****/



  private List<VehicleLocationCacheElements> getBlockLocationRecordCollectionForBlock(
          BlockInstance blockInstance, TargetTime time) {
    return getBlockLocationRecordCollections(new BlockInstanceStrategy(
            blockInstance), time);
  }

  private List<VehicleLocationCacheElements> getBlockLocationRecordCollectionForVehicle(
          AgencyAndId vehicleId, TargetTime time) {
    return getBlockLocationRecordCollections(new VehicleIdRecordStrategy(
            vehicleId), time);
  }

  private List<VehicleLocationCacheElements> getBlockLocationRecordCollections(
          RecordStrategy strategy, TargetTime time) {

    List<VehicleLocationCacheElements> entries = strategy.getRecordsFromCache();

    if (!entries.isEmpty()) {
      List<VehicleLocationCacheElements> inRange = new ArrayList<VehicleLocationCacheElements>();
      // determine the window for the age of the cache elements
      long offset = _predictionCacheMaxOffset * 1000;
      if (strategy.getRecordAgeWindowInSeconds() != null) {
        // we have a local override for the age, use it
        offset = strategy.getRecordAgeWindowInSeconds() * 1000;
      }
      for (VehicleLocationCacheElements elements : entries) {
        if (elements.isEmpty())
          continue;
        Range range = elements.getTimeRange();
        long tFrom = (long) (range.getMin() - offset);
        long tTo = (long) (range.getMax() + offset);
        if (tFrom <= time.getCurrentTime() && time.getCurrentTime() <= tTo)
          inRange.add(elements);
      }
      if (!inRange.isEmpty())
        return inRange;
    }

    long offset = _blockLocationRecordCacheWindowSize * 1000 / 2;

    // We only consult persisted cache entries if the requested target time is
    // not within our current cache window
    boolean outOfRange = time.getTargetTime() + offset < time.getCurrentTime()
            || time.getCurrentTime() < time.getTargetTime() - offset;

    if (outOfRange && _persistBlockLocationRecords) {

      _blockLocationRecordPersistentStoreAccessCount.incrementAndGet();

      long fromTime = time.getTargetTime() - offset;
      long toTime = time.getTargetTime() + offset;

      List<BlockLocationRecord> predictions = strategy.getRecordsFromDao(
              fromTime, toTime);

      if (!predictions.isEmpty()) {

        Map<BlockLocationRecordKey, List<BlockLocationRecord>> recordsByKey = groupRecord(predictions);

        List<VehicleLocationCacheElements> allCollections = new ArrayList<VehicleLocationCacheElements>();
        for (Map.Entry<BlockLocationRecordKey, List<BlockLocationRecord>> entry : recordsByKey.entrySet()) {
          BlockLocationRecordKey key = entry.getKey();
          List<BlockLocationRecord> blockLocationRecords = entry.getValue();
          List<VehicleLocationCacheElements> someRecords = getBlockLocationRecordsAsVehicleLocationRecords(
                  key.getBlockInstance(), blockLocationRecords);
          allCollections.addAll(someRecords);
        }

        return allCollections;
      }
    }

    return Collections.emptyList();
  }

  private List<BlockLocationRecord> getVehicleLocationRecordAsBlockLocationRecord(
          BlockInstance blockInstance, VehicleLocationRecord record,
          ScheduledBlockLocation scheduledBlockLocation) {

    BlockLocationRecord.Builder builder = BlockLocationRecord.builder();

    if (scheduledBlockLocation != null) {

      BlockTripEntry activeTrip = scheduledBlockLocation.getActiveTrip();
      builder.setTripId(activeTrip.getTrip().getId());
      builder.setBlockId(activeTrip.getBlockConfiguration().getBlock().getId());
      // store the vehicleType for later retrieval
      builder.setVehicleType(EVehicleType.toEnum(activeTrip.getTrip().getRoute().getType()));

      double distanceAlongBlock = scheduledBlockLocation.getDistanceAlongBlock();
      builder.setDistanceAlongBlock(distanceAlongBlock);

      double distanceAlongTrip = distanceAlongBlock
              - activeTrip.getDistanceAlongBlock();
      builder.setDistanceAlongTrip(distanceAlongTrip);
    }

    if (record.getBlockId() != null)
      builder.setBlockId(record.getBlockId());
    if (record.getTripId() != null)
      builder.setTripId(record.getTripId());
    builder.setTime(record.getTimeOfRecord());
    builder.setServiceDate(record.getServiceDate());

    if (record.isScheduleDeviationSet())
      builder.setScheduleDeviation(record.getScheduleDeviation());

    if (record.isDistanceAlongBlockSet()) {
      double distanceAlongBlock = record.getDistanceAlongBlock();
      builder.setDistanceAlongBlock(distanceAlongBlock);
      AgencyAndId tripId = record.getTripId();
      if (tripId != null) {
        BlockConfigurationEntry block = blockInstance.getBlock();
        for (BlockTripEntry blockTrip : block.getTrips()) {
          TripEntry trip = blockTrip.getTrip();
          if (trip.getId().equals(tripId)) {
            double distanceAlongTrip = distanceAlongBlock
                    - blockTrip.getDistanceAlongBlock();
            builder.setDistanceAlongTrip(distanceAlongTrip);
          }
        }
      }
    }

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
      // because the builder does not support schedule relationship suppress skipped stops
      if (!tpr.isSkipped()) {
        // note: this may return trips outside active trip
        builder.setTimepointId(tpr.getTimepointId());
        builder.setTimepointScheduledTime(tpr.getTimepointScheduledTime());
        builder.setTimepointPredictedArrivalTime(tpr.getTimepointPredictedArrivalTime());
        builder.setTimepointPredictedDepartureTime(tpr.getTimepointPredictedDepartureTime());
        results.add(builder.create());
      }
    }
    return results;
  }

  private List<VehicleLocationCacheElements> getBlockLocationRecordsAsVehicleLocationRecords(
          BlockInstance blockInstance, List<BlockLocationRecord> records) {

    List<VehicleLocationCacheElements> results = new ArrayList<VehicleLocationCacheElements>();

    for (BlockLocationRecord record : records) {
      VehicleLocationRecord vlr = new VehicleLocationRecord();
      vlr.setBlockId(blockInstance.getBlock().getBlock().getId());
      if (record.isLocationSet()) {
        vlr.setCurrentLocationLat(record.getLocationLat());
        vlr.setCurrentLocationLon(record.getLocationLon());
      }
      if (record.isOrientationSet())
        vlr.setCurrentOrientation(record.getOrientation());
      if (record.isDistanceAlongBlockSet())
        vlr.setDistanceAlongBlock(record.getDistanceAlongBlock());
      vlr.setPhase(record.getPhase());
      if (record.isScheduleDeviationSet())
        vlr.setScheduleDeviation(record.getScheduleDeviation());
      vlr.setServiceDate(record.getServiceDate());
      vlr.setStatus(record.getStatus());
      vlr.setTimeOfRecord(record.getTime());
      vlr.setVehicleId(record.getVehicleId());

      VehicleLocationCacheElement element = new VehicleLocationCacheElement(
              vlr, null, null);
      VehicleLocationCacheElements elements = new VehicleLocationCacheElements(
              blockInstance, element);
      results.add(elements);
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

        long t1 = SystemTime.currentTimeMillis();
        _blockLocationRecordDao.saveBlockLocationRecords(queue);
        long t2 = SystemTime.currentTimeMillis();
        _lastInsertDuration = t2 - t1;
        _lastInsertCount = queue.size();
      } catch (Throwable ex) {
        _log.error("error writing block location records to dao", ex);
      }
    }
  }

  private interface RecordStrategy {

    public List<VehicleLocationCacheElements> getRecordsFromCache();

    public List<BlockLocationRecord> getRecordsFromDao(long fromTime,
                                                       long toTime);

    public Integer getRecordAgeWindowInSeconds();
  }

  private class BlockInstanceStrategy implements RecordStrategy {

    private final BlockInstance _blockInstance;
    private final Integer _recordAgeWindowInSeconds;
    public Integer getRecordAgeWindowInSeconds() {
      return _recordAgeWindowInSeconds;
    }

    public BlockInstanceStrategy(BlockInstance blockInstance) {
      _blockInstance = blockInstance;
      _recordAgeWindowInSeconds = getRecordAgeWindowFromBlock(blockInstance);
    }

    private Integer getRecordAgeWindowFromBlock(BlockInstance blockInstance) {
      if (blockInstance != null)
        if (!blockInstance.getBlock().getTrips().isEmpty())
          if (blockInstance.getBlock().getTrips().get(0).getTrip() != null)
            if (blockInstance.getBlock().getTrips().get(0).getTrip().getRoute() != null)
              return getRecordAgeFromRouteType(blockInstance.getBlock().getTrips().get(0).getTrip().getRoute().getType());
      return null;
    }

    private Integer getRecordAgeFromRouteType(int routeType) {
      // SUBWAY AVL demands a different caching configuration
      if (routeType == EVehicleType.SUBWAY.getGtfsType())
        return _statelessAvlOffset;
      return null;
    }

    @Override
    public List<VehicleLocationCacheElements> getRecordsFromCache() {
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

    private final AgencyAndId _vehicleId;
    private final Integer _recordAgeWindowInSeconds;
    public final Integer getRecordAgeWindowInSeconds() {
      return _recordAgeWindowInSeconds;
    }

    public VehicleIdRecordStrategy(AgencyAndId vehicleId) {
      _vehicleId = vehicleId;
      _recordAgeWindowInSeconds = null;
    }

    @Override
    public List<VehicleLocationCacheElements> getRecordsFromCache() {
      VehicleLocationCacheElements elementsForVehicleId = _cache.getRecordForVehicleId(_vehicleId);
      if (elementsForVehicleId == null)
        return Collections.emptyList();
      return Arrays.asList(elementsForVehicleId);
    }

    @Override
    public List<BlockLocationRecord> getRecordsFromDao(long fromTime,
                                                       long toTime) {
      return _blockLocationRecordDao.getBlockLocationRecordsForVehicleAndTimeRange(
              _vehicleId, fromTime, toTime);
    }
  }

}
