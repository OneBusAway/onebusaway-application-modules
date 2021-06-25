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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.Min;
import org.onebusaway.collections.Range;
import org.onebusaway.container.ConfigurationParameter;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.EVehicleType;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data.model.TransitDataConstants;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockVehicleLocationListener;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocationService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationListener;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.onebusaway.transit_data_federation.services.realtime.RealTimeHistoryService;
import org.onebusaway.transit_data_federation.services.realtime.ScheduleDeviationSamples;
import org.onebusaway.transit_data_federation.services.realtime.VehicleLocationCacheElement;
import org.onebusaway.transit_data_federation.services.realtime.VehicleLocationCacheElements;
import org.onebusaway.transit_data_federation.services.realtime.VehicleLocationRecordCache;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.util.SystemTime;
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

  private RealTimeHistoryService _realTimeHistoryService;

  private List<BlockLocationListener> _blockLocationListeners = Collections.emptyList();

  /**
   * By default, we keep around 20 minutes of cache entries
   */
  private int _blockLocationRecordCacheWindowSize = 20 * 60;

  private int _predictionCacheMaxOffset = 5 * 60;

  private int _blockInstanceMatchingWindow = 60 * 60 * 1000;

  /**
   * When true, we will interpolate the current location of a transit vehicle
   * based on estimated schedule deviation information. If false, we will use
   * the last known location of the bus as the position.
   */
  private boolean _locationInterpolation = true;

  /**
   * When true, we attempt to interpolate the current location of the vehicle
   * given the most recent distance-along-block update. This parameter has been
   * deprecated in favor of the more-general {@link #_locationInterpolation}
   * parameter.
   */
  @Deprecated
  private boolean _distanceAlongBlockLocationInterpolation = false;

  /**
   * Should block location records be stored to the database?
   */
  private boolean _persistBlockLocationRecords = false;

  /**
   * Should we sample the schedule deviation history?
   * (expensive -- off by default)
   */
  private boolean _sampleScheduleDeviationHistory = false;

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
  public void setScheduledBlockLocationService(
      ScheduledBlockLocationService scheduleBlockLocationService) {
    _scheduledBlockLocationService = scheduleBlockLocationService;
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

  /**
   * When true, we will interpolate the current location of a transit vehicle
   * based on the last know location of the bus and the schedule deviation of
   * the bus at the time. If false, we will use the last known location of the
   * bus as the current location.
   * 
   * @param locationInterpolation
   */
  @ConfigurationParameter
  public void setLocationInterpolation(boolean locationInterpolation) {
    _locationInterpolation = locationInterpolation;
  }

  /**
   * Disablings this saves a database call to the schedule deviation history
   * table.
   * @param sampleScheduleDeviationHistory
   */
  @ConfigurationParameter
  public void setSampleScheduleDeviationHistory(Boolean sampleScheduleDeviationHistory) {
    _sampleScheduleDeviationHistory = sampleScheduleDeviationHistory;
  }

  /**
   * @param distanceAlongBlockLocationInterpolation
   * 
   * @deprecated in favor of the more general
   *             {@link #setLocationInterpolation(boolean)} configuration
   *             method.
   */
  @Deprecated
  public void setDistanceAlongBlockLocationInterpolation(
      boolean distanceAlongBlockLocationInterpolation) {
    _distanceAlongBlockLocationInterpolation = distanceAlongBlockLocationInterpolation;
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

  /**
   * 
   * @param blockInstance
   * @param cacheElements
   * @param scheduledLocation
   * @param targetTime
   * @return null if the effective scheduled block location cannot be determined
   */
  private BlockLocation getBlockLocation(BlockInstance blockInstance,
      VehicleLocationCacheElements cacheElements,
      ScheduledBlockLocation scheduledLocation, long targetTime) {

    BlockLocation location = new BlockLocation();
    location.setTime(targetTime);

    location.setBlockInstance(blockInstance);

    VehicleLocationCacheElement cacheElement = null;
    boolean isCancelled = false;
    if (cacheElements != null)
      cacheElement = cacheElements.getElementForTimestamp(targetTime);

    if (cacheElement != null) {

      VehicleLocationRecord record = cacheElement.getRecord();

      if (scheduledLocation == null)
        scheduledLocation = getScheduledBlockLocationForVehicleLocationCacheRecord(
            blockInstance, cacheElement, targetTime);

      if (scheduledLocation != null) {
        location.setEffectiveScheduleTime(scheduledLocation.getScheduledTime());
        location.setDistanceAlongBlock(scheduledLocation.getDistanceAlongBlock());

      }

      location.setBlockStartTime(record.getBlockStartTime());
      location.setPredicted(true);
      location.setLastUpdateTime(record.getTimeOfRecord());
      location.setLastLocationUpdateTime(record.getTimeOfLocationUpdate());
      location.setScheduleDeviation(record.getScheduleDeviation());
      location.setScheduleDeviations(cacheElement.getScheduleDeviations());

      if (record.isCurrentLocationSet()) {
        CoordinatePoint p = new CoordinatePoint(record.getCurrentLocationLat(),
            record.getCurrentLocationLon());
        location.setLastKnownLocation(p);
      }
      location.setOrientation(record.getCurrentOrientation());
      location.setPhase(record.getPhase());
      if (TransitDataConstants.STATUS_CANCELED.equals(record.getStatus())) {
        isCancelled = true;
        _log.debug("vehicle " + record.getVehicleId() + " is cancelled");
      }
      location.setStatus(record.getStatus());
      location.setVehicleId(record.getVehicleId());

      List<TimepointPredictionRecord> timepointPredictions = record.getTimepointPredictions();
      if (timepointPredictions != null && !timepointPredictions.isEmpty()) {

        SortedMap<Integer, Double> scheduleDeviations = new TreeMap<Integer, Double>();

        BlockConfigurationEntry blockConfig = blockInstance.getBlock();

        int tprIndexCounter = 0;
        for (TimepointPredictionRecord tpr : timepointPredictions) {

          AgencyAndId stopId = tpr.getTimepointId();
          long predictedTime;
          if (tpr.getTimepointPredictedDepartureTime() != -1) {
            predictedTime = tpr.getTimepointPredictedDepartureTime();
          } else {
            predictedTime = tpr.getTimepointPredictedArrivalTime();
          }
          if (stopId == null || predictedTime == 0)
            continue;
          for (BlockStopTimeEntry blockStopTime : blockConfig.getStopTimes()) {
            StopTimeEntry stopTime = blockStopTime.getStopTime();
            StopEntry stop = stopTime.getStop();
            // StopSequence equals to -1 when there is no stop sequence in the GTFS-rt
            if (stopId.equals(stop.getId()) && stopTime.getTrip().getId().equals(tpr.getTripId()) &&
               (tpr.getStopSequence() == -1 || stopTime.getSequence() == tpr.getStopSequence())) {
              
              if (tpr.getStopSequence() == -1 && isFirstOrLastStopInTrip(stopTime) && isLoopRoute(stopTime)) {
                // GTFS-rt feed didn't provide stop_sequence, and we have a loop, and we're attempting to apply the update to the first/last stop
                
                if (isSinglePredictionForTrip(timepointPredictions, tpr, tprIndexCounter)) {
                  continue;
                }
                
                // If this isn't the last prediction, and we're on the first stop, then apply it
                if (isLastPrediction(stopTime, timepointPredictions, tpr, tprIndexCounter)
                    && isFirstStopInRoute(stopTime)) {
                  // Do not calculate schedule deviation
                  continue;
                }
                
                // If this is the last prediction, and we're on the last stop, then apply it
                if (isFirstPrediction(stopTime, timepointPredictions, tpr, tprIndexCounter) 
                    && isLastStopInRoute(stopTime)) {
                  // Do not calculate schedule deviation
                  continue;
                }
              }
              int arrivalOrDepartureTime;
              // We currently use the scheduled arrival time of the stop as the search index
              // This MUST be consistent with the index search in ArrivalAndSepartureServiceImpl.getBestScheduleDeviation()
              int index = stopTime.getArrivalTime();
              if (tpr.getTimepointPredictedDepartureTime() != -1) {
                // Prefer departure time, because if both exist departure deviations should be the ones propagated downstream
                arrivalOrDepartureTime = stopTime.getDepartureTime();
              } else {
                arrivalOrDepartureTime = stopTime.getArrivalTime();
              }
              int deviation = (int) ((predictedTime - blockInstance.getServiceDate()) / 1000 - arrivalOrDepartureTime);
              scheduleDeviations.put(index, (double) deviation);
            }
          }
          tprIndexCounter++;
        }
        location.setTimepointPredictions(timepointPredictions);

        double[] scheduleTimes = new double[scheduleDeviations.size()];
        double[] scheduleDeviationMus = new double[scheduleDeviations.size()];
        double[] scheduleDeviationSigmas = new double[scheduleDeviations.size()];

        int index = 0;
        for (Map.Entry<Integer, Double> entry : scheduleDeviations.entrySet()) {
          scheduleTimes[index] = entry.getKey();
          scheduleDeviationMus[index] = entry.getValue();
          index++;
        }

        ScheduleDeviationSamples samples = new ScheduleDeviationSamples(
            scheduleTimes, scheduleDeviationMus, scheduleDeviationSigmas);
        location.setScheduleDeviations(samples);
      }

    } else {
      if (scheduledLocation == null)
        scheduledLocation = getScheduledBlockLocationForBlockInstance(
                blockInstance, targetTime);
    }

    /**
     * Will be null in the following cases:
     * 
     * 1) When the effective schedule time is beyond the last scheduled stop
     * time for the block.
     * 
     * 2) When the effective distance along block is outside the range of the
     * block's shape.
     */
    if (scheduledLocation == null) {
      if (isCancelled) {
        // we need to let the record flow through if cancelled
        return location;
      }
      return null;
    }

    // if we have route info, set the vehicleType
    if (scheduledLocation.getActiveTrip() != null
            && scheduledLocation.getActiveTrip().getTrip() != null
            && scheduledLocation.getActiveTrip().getTrip().getRoute() != null) {
      location.setVehicleType(EVehicleType.toEnum(scheduledLocation.getActiveTrip().getTrip().getRoute().getType()));
    }

    location.setInService(scheduledLocation.isInService());
    location.setActiveTrip(scheduledLocation.getActiveTrip());
    location.setLocation(scheduledLocation.getLocation());
    location.setOrientation(scheduledLocation.getOrientation());
    location.setScheduledDistanceAlongBlock(scheduledLocation.getDistanceAlongBlock());
    location.setClosestStop(scheduledLocation.getClosestStop());
    location.setClosestStopTimeOffset(scheduledLocation.getClosestStopTimeOffset());
    location.setNextStop(scheduledLocation.getNextStop());
    location.setNextStopTimeOffset(scheduledLocation.getNextStopTimeOffset());
    location.setPreviousStop(scheduledLocation.getPreviousStop());

    return location;
  }

  /**
   * @param timepointPredictions is contains all tprs for the block 
   * @param tpr is the current time-point prediction for given stop
   * @param tprIndexCounter
   * @return true if there is only one time-point prediction
   * for given trip
   */
  private boolean isSinglePredictionForTrip(
      List<TimepointPredictionRecord> timepointPredictions,
      TimepointPredictionRecord tpr, int tprIndexCounter) {
    
    if (timepointPredictions.size() == 1) {
      return true;
    } 
    
    boolean isNextPredictionHasSameTripId = true;
    if(tprIndexCounter + 1 < timepointPredictions.size()){
      isNextPredictionHasSameTripId = timepointPredictions.get(tprIndexCounter + 1).
          getTripId().equals(tpr.getTripId());
      if (isNextPredictionHasSameTripId) {
        return false;
      }
    }
    
    if (tprIndexCounter - 1 >= 0) {
      return !timepointPredictions.get(tprIndexCounter - 1).getTripId().equals(tpr.getTripId());
    }
    
    return !isNextPredictionHasSameTripId;
  }
  
  /**
   * Checks if the first and the last stop of the trip are the same
   * @param stopTime
   * @return true if its loop route
   */
  private boolean isLoopRoute(StopTimeEntry stopTime) {
    List<StopTimeEntry> stopTimes = stopTime.getTrip().getStopTimes();
    AgencyAndId firstStopId = stopTimes.get(0).getStop().getId();
    AgencyAndId lastStopId = stopTimes.get(stopTimes.size() -1).getStop().getId();
    return firstStopId.equals(lastStopId);
  }

  /**
   * @param stopTime
   * @return true if the given stop is the first or the last stop in given trip
   */
  private boolean isFirstOrLastStopInTrip(StopTimeEntry stopTime) {
    List<StopTimeEntry> stopTimes = stopTime.getTrip().getStopTimes();
    AgencyAndId firstStopId = stopTimes.get(0).getStop().getId();
    AgencyAndId lastStopId = stopTimes.get(stopTimes.size() -1).getStop().getId();
    AgencyAndId currentStopId = stopTime.getStop().getId();
    return firstStopId.equals(currentStopId) || lastStopId.equals(currentStopId);
  }
  
  /**
   * 
   * @param stopTime
   * @return true if the given stop is the first stop of the route
   */
  private boolean isFirstStopInRoute(StopTimeEntry stopTime) {
    List<StopTimeEntry> stopTimes = stopTime.getTrip().getStopTimes();
    return stopTimes.get(0).getSequence() == stopTime.getSequence(); 
  }
  
  /**
   * 
   * @param stopTime
   * @return true if the given stop is the last stop of the route
   */
  private boolean isLastStopInRoute(StopTimeEntry stopTime) {
    List<StopTimeEntry> stopTimes = stopTime.getTrip().getStopTimes();
    return stopTimes.get(stopTimes.size() -1).getSequence() == stopTime.getSequence(); 
  }
  
  /**
   * 
   * @param stopTime is the current stop
   * @param timepointPredictions is the all time-point predictions in the block
   * @param timepointPredictionRecord is the current tpr for the stop
   * @param index is the index of the current tpr in timepointPredictions
   * @return true if the given tpr is the first prediction for the trip
   */
  private boolean isFirstPrediction (StopTimeEntry stopTime, List<TimepointPredictionRecord> timepointPredictions,
      TimepointPredictionRecord timepointPredictionRecord, int index) {
    
    List<StopTimeEntry> stopTimes = stopTime.getTrip().getStopTimes();
    AgencyAndId firstStopId = stopTimes.get(0).getStop().getId();
    
    if (firstStopId.equals(timepointPredictionRecord.getTimepointId())
        && stopTime.getTrip().getId().equals(timepointPredictionRecord.getTripId())) {
      return index == 0 || ( index > 0 &&
          !timepointPredictions.get(index - 1).getTripId().equals(timepointPredictionRecord.getTripId()));
    }
    return false;
  }
  
  /**
   * 
   * @param stopTime is the current stop
   * @param timepointPredictions is the all time-point predictions in the block
   * @param timepointPredictionRecord is the current tpr for the stop
   * @param index is the index of the current tpr in timepointPredictions
   * @return return true if the given tpr is the last prediction for the trip
   */
  private boolean isLastPrediction (StopTimeEntry stopTime, List<TimepointPredictionRecord> timepointPredictions,
      TimepointPredictionRecord timepointPredictionRecord, int index) {
    
    List<StopTimeEntry> stopTimes = stopTime.getTrip().getStopTimes();
    AgencyAndId lastStopId = stopTimes.get(stopTimes.size() - 1).getStop().getId();
    
    if (lastStopId.equals(timepointPredictionRecord.getTimepointId())
        && stopTime.getTrip().getId().equals(timepointPredictionRecord.getTripId())) {
      return index + 1 == timepointPredictions.size() || ( index < timepointPredictions.size() &&
          !timepointPredictions.get(index + 1).getTripId().equals(timepointPredictionRecord.getTripId()));
    }
    return false;
  }
  
  /****
   * {@link ScheduledBlockLocation} Methods
   ****/

  private ScheduledBlockLocation getScheduledBlockLocationForVehicleLocationRecord(
      VehicleLocationRecord record, BlockInstance blockInstance) {

    BlockConfigurationEntry blockConfig = blockInstance.getBlock();
    long serviceDate = blockInstance.getServiceDate();

    long targetTime = record.getTimeOfRecord();

    int scheduledTime = (int) ((targetTime - serviceDate) / 1000);

    if (record.isScheduleDeviationSet()) {

      /**
       * Effective scheduled time is the point that a transit vehicle is at on
       * its schedule, with schedule deviation taken into account. So if it's
       * 100 minutes into the current service date and the bus is running 10
       * minutes late, it's actually at the 90 minute point in its scheduled
       * operation.
       */
      int effectiveScheduledTime = (int) (scheduledTime - record.getScheduleDeviation());

      return _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
          blockConfig, effectiveScheduledTime);
    }

    if (record.isDistanceAlongBlockSet()) {
      return _scheduledBlockLocationService.getScheduledBlockLocationFromDistanceAlongBlock(
          blockConfig, record.getDistanceAlongBlock());
    }

    return _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
        blockConfig, scheduledTime);
  }

  private ScheduledBlockLocation getScheduledBlockLocationForBlockInstance(
      BlockInstance blockInstance, long targetTime) {

    BlockConfigurationEntry blockConfig = blockInstance.getBlock();
    long serviceDate = blockInstance.getServiceDate();

    int scheduledTime = (int) ((targetTime - serviceDate) / 1000);

    return _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
        blockConfig, scheduledTime);
  }

  private ScheduledBlockLocation getScheduledBlockLocationForVehicleLocationCacheRecord(
      BlockInstance blockInstance, VehicleLocationCacheElement cacheElement,
      long targetTime) {

    VehicleLocationRecord record = cacheElement.getRecord();
    ScheduledBlockLocation scheduledBlockLocation = cacheElement.getScheduledBlockLocation();

    BlockConfigurationEntry blockConfig = blockInstance.getBlock();
    long serviceDate = blockInstance.getServiceDate();

    int scheduledTime = (int) ((targetTime - serviceDate) / 1000);

    /**
     * If location interpolation has been turned off, then we assume the vehicle
     * is at its last known location, so we return that if it's been stored with
     * the cache element.
     */
    if (!_locationInterpolation && scheduledBlockLocation != null) {
      return scheduledBlockLocation;
    }

    if (record.isScheduleDeviationSet()) {

      /**
       * Effective scheduled time is the point that a transit vehicle is at on
       * its schedule, with schedule deviation taken into account. So if it's
       * 100 minutes into the current service date and the bus is running 10
       * minutes late, it's actually at the 90 minute point in its scheduled
       * operation.
       */
      int effectiveScheduledTime = (int) (scheduledTime - record.getScheduleDeviation());

      if (scheduledBlockLocation != null
          && scheduledBlockLocation.getScheduledTime() <= effectiveScheduledTime) {

        return _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
            scheduledBlockLocation, effectiveScheduledTime);
      }

      return _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
          blockConfig, effectiveScheduledTime);
    }

    if (record.isDistanceAlongBlockSet()) {

      if ((_locationInterpolation || _distanceAlongBlockLocationInterpolation)
          && scheduledBlockLocation != null
          && scheduledBlockLocation.getDistanceAlongBlock() <= record.getDistanceAlongBlock()) {

        int ellapsedTime = (int) ((targetTime - record.getTimeOfRecord()) / 1000);

        if (ellapsedTime >= 0) {

          int effectiveScheduledTime = scheduledBlockLocation.getScheduledTime()
              + ellapsedTime;

          return _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
              blockConfig, effectiveScheduledTime);
        }

        return _scheduledBlockLocationService.getScheduledBlockLocationFromDistanceAlongBlock(
            scheduledBlockLocation, record.getDistanceAlongBlock());
      }

      return _scheduledBlockLocationService.getScheduledBlockLocationFromDistanceAlongBlock(
          blockConfig, record.getDistanceAlongBlock());
    }

    return _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
        blockConfig, scheduledTime);
  }

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
      long offset = _predictionCacheMaxOffset * 1000;
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
  }

  private class BlockInstanceStrategy implements RecordStrategy {

    private BlockInstance _blockInstance;

    public BlockInstanceStrategy(BlockInstance blockInstance) {
      _blockInstance = blockInstance;
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

    private AgencyAndId _vehicleId;

    public VehicleIdRecordStrategy(AgencyAndId vehicleId) {
      _vehicleId = vehicleId;
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
