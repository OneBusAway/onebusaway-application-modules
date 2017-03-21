/**
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.collections.Min;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.mappings.InvalidStopTimeException;
import org.onebusaway.gtfs.serialization.mappings.StopTimeFieldMappingFactory;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;
import com.google.transit.realtime.GtfsRealtimeOneBusAway;
import com.google.transit.realtime.GtfsRealtimeOneBusAway.OneBusAwayTripUpdate;

class GtfsRealtimeTripLibrary {

  private static final Logger _log = LoggerFactory.getLogger(GtfsRealtimeTripLibrary.class);

  private GtfsRealtimeEntitySource _entitySource;

  private BlockCalendarService _blockCalendarService;

  private String[] _agencyIds = {};
  void setAgencyIds(List<String> agencies) {
      if (agencies != null) {
        _agencyIds = agencies.toArray(_agencyIds);
      }
  }
  private boolean _stripAgencyPrefix = true;
  public void setStripAgencyPrefix(boolean remove) {
    _stripAgencyPrefix = remove;
  }
  /**
   * This is primarily here to assist with unit testing.
   */
  private long _currentTime = 0;

  public void setEntitySource(GtfsRealtimeEntitySource entitySource) {
    _entitySource = entitySource;
  }

  public void setBlockCalendarService(BlockCalendarService blockCalendarService) {
    _blockCalendarService = blockCalendarService;
  }

  public long getCurrentTime() {
    return _currentTime;
  }

  public void setCurrentTime(long currentTime) {
    _currentTime = currentTime;
  }

  public List<CombinedTripUpdatesAndVehiclePosition> groupTripUpdatesAndVehiclePositions(
      FeedMessage tripUpdates, FeedMessage vehiclePositions) {
    return groupTripUpdatesAndVehiclePositions(null, tripUpdates, vehiclePositions);
  }
  
  public List<CombinedTripUpdatesAndVehiclePosition> groupTripUpdatesAndVehiclePositions(MonitoredResult result,
      FeedMessage tripUpdates, FeedMessage vehiclePositions) {

    Map<BlockDescriptor, List<TripUpdate>> tripUpdatesByBlockDescriptor = getTripUpdatesByBlockDescriptor(result, tripUpdates);
    boolean tripsIncludeVehicleIds = determineIfTripUpdatesIncludeVehicleIds(tripUpdatesByBlockDescriptor.keySet());
    Map<BlockDescriptor, FeedEntity> vehiclePositionsByBlockDescriptor = getVehiclePositionsByBlockDescriptor(result,
        vehiclePositions, tripsIncludeVehicleIds);

    List<CombinedTripUpdatesAndVehiclePosition> updates = new ArrayList<CombinedTripUpdatesAndVehiclePosition>(
        tripUpdatesByBlockDescriptor.size());

    for (Map.Entry<BlockDescriptor, List<TripUpdate>> entry : tripUpdatesByBlockDescriptor.entrySet()) {

      CombinedTripUpdatesAndVehiclePosition update = new CombinedTripUpdatesAndVehiclePosition();
      update.block = entry.getKey();
      update.tripUpdates = entry.getValue();

      FeedEntity vehiclePositionEntity = vehiclePositionsByBlockDescriptor.get(update.block);
      if (vehiclePositionEntity != null) {
        VehiclePosition vehiclePosition = vehiclePositionEntity.getVehicle();
        update.vehiclePosition = vehiclePosition;
        if (vehiclePosition.hasVehicle()) {
          VehicleDescriptor vehicle = vehiclePosition.getVehicle();
          if (vehicle.hasId()) {
            update.block.setVehicleId(vehicle.getId());
          }
        }
      }

      if (update.block.getVehicleId() == null) {
        for (TripUpdate tripUpdate : update.tripUpdates) {
          if (tripUpdate.hasVehicle()) {
            VehicleDescriptor vehicle = tripUpdate.getVehicle();
            if (vehicle.hasId()) {
              update.block.setVehicleId(vehicle.getId());
            }
          }
        }
      }

      updates.add(update);
    }

    return updates;
  }

  /**
   * The {@link VehicleLocationRecord} is guarnateed to have a
   * {@link VehicleLocationRecord#getVehicleId()} value.
   * 
   * @param update
   * @return
   */
  public VehicleLocationRecord createVehicleLocationRecordForUpdate(
      CombinedTripUpdatesAndVehiclePosition update) {
    return createVehicleLocationRecordForUpdate(null, update);
  }    
    
    public VehicleLocationRecord createVehicleLocationRecordForUpdate(MonitoredResult result,
        CombinedTripUpdatesAndVehiclePosition update) {


    VehicleLocationRecord record = new VehicleLocationRecord();
    record.setTimeOfRecord(currentTime());

    BlockDescriptor blockDescriptor = update.block;

    record.setBlockId(blockDescriptor.getBlockInstance().getBlock().getBlock().getId());
    record.setStatus(blockDescriptor.getScheduleRelationship().toString());

    applyTripUpdatesToRecord(result, blockDescriptor, update.tripUpdates, record, update.block.getVehicleId());

    if (update.vehiclePosition != null) {
      applyVehiclePositionToRecord(update.vehiclePosition, record);
    }

    /**
     * By default, we use the block id as the vehicle id
     */
    record.setVehicleId(record.getBlockId());

    if (result != null) {
      if (record.getTripId() != null) {
        result.addMatchedTripId(record.getTripId().toString());
      } else {
        // we don't have a tripId, use the BlockId instead
        result.addMatchedTripId(record.getBlockId().toString());
      }
    }
    
    if (blockDescriptor.getVehicleId() != null) {
      String agencyId = record.getBlockId().getAgencyId();
      record.setVehicleId(new AgencyAndId(agencyId,
          parseVehicleId(blockDescriptor.getVehicleId())));
    }

    return record;
  }


  // parse out the vehicle id in a null safe manner
  private String parseVehicleId(String rawVehicleId) {
    if (rawVehicleId == null) return null;
    String[] strings = rawVehicleId.split("_");
    if (strings == null || strings.length < 2) return rawVehicleId;
    return strings[strings.length-1];

  }
  /****
   * 
   ****/

  private boolean determineIfTripUpdatesIncludeVehicleIds(
      Collection<BlockDescriptor> blockDescriptors) {

    int vehicleIdCount = 0;
    for (BlockDescriptor blockDescriptor : blockDescriptors) {
      if (blockDescriptor.getVehicleId() != null)
        vehicleIdCount++;
    }

    return vehicleIdCount > blockDescriptors.size() / 2;
  }

  private Map<BlockDescriptor, List<TripUpdate>> getTripUpdatesByBlockDescriptor(MonitoredResult result,
      FeedMessage tripUpdates) {

    Map<BlockDescriptor, List<TripUpdate>> tripUpdatesByBlockDescriptor = new FactoryMap<BlockDescriptor, List<TripUpdate>>(
        new ArrayList<TripUpdate>());

    int totalTrips = 0;
    int unknownTrips = 0;

    for (FeedEntity entity : tripUpdates.getEntityList()) {
      TripUpdate tripUpdate = entity.getTripUpdate();
      if (tripUpdate == null) {
        _log.warn("expected a FeedEntity with a TripUpdate");
        continue;
      }
      TripDescriptor trip = tripUpdate.getTrip();
      long time = tripUpdate.hasTimestamp() ? tripUpdate.getTimestamp() * 1000 : currentTime();
      BlockDescriptor blockDescriptor = getTripDescriptorAsBlockDescriptor(result,
          trip, time);
      totalTrips++;
      if (blockDescriptor == null) {
        unknownTrips++;
        continue;
      }

      if (!hasDelayValue(tripUpdate)) {
        continue;
      }

      tripUpdatesByBlockDescriptor.get(blockDescriptor).add(tripUpdate);
    }

    if (unknownTrips > 0) {
      _log.warn("unknown/total trips= {}/{}", unknownTrips, totalTrips);
    }

    return tripUpdatesByBlockDescriptor;
  }

  private boolean hasDelayValue(TripUpdate tripUpdate) {

    if (tripUpdate.hasExtension(GtfsRealtimeOneBusAway.obaTripUpdate)) {
      OneBusAwayTripUpdate obaTripUpdate = tripUpdate.getExtension(GtfsRealtimeOneBusAway.obaTripUpdate);
      if (obaTripUpdate.hasDelay()) {
        return true;
      }
    }

    if (tripUpdate.getStopTimeUpdateCount() == 0)
      return false;

    StopTimeUpdate stopTimeUpdate = tripUpdate.getStopTimeUpdate(0);
    if (!(stopTimeUpdate.hasArrival() || stopTimeUpdate.hasDeparture()))
      return false;

    boolean hasDelay = false;
    if (stopTimeUpdate.hasDeparture()) {
      StopTimeEvent departure = stopTimeUpdate.getDeparture();
      hasDelay |= departure.hasDelay();
      hasDelay |= departure.hasTime();
    }
    if (stopTimeUpdate.hasArrival()) {
      StopTimeEvent arrival = stopTimeUpdate.getArrival();
      hasDelay |= arrival.hasDelay();
      hasDelay |= arrival.hasTime();
    }
    return hasDelay;
  }

  private Map<BlockDescriptor, FeedEntity> getVehiclePositionsByBlockDescriptor(MonitoredResult result,
      FeedMessage vehiclePositions, boolean includeVehicleIds) {

    Map<BlockDescriptor, FeedEntity> vehiclePositionsByBlockDescriptor = new HashMap<BlockDescriptor, FeedEntity>();

    for (FeedEntity entity : vehiclePositions.getEntityList()) {
      VehiclePosition vehiclePosition = entity.getVehicle();
      if (vehiclePosition == null) {
        _log.warn("expected a FeedEntity with a VehiclePosition");
        continue;
      }
      if (!(vehiclePosition.hasTrip() || vehiclePosition.hasPosition())) {
        continue;
      }
      TripDescriptor trip = vehiclePosition.getTrip();
      long time = vehiclePosition.hasTimestamp() ? vehiclePosition.getTimestamp() * 1000 : currentTime();
      BlockDescriptor blockDescriptor = getTripDescriptorAsBlockDescriptor(result, 
          trip, time);
      if (blockDescriptor != null) {
        FeedEntity existing = vehiclePositionsByBlockDescriptor.put(
            blockDescriptor, entity);
        if (existing != null) {
          _log.warn("multiple updates found for trip: " + trip);
        }
      }
    }

    return vehiclePositionsByBlockDescriptor;
  }

  private BlockDescriptor getTripDescriptorAsBlockDescriptor(MonitoredResult result,
      TripDescriptor trip, long currentTime) {
    if (!trip.hasTripId()) {
      return null;
    }
    TripEntry tripEntry = _entitySource.getTrip(trip.getTripId());
    if (tripEntry == null) {
      if (result != null) {
        _log.debug("reporting unmatched trip with id=" + trip.getTripId());
        result.addUnmatchedTripId(trip.getTripId());
      } else {
        _log.warn("no trip found with id=" + trip.getTripId());
      }
      
      return null;
    }

    ServiceDate serviceDate = null;
    BlockInstance instance;

    BlockEntry block = tripEntry.getBlock();

    if (trip.hasStartDate() && !"0".equals(trip.getStartDate())) {
      try {
        serviceDate = ServiceDate.parseString(trip.getStartDate());
      } catch (ParseException ex) {
        _log.warn("Could not parse service date " + trip.getStartDate(), ex);
      }
    }

    if (serviceDate != null) {
      instance = _blockCalendarService.getBlockInstance(block.getId(),
              serviceDate.getAsDate().getTime());
      if (instance == null) {
        _log.warn("block " + block.getId() + " does not exist on service date "
                + serviceDate);
        return null;
      }
    } else {
      long timeFrom = currentTime - 30 * 60 * 1000;
      long timeTo = currentTime + 30 * 60 * 1000;

      List<BlockInstance> instances = _blockCalendarService.getActiveBlocks(
              block.getId(), timeFrom, timeTo);

      if (instances.isEmpty()) {
        instances = _blockCalendarService.getClosestActiveBlocks(block.getId(),
                currentTime);
      }

      if (instances.isEmpty()) {
        _log.warn("could not find any active instances for the specified block="
                + block.getId() + " trip=" + trip);
        return null;
      }
      instance = instances.get(0);
    }

    if (serviceDate == null) {
      serviceDate = new ServiceDate(new Date(instance.getServiceDate()));
    }

    BlockDescriptor blockDescriptor = new BlockDescriptor();
    blockDescriptor.setBlockInstance(instance);
    blockDescriptor.setStartDate(serviceDate);
    if (trip.hasScheduleRelationship()) {
      blockDescriptor.setScheduleRelationshipValue(trip.getScheduleRelationship().toString());
    }
    int tripStartTime = 0;
    int blockStartTime = 0;
    if (trip.hasStartTime() && !"0".equals(trip.getStartTime())) {
      try {
        tripStartTime = StopTimeFieldMappingFactory.getStringAsSeconds(trip.getStartTime());
      } catch (InvalidStopTimeException iste) {
        _log.error("invalid stopTime of " + trip.getStartTime() + " for trip " + trip);
      }
      blockStartTime = getBlockStartTimeForTripStartTime(instance,
              tripEntry.getId(), tripStartTime);

      blockDescriptor.setStartTime(blockStartTime);
    }
    return blockDescriptor;
  }

  private int getBlockStartTimeForTripStartTime(BlockInstance instance,
                                                AgencyAndId tripId, int tripStartTime) {
    BlockConfigurationEntry block = instance.getBlock();

    Map<AgencyAndId, BlockTripEntry> blockTripsById = MappingLibrary.mapToValue(
            block.getTrips(), "trip.id");

    int rawBlockStartTime = block.getDepartureTimeForIndex(0);

    int rawTripStartTime = blockTripsById.get(tripId).getDepartureTimeForIndex(
            0);

    int adjustedBlockStartTime = rawBlockStartTime
            + (tripStartTime - rawTripStartTime);

    return adjustedBlockStartTime;
  }

  private void applyTripUpdatesToRecord(MonitoredResult result, BlockDescriptor blockDescriptor,
      List<TripUpdate> tripUpdates, VehicleLocationRecord record, String vehicleId) {


    BlockInstance instance = blockDescriptor.getBlockInstance();

    BlockConfigurationEntry blockConfiguration = instance.getBlock();
    List<BlockTripEntry> blockTrips = blockConfiguration.getTrips();

    Map<String, List<TripUpdate>> tripUpdatesByTripId = MappingLibrary.mapToValueList(
        tripUpdates, "trip.tripId");

    long t = currentTime();
    int currentTime = (int) ((t - instance.getServiceDate()) / 1000);
    BestScheduleDeviation best = new BestScheduleDeviation();
    List<TimepointPredictionRecord> timepointPredictions = new ArrayList<TimepointPredictionRecord>();

    for (BlockTripEntry blockTrip : blockTrips) {
      TripEntry trip = blockTrip.getTrip();
      AgencyAndId tripId = trip.getId();
      List<TripUpdate> updatesForTrip = findUpdatesForTrip(tripUpdatesByTripId, tripId);
      boolean tripUpdateHasDelay = false;

      if (updatesForTrip != null) {
        for (TripUpdate tripUpdate : updatesForTrip) {

          if (tripUpdate.hasExtension(GtfsRealtimeOneBusAway.obaTripUpdate)) {
            OneBusAwayTripUpdate obaTripUpdate = tripUpdate.getExtension(GtfsRealtimeOneBusAway.obaTripUpdate);
            if (obaTripUpdate.hasDelay()) {
              /**
               * TODO: Improved logic around picking the "best" schedule deviation
               */
              int delay = obaTripUpdate.getDelay();
              best.delta = 0;
              best.isInPast = false;
              best.scheduleDeviation = delay;
              tripUpdateHasDelay = true;
            }

            if (obaTripUpdate.hasTimestamp()) {
              best.timestamp = obaTripUpdate.getTimestamp() * 1000;
            }
          }


          if (tripUpdate.hasDelay()) {
            /**
             * TODO: Improved logic around picking the "best" schedule deviation
             */
            best.delta = 0;
            best.isInPast = false;
            best.scheduleDeviation = tripUpdate.getDelay();
            tripUpdateHasDelay = true;
          }
          if (tripUpdate.hasTimestamp()) {
            best.timestamp = tripUpdate.getTimestamp() * 1000;
          }
          for (StopTimeUpdate stopTimeUpdate : tripUpdate.getStopTimeUpdateList()) {
            BlockStopTimeEntry blockStopTime = getBlockStopTimeForStopTimeUpdate(
                    tripUpdate, stopTimeUpdate, blockTrip.getStopTimes(),
                    instance.getServiceDate());
            if (blockStopTime == null) {
              _log.warn("missing blockStopTime for stopTimeUpdate=" + stopTimeUpdate);
              continue;
            }

            StopTimeEntry stopTime = blockStopTime.getStopTime();

            TimepointPredictionRecord tpr = new TimepointPredictionRecord();
            tpr.setTimepointId(new AgencyAndId(stopTime.getStop().getId().getAgencyId(), idOnly(stopTime.getStop().getId().getId())));
            tpr.setTimepointScheduledTime(instance.getServiceDate() + stopTime.getArrivalTime() * 1000);

            int currentArrivalTime = computeArrivalTime(stopTime,
                    stopTimeUpdate, instance.getServiceDate());
            if (currentArrivalTime >= 0) {
              updateBestScheduleDeviation(currentTime,
                      stopTime.getArrivalTime(), currentArrivalTime, best);
              long timepointPredictedTime = instance.getServiceDate() + (currentArrivalTime * 1000L);
              tpr.setTimepointPredictedTime(timepointPredictedTime);
            }
            int currentDepartureTime = computeDepartureTime(stopTime,
                    stopTimeUpdate, instance.getServiceDate());
            if (currentDepartureTime >= 0) {
              updateBestScheduleDeviation(currentTime,
                      stopTime.getDepartureTime(), currentDepartureTime, best);
              long timepointPredictedTime = instance.getServiceDate() + (currentDepartureTime * 1000L);
              tpr.setTimepointPredictedTime(timepointPredictedTime);
            }
            if (tpr.getTimepointPredictedTime() != -1) {
              timepointPredictions.add(tpr);
            }
          }
        }
      }

      // If we have a TripUpdate delay and timepoint predictions, interpolate
      // timepoint predictions for close, unserved stops. See GtfsRealtimeTripLibraryTest
      // for full explanation
      // tripUpdateHasDelay = true => best.scheduleDeviation is TripUpdate delay
      if (timepointPredictions.size() > 0 && tripUpdateHasDelay) {
        Set<AgencyAndId> records = new HashSet<AgencyAndId>();
        for (TimepointPredictionRecord tpr : timepointPredictions) {
          records.add(tpr.getTimepointId());
        }
        long tprStartTime = getEarliestTimeInRecords(timepointPredictions);
        for (StopTimeEntry stopTime : trip.getStopTimes()) {
          if (records.contains(stopTime.getStop().getId())) {
            continue;
          }
          long predictionOffset = instance.getServiceDate() + (best.scheduleDeviation * 1000L);
          long predictedDepartureTime = (stopTime.getDepartureTime() * 1000L) + predictionOffset;
          long predictedArrivalTime = (stopTime.getArrivalTime() * 1000L) + predictionOffset;
          long time = best.timestamp != 0 ? best.timestamp : currentTime();
          if (predictedDepartureTime > time && predictedDepartureTime < tprStartTime) {
            TimepointPredictionRecord tpr = new TimepointPredictionRecord();
            tpr.setTimepointId(new AgencyAndId(stopTime.getStop().getId().getAgencyId(), idOnly(stopTime.getStop().getId().getId())));
            // TODO refactor NYC to support unified
//            tpr.setTripId(stopTime.getTrip().getId());
//            tpr.setStopSequence(stopTime.getGtfsSequence());
//            tpr.setTimepointPredictedArrivalTime(predictedArrivalTime);
//            tpr.setTimepointPredictedDepartureTime(predictedDepartureTime);
            tpr.setTimepointPredictedTime(predictedArrivalTime);
            tpr.setTimepointScheduledTime(predictionOffset);
            timepointPredictions.add(tpr);
          }
        }
      }
    }
    record.setServiceDate(instance.getServiceDate());
    record.setScheduleDeviation(best.scheduleDeviation);
    if (best.timestamp != 0) {
      record.setTimeOfRecord(best.timestamp);
    }
    if (timepointPredictions.isEmpty()) {
      _log.info("no tps for vehicle=" + vehicleId);
    }
    record.setTimepointPredictions(timepointPredictions);
  }


  private List<TripUpdate> findUpdatesForTrip(Map<String, List<TripUpdate>> tripUpdatesByTripId, AgencyAndId tripId) {
    if (_stripAgencyPrefix) {
      for (String s : _agencyIds) {
        List<TripUpdate> updates = tripUpdatesByTripId.get(s + "_" + tripId.getId());
        if (updates != null) {
          return updates;
        }
      }
      return null;
    }
    return tripUpdatesByTripId.get(tripId.getId());
  }

  private BlockStopTimeEntry getBlockStopTimeForStopTimeUpdate(
      TripUpdate tripUpdate, StopTimeUpdate stopTimeUpdate,
      List<BlockStopTimeEntry> stopTimes, long serviceDate) {

    if (stopTimeUpdate.hasStopSequence()) {
      int stopSequence = stopTimeUpdate.getStopSequence();
      if (0 <= stopSequence && stopSequence < stopTimes.size()) {
        BlockStopTimeEntry blockStopTime = stopTimes.get(stopSequence);
        if (!stopTimeUpdate.hasStopId()) {
          return blockStopTime;
        }
        if (blockStopTime.getStopTime().getStop().getId().getId().equals(
            idOnly(stopTimeUpdate.getStopId()))) {
          return blockStopTime;
        }
        // The stop sequence and stop id didn't match, so we fall through to
        // match by stop id if possible

      } else {
        _log.warn("StopTimeSequence is out of bounds: stopSequence="
            + stopSequence + " tripUpdate=\n" + tripUpdate);
      }
    }

    if (stopTimeUpdate.hasStopId()) {
      int time = getTimeForStopTimeUpdate(stopTimeUpdate, serviceDate);
      String stopId = idOnly(stopTimeUpdate.getStopId());
      // There could be loops, meaning a stop could appear multiple times along
      // a trip. To get around this.
      Min<BlockStopTimeEntry> bestMatches = new Min<BlockStopTimeEntry>();
      for (BlockStopTimeEntry blockStopTime : stopTimes) {
        if (blockStopTime.getStopTime().getStop().getId().getId().equals(stopId)) {
          StopTimeEntry stopTime = blockStopTime.getStopTime();
          int departureDelta = Math.abs(stopTime.getDepartureTime() - time);
          int arrivalDelta = Math.abs(stopTime.getArrivalTime() - time);
          bestMatches.add(departureDelta, blockStopTime);
          bestMatches.add(arrivalDelta, blockStopTime);
        }
      }
      if (!bestMatches.isEmpty())
        return bestMatches.getMinElement();
    }

    return null;
  }


  private String idOnly(String s) {
    if (s == null || !_stripAgencyPrefix) return s;
    for (String t : _agencyIds) {
      s = s.replace(t+"_", "");
    }
    return s;
  }

  private int getTimeForStopTimeUpdate(StopTimeUpdate stopTimeUpdate,
      long serviceDate) {
    long t = currentTime();
    if (stopTimeUpdate.hasArrival()) {
      StopTimeEvent arrival = stopTimeUpdate.getArrival();
      if (arrival.hasTime()) {
        return (int) (arrival.getTime() - serviceDate / 1000);
      }
      if (arrival.hasDelay()) {
        return (int) ((t - serviceDate) / 1000 - arrival.getDelay());
      }
    }
    if (stopTimeUpdate.hasDeparture()) {
      StopTimeEvent departure = stopTimeUpdate.getDeparture();
      if (departure.hasTime()) {
        return (int) (departure.getTime() - serviceDate / 1000);
      }
      if (departure.hasDelay()) {
        return (int) ((t - serviceDate) / 1000 - departure.getDelay());
      }
    }
    throw new IllegalStateException(
        "expected at least an arrival or departure time or delay for update: "
            + stopTimeUpdate);
  }

  private int computeArrivalTime(StopTimeEntry stopTime,
      StopTimeUpdate stopTimeUpdate, long serviceDate) {
    if (!stopTimeUpdate.hasArrival())
      return -1;
    StopTimeEvent arrival = stopTimeUpdate.getArrival();
    if (arrival.hasDelay())
      return stopTime.getArrivalTime() + arrival.getDelay();
    if (arrival.hasTime())
      return (int) (arrival.getTime() - serviceDate / 1000);
    throw new IllegalStateException(
        "expected arrival delay or time for stopTimeUpdate " + stopTimeUpdate);
  }

  private int computeDepartureTime(StopTimeEntry stopTime,
      StopTimeUpdate stopTimeUpdate, long serviceDate) {
    if (!stopTimeUpdate.hasDeparture())
      return -1;
    StopTimeEvent departure = stopTimeUpdate.getDeparture();
    if (departure.hasDelay())
      return stopTime.getDepartureTime() + departure.getDelay();
    if (departure.hasTime())
      return (int) (departure.getTime() - serviceDate / 1000);
    throw new IllegalStateException(
        "expected departure delay or time for stopTimeUpdate " + stopTimeUpdate);
  }

  private void updateBestScheduleDeviation(int currentTime,
      int expectedStopTime, int actualStopTime, BestScheduleDeviation best) {

    int delta = Math.abs(currentTime - actualStopTime);
    boolean isInPast = currentTime > actualStopTime;
    int scheduleDeviation = actualStopTime - expectedStopTime;

    if (delta < best.delta || (!isInPast && best.isInPast)) {
      best.delta = delta;
      best.isInPast = isInPast;
      best.scheduleDeviation = scheduleDeviation;
    }
  }

  private void applyVehiclePositionToRecord(VehiclePosition vehiclePosition,
      VehicleLocationRecord record) {
    Position position = vehiclePosition.getPosition();
    record.setCurrentLocationLat(position.getLatitude());
    record.setCurrentLocationLon(position.getLongitude());
  }



  private static long getEarliestTimeInRecords(Collection<TimepointPredictionRecord> records) {
    long min = Long.MAX_VALUE;
    for (TimepointPredictionRecord tpr : records) {
      if (tpr.getTimepointPredictedTime() != -1) {
        min = Math.min(min, tpr.getTimepointPredictedTime());
      }
    }
    return min;
  }

  private long currentTime() {
    if (_currentTime != 0)
      return _currentTime;
    return System.currentTimeMillis();
  }

  private static class BestScheduleDeviation {
    public int delta = Integer.MAX_VALUE;
    public int scheduleDeviation = 0;
    public boolean isInPast = true;
    public long timestamp = 0;
  }
}
