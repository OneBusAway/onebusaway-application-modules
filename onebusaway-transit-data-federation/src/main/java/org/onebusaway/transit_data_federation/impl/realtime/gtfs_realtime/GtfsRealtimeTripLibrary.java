/**
 * Copyright (C) 2014 Kurt Raschke <kurt@kurtraschke.com>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.collections.Min;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.mappings.InvalidStopTimeException;
import org.onebusaway.gtfs.serialization.mappings.StopTimeFieldMappingFactory;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;
import com.google.transit.realtime.GtfsRealtimeOneBusAway;
import com.google.transit.realtime.GtfsRealtimeOneBusAway.OneBusAwayTripUpdate;
import org.onebusaway.realtime.api.TimepointPredictionRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class GtfsRealtimeTripLibrary {

  private static final Logger _log = LoggerFactory.getLogger(GtfsRealtimeTripLibrary.class);

  private GtfsRealtimeEntitySource _entitySource;

  private BlockCalendarService _blockCalendarService;

  /**
   * This is primarily here to assist with unit testing.
   */
  private long _currentTime = 0;

  private StopModificationStrategy _stopModificationStrategy = null;

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
  
  public void setStopModificationStrategy(StopModificationStrategy strategy) {
    _stopModificationStrategy = strategy;
  }

  /**
   * Trip updates describe a trip which is undertaken by a vehicle (which is
   * itself described in vehicle positions), but GTFS-realtime does not demand
   * that the two messages be related to each other. Where trip updates and
   * vehicle positions both contain a vehicle ID, we use those vehicle IDs to
   * join the messages together.
   *
   * Otherwise, where vehicle IDs are not provided, we join trip updates and
   * vehicle positions based on trip descriptors. If multiple trip updates
   * are provided for a block, they are all used, but cannot be mapped to
   * vehicle positions.
   *
   * @param tripUpdates
   * @param vehiclePositions
   * @return
   */
  public List<CombinedTripUpdatesAndVehiclePosition> groupTripUpdatesAndVehiclePositions(
      FeedMessage tripUpdateMessage, FeedMessage vehiclePositionsMessage) {
    return groupTripUpdatesAndVehiclePositions(null, tripUpdateMessage, vehiclePositionsMessage);
  }
  
  public List<CombinedTripUpdatesAndVehiclePosition> groupTripUpdatesAndVehiclePositions(MonitoredResult result,
      FeedMessage tripUpdateMessage, FeedMessage vehiclePositionsMessage) {

    List<CombinedTripUpdatesAndVehiclePosition> updates = new ArrayList<CombinedTripUpdatesAndVehiclePosition>();
    Map<String, TripUpdate> tripUpdatesByVehicleId = new HashMap<String, TripUpdate>();
    Map<String, VehiclePosition> vehiclePositionsByVehicleId = new HashMap<String, VehiclePosition>();

    ListMultimap<BlockDescriptor, TripUpdate> anonymousTripUpdatesByBlock = ArrayListMultimap.<BlockDescriptor, TripUpdate> create();
    Map<BlockDescriptor, VehiclePosition> anonymousVehiclePositionsByBlock = new HashMap<BlockDescriptor, VehiclePosition>();

    Set<BlockDescriptor> badAnonymousVehiclePositions = new HashSet<BlockDescriptor>();

    for (FeedEntity fe : tripUpdateMessage.getEntityList()) {
      if (!fe.hasTripUpdate()) {
        continue;
      }

      TripUpdate tu = fe.getTripUpdate();

      if (tu.hasVehicle() && tu.getVehicle().hasId()) {
        // Trip update has a vehicle ID - index by vehicle ID
        String vehicleId = tu.getVehicle().getId();

        if (!tripUpdatesByVehicleId.containsKey(vehicleId)) {
          tripUpdatesByVehicleId.put(vehicleId, tu);
        } else {
          // upcoming merge will fix this
          _log.debug("Multiple TripUpdates for vehicle {}; taking newest.",
              vehicleId);

          TripUpdate otherUpdate = tripUpdatesByVehicleId.get(vehicleId);

          long otherTimestamp = otherUpdate.getTimestamp();

          if (tu.getTimestamp() > otherTimestamp) {
            tripUpdatesByVehicleId.put(vehicleId, tu);
          }

        }
      } else {
        /*
         * Trip update does not have a vehicle ID - index by TripDescriptor
         * (includes start date and time).
         */
        TripDescriptor td = tu.getTrip();
        BlockDescriptor bd = getTripDescriptorAsBlockDescriptor(result, td);

        if (bd == null) {
          continue;
        }

        if (!anonymousTripUpdatesByBlock.containsKey(bd)) {
          anonymousTripUpdatesByBlock.put(bd, tu);
        } else {
          _log.warn(
              "Multiple anonymous TripUpdates for trip {}; will not map to VehiclePosition.",
              td.getTripId());
          anonymousTripUpdatesByBlock.put(bd, tu);
        }
      }

    }

    for (FeedEntity fe : vehiclePositionsMessage.getEntityList()) {
      if (!fe.hasVehicle()) {
        continue;
      }

      VehiclePosition vp = fe.getVehicle();

      if (vp.hasVehicle() && vp.getVehicle().hasId()) {
        // Vehicle position has a vehicle ID - index by vehicle ID
        String vehicleId = vp.getVehicle().getId();

        if (!vehiclePositionsByVehicleId.containsKey(vehicleId)) {
          vehiclePositionsByVehicleId.put(vehicleId, vp);
        } else {
          _log.warn("Multiple updates for vehicle {}; taking newest.",
              vehicleId);

          VehiclePosition otherUpdate = vehiclePositionsByVehicleId.get(vehicleId);

          long otherTimestamp = otherUpdate.getTimestamp();

          if (vp.getTimestamp() > otherTimestamp) {
            vehiclePositionsByVehicleId.put(vehicleId, vp);
          }

        }
      } else if (vp.hasTrip()) {
        /*
         * Vehicle position does not have vehicle ID but has TripDescriptor, so
         * use that, but only if there is only one.
         */

        TripDescriptor td = vp.getTrip();
        BlockDescriptor bd = getTripDescriptorAsBlockDescriptor(result, td);

        if (bd == null) {
          continue;
        }

        if (!anonymousVehiclePositionsByBlock.containsKey(bd)) {
          anonymousVehiclePositionsByBlock.put(bd, vp);
        } else {
          /*
           * When we have multiple VehiclePositions for a block but no way to
           * uniquely distinguish them there is nothing useful or reasonable we
           * can do with the data.
           */
          _log.warn(
              "Multiple anonymous VehiclePositions for trip {}; giving up.",
              td.getTripId());
          badAnonymousVehiclePositions.add(bd);
        }
      } else {
        /*
         * Pathological VehiclePosition contains no identifying information;
         * skip.
         */
        continue;
      }
    }

    // Remove multiple vehicles where multiple anonymous vehicles are present in
    // a block
    for (BlockDescriptor bd : badAnonymousVehiclePositions) {
      anonymousVehiclePositionsByBlock.remove(bd);
    }

    // Map updates by vehicle ID
    for (Map.Entry<String, TripUpdate> e : tripUpdatesByVehicleId.entrySet()) {
      CombinedTripUpdatesAndVehiclePosition update = new CombinedTripUpdatesAndVehiclePosition();

      String vehicleId = e.getKey();
      TripUpdate tu = e.getValue();
      update.block = getTripDescriptorAsBlockDescriptor(result, tu.getTrip());
      update.tripUpdates = Collections.singletonList(tu);

      if (vehiclePositionsByVehicleId.containsKey(vehicleId)) {
        update.vehiclePosition = vehiclePositionsByVehicleId.get(vehicleId);
      }

      updates.add(update);
    }

    // Map anonymous updates by block descriptor
    for (Entry<BlockDescriptor, Collection<TripUpdate>> e : anonymousTripUpdatesByBlock.asMap().entrySet()) {
      CombinedTripUpdatesAndVehiclePosition update = new CombinedTripUpdatesAndVehiclePosition();

      BlockDescriptor bd = e.getKey();
      update.block = bd;
      update.tripUpdates = new ArrayList<TripUpdate>(e.getValue());

      if (update.tripUpdates.size() == 1
          && anonymousVehiclePositionsByBlock.containsKey(bd)) {
        update.vehiclePosition = anonymousVehiclePositionsByBlock.get(bd);
      }

      updates.add(update);
    }

    // Set vehicle ID in block if possible
    for (CombinedTripUpdatesAndVehiclePosition update : updates) {
      String vehicleId = null;

      for (TripUpdate tu : update.tripUpdates) {
        if (tu.hasVehicle() && tu.getVehicle().hasId()) {
          vehicleId = tu.getVehicle().getId();
          break;
        }
      }

      if (vehicleId == null && update.vehiclePosition != null
          && update.vehiclePosition.hasVehicle()
          && update.vehiclePosition.getVehicle().hasId()) {
        vehicleId = update.vehiclePosition.getVehicle().getId();
      }

      if (vehicleId != null && update.block != null) {
        update.block.setVehicleId(vehicleId);
      }
    }

    return updates;
  }

  /**
   * The {@link VehicleLocationRecord} is guaranteed to have a
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
    if (update.block == null) return null;

    record.setBlockId(blockDescriptor.getBlockInstance().getBlock().getBlock().getId());

    applyTripUpdatesToRecord(result, blockDescriptor, update.tripUpdates, record);

    if (update.vehiclePosition != null) {
      applyVehiclePositionToRecord(result, update.vehiclePosition, record);
    }

    /**
     * By default, we use the block id as the vehicle id
     */
    record.setVehicleId(record.getBlockId());

    if (result != null) {
      if (record.getTripId() != null) {
        result.addMatchedTripId(record.getTripId().toString());
      } else if (record.getBlockId() != null) {
        // here we take a matched block as if it were a trip
        result.addMatchedTripId(record.getBlockId().toString());
      } else {
        // we don't have a tripId, use the BlockId instead
        result.addMatchedTripId(record.getBlockId().toString());
      }
    }
    
    if (blockDescriptor.getVehicleId() != null) {
      String agencyId = record.getBlockId().getAgencyId();
      record.setVehicleId(new AgencyAndId(agencyId,
          blockDescriptor.getVehicleId()));
    }

    return record;
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

  private boolean hasDelayValue(TripUpdate tripUpdate) {

    if (tripUpdate.hasDelay()) {
      return true;
    }

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


  private BlockDescriptor getTripDescriptorAsBlockDescriptor(MonitoredResult result,
      TripDescriptor trip) {
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
    if (trip.hasStartDate() && ! "0".equals(trip.getStartDate())) {
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
    	long t = currentTime();
    	long timeFrom = t - 30 * 60 * 1000;
    	long timeTo = t + 30 * 60 * 1000;
    	
    	List<BlockInstance> instances = _blockCalendarService.getActiveBlocks(
    			block.getId(), timeFrom, timeTo);
    	
    	if (instances.isEmpty()) {
    		instances = _blockCalendarService.getClosestActiveBlocks(block.getId(), 
    				t);
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

  
  private void applyTripUpdatesToRecord(MonitoredResult result, BlockDescriptor blockDescriptor,
      List<TripUpdate> tripUpdates, VehicleLocationRecord record) {

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
      List<TripUpdate> updatesForTrip = tripUpdatesByTripId.get(tripId.getId());
      
      if (updatesForTrip != null) {
        for (TripUpdate tripUpdate : updatesForTrip) {

          /**
           * TODO: delete this code once all upstream systems have been
           * migrated the new "delay" and "timestamp" fields.
           */
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
          }
          if (tripUpdate.hasTimestamp()) {
            best.timestamp = tripUpdate.getTimestamp() * 1000;
          }

          for (StopTimeUpdate stopTimeUpdate : tripUpdate.getStopTimeUpdateList()) {
            BlockStopTimeEntry blockStopTime = getBlockStopTimeForStopTimeUpdate(result,
                tripUpdate, stopTimeUpdate, blockTrip.getStopTimes(),
                instance.getServiceDate());
            if (blockStopTime == null)
              continue;

            StopTimeEntry stopTime = blockStopTime.getStopTime();

            TimepointPredictionRecord tpr = new TimepointPredictionRecord();
            tpr.setTimepointId(stopTime.getStop().getId());
            tpr.setTripId(stopTime.getTrip().getId());
            if (stopTimeUpdate.hasStopSequence()) {
              tpr.setStopSequence(stopTimeUpdate.getStopSequence());
            }

            int currentArrivalTime = computeArrivalTime(stopTime,
                stopTimeUpdate, instance.getServiceDate());
            int currentDepartureTime = computeDepartureTime(stopTime,
                stopTimeUpdate, instance.getServiceDate());

            if (currentArrivalTime >= 0) {
              updateBestScheduleDeviation(currentTime,
                  stopTime.getArrivalTime(), currentArrivalTime, best);

              long timepointPredictedTime = instance.getServiceDate() + (currentArrivalTime * 1000L);
              tpr.setTimepointPredictedArrivalTime(timepointPredictedTime);
            } 

            if (currentDepartureTime >= 0) {
              updateBestScheduleDeviation(currentTime,
                  stopTime.getDepartureTime(), currentDepartureTime, best);

              long timepointPredictedTime = instance.getServiceDate() + (currentDepartureTime * 1000L);
              tpr.setTimepointPredictedDepartureTime(timepointPredictedTime);
            }

            if (tpr.getTimepointPredictedArrivalTime() != -1 || 
                tpr.getTimepointPredictedDepartureTime() != -1) {
              timepointPredictions.add(tpr);
            }
          }
        }
      }
    }

    record.setServiceDate(instance.getServiceDate());
    if (blockDescriptor.getStartTime() != null) {
      record.setBlockStartTime(blockDescriptor.getStartTime());
    }
    record.setScheduleDeviation(best.scheduleDeviation);
    if (best.timestamp != 0) {
      record.setTimeOfRecord(best.timestamp);
    }
    record.setTimepointPredictions(timepointPredictions);
  }

  private BlockStopTimeEntry getBlockStopTimeForStopTimeUpdate(MonitoredResult result,
      TripUpdate tripUpdate, StopTimeUpdate stopTimeUpdate,
      List<BlockStopTimeEntry> stopTimes, long serviceDate) {

    if (stopTimeUpdate.hasStopSequence()) {
      int stopSequence = stopTimeUpdate.getStopSequence();

      Map<Integer, BlockStopTimeEntry> sequenceToStopTime = MappingLibrary.mapToValue(stopTimes, "stopTime.gtfsSequence");

      if (sequenceToStopTime.containsKey(stopSequence)) {
        BlockStopTimeEntry blockStopTime = sequenceToStopTime.get(stopSequence);
        if (!stopTimeUpdate.hasStopId()) {
          if (result != null) {
            result.addMatchedStopId(blockStopTime.getStopTime().getStop().getId().getId());
          }
          return blockStopTime;
        }
        String stopTimeUpdateStopId = convertStopId(stopTimeUpdate.getStopId());
        if (blockStopTime.getStopTime().getStop().getId().getId().equals(
            stopTimeUpdateStopId)) {
          if (result != null) {
            result.addMatchedStopId(blockStopTime.getStopTime().getStop().getId().getId());
          }
          return blockStopTime;
        }
        // The stop sequence and stop id didn't match, so we fall through to
        // match by stop id if possible
        // we do not log this as it still may match later

      } else {
        _log.debug("StopTimeSequence is out of bounds: stopSequence="
            + stopSequence + " tripUpdate=\n" + tripUpdate);
        // sadly we can't report an invalid stop sequence -- we need a stopId
      }
    }

    if (stopTimeUpdate.hasStopId()) {
      int time = getTimeForStopTimeUpdate(stopTimeUpdate, serviceDate);
      String stopId = convertStopId(stopTimeUpdate.getStopId());
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
      if (!bestMatches.isEmpty()) {
        if (result != null) {
          result.addMatchedStopId(convertStopId(stopId));
        }
        return bestMatches.getMinElement();
      }
    }
    if (result != null) {
      // if we are here, the stop did not fall on that block
      result.addUnmatchedStopId(convertStopId(stopTimeUpdate.getStopId()));
    }
    return null;
  }

  private String convertStopId(String stopId) {
    if (this._stopModificationStrategy == null) {
      return stopId;
    }
    return _stopModificationStrategy.convertStopId(stopId);
  }

  private int getTimeForStopTimeUpdate(StopTimeUpdate stopTimeUpdate,
      long serviceDate) {
    long t = currentTime();
    if (stopTimeUpdate.hasArrival()) {
      StopTimeEvent arrival = stopTimeUpdate.getArrival();
      // note that we prefer time over delay if both are present
      if (arrival.hasTime()) {
          return (int) (arrival.getTime() - serviceDate / 1000);
      }
      if (arrival.hasDelay()) {
        return (int) ((t - serviceDate) / 1000 - arrival.getDelay());
      }
    }
    if (stopTimeUpdate.hasDeparture()) {
      StopTimeEvent departure = stopTimeUpdate.getDeparture();
      // again we prefer time over delay if both are present
      if (departure.hasTime())
          return (int) (departure.getTime() - serviceDate / 1000);

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

  private void applyVehiclePositionToRecord(MonitoredResult result,
      VehiclePosition vehiclePosition,
      VehicleLocationRecord record) {
    Position position = vehiclePosition.getPosition();
    record.setCurrentLocationLat(position.getLatitude());
    record.setCurrentLocationLon(position.getLongitude());
    if (result != null) {
      result.addLatLon(position.getLatitude(), position.getLongitude());
    }
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
