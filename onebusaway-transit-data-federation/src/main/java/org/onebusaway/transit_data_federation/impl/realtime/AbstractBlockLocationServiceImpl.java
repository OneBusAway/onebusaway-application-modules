/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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

import org.onebusaway.container.ConfigurationParameter;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.EVehicleType;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data.model.TransitDataConstants;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocationService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.ScheduleDeviationSamples;
import org.onebusaway.transit_data_federation.services.realtime.VehicleLocationCacheElement;
import org.onebusaway.transit_data_federation.services.realtime.VehicleLocationCacheElements;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Common logic between static and dynamic BlockLocations.
 */
public abstract class AbstractBlockLocationServiceImpl {

  private static Logger _log = LoggerFactory.getLogger(AbstractBlockLocationServiceImpl.class);

  /**
   * When true, we will interpolate the current location of a transit vehicle
   * based on estimated schedule deviation information. If false, we will use
   * the last known location of the bus as the position.
   */
  private boolean _locationInterpolation = false;

  /**
   * Should we sample the schedule deviation history?
   * (expensive -- off by default)
   */
  protected boolean _sampleScheduleDeviationHistory = false;


  protected ScheduledBlockLocationService _scheduledBlockLocationService;
  /**
   * When true, we attempt to interpolate the current location of the vehicle
   * given the most recent distance-along-block update. This parameter has been
   * deprecated in favor of the more-general {@link #_locationInterpolation}
   * parameter.
   */
  @Deprecated
  private boolean _distanceAlongBlockLocationInterpolation = false;


  @Autowired
  public void setScheduledBlockLocationService(
          ScheduledBlockLocationService scheduleBlockLocationService) {
    _scheduledBlockLocationService = scheduleBlockLocationService;
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

  /**
   *
   * @param blockInstance
   * @param cacheElements
   * @param scheduledLocation
   * @param targetTime
   * @return null if the effective scheduled block location cannot be determined
   */
  protected BlockLocation getBlockLocation(BlockInstance blockInstance,
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
      location.setVehicleFeatures(record.getVehicleFeatures());

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

  protected ScheduledBlockLocation getScheduledBlockLocationForVehicleLocationCacheRecord(
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

  protected ScheduledBlockLocation getScheduledBlockLocationForBlockInstance(
          BlockInstance blockInstance, long targetTime) {

    BlockConfigurationEntry blockConfig = blockInstance.getBlock();
    long serviceDate = blockInstance.getServiceDate();

    int scheduledTime = (int) ((targetTime - serviceDate) / 1000);

    return _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
            blockConfig, scheduledTime);
  }

  /**
   * @param stopTime
   * @return true if the given stop is the first or the last stop in given trip
   */
  protected boolean isFirstOrLastStopInTrip(StopTimeEntry stopTime) {
    List<StopTimeEntry> stopTimes = stopTime.getTrip().getStopTimes();
    AgencyAndId firstStopId = stopTimes.get(0).getStop().getId();
    AgencyAndId lastStopId = stopTimes.get(stopTimes.size() -1).getStop().getId();
    AgencyAndId currentStopId = stopTime.getStop().getId();
    return firstStopId.equals(currentStopId) || lastStopId.equals(currentStopId);
  }

  /**
   * Checks if the first and the last stop of the trip are the same
   * @param stopTime
   * @return true if its loop route
   */
  protected boolean isLoopRoute(StopTimeEntry stopTime) {
    List<StopTimeEntry> stopTimes = stopTime.getTrip().getStopTimes();
    AgencyAndId firstStopId = stopTimes.get(0).getStop().getId();
    AgencyAndId lastStopId = stopTimes.get(stopTimes.size() -1).getStop().getId();
    return firstStopId.equals(lastStopId);
  }


  /**
   *
   * @param stopTime
   * @return true if the given stop is the first stop of the route
   */
  protected boolean isFirstStopInRoute(StopTimeEntry stopTime) {
    List<StopTimeEntry> stopTimes = stopTime.getTrip().getStopTimes();
    return stopTimes.get(0).getSequence() == stopTime.getSequence();
  }

  /**
   *
   * @param stopTime
   * @return true if the given stop is the last stop of the route
   */
  protected boolean isLastStopInRoute(StopTimeEntry stopTime) {
    List<StopTimeEntry> stopTimes = stopTime.getTrip().getStopTimes();
    return stopTimes.get(stopTimes.size() -1).getSequence() == stopTime.getSequence();
  }


  /**
   * @param timepointPredictions is contains all tprs for the block
   * @param tpr is the current time-point prediction for given stop
   * @param tprIndexCounter
   * @return true if there is only one time-point prediction
   * for given trip
   */
  protected boolean isSinglePredictionForTrip(
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


  /**
   *
   * @param stopTime is the current stop
   * @param timepointPredictions is the all time-point predictions in the block
   * @param timepointPredictionRecord is the current tpr for the stop
   * @param index is the index of the current tpr in timepointPredictions
   * @return true if the given tpr is the first prediction for the trip
   */
  protected boolean isFirstPrediction (StopTimeEntry stopTime, List<TimepointPredictionRecord> timepointPredictions,
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

  protected ScheduledBlockLocation getScheduledBlockLocationForVehicleLocationRecord(
          VehicleLocationRecord record, BlockInstance blockInstance) {

    BlockConfigurationEntry blockConfig = blockInstance.getBlock();
    // we don't have a specified time, so use the timeOfRecord to see if the trip is
    // active now
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

    // use the scheduledTime we guessed at earlier
    return _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
            blockConfig, scheduledTime);
  }


}
