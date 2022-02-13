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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.collections.Min;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.mappings.InvalidStopTimeException;
import org.onebusaway.gtfs.serialization.mappings.StopTimeFieldMappingFactory;
import org.onebusaway.realtime.api.OccupancyStatus;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.realtime.api.VehicleOccupancyRecord;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockGeospatialService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class GtfsRealtimeTripLibrary {

  private static final Logger _log = LoggerFactory.getLogger(GtfsRealtimeTripLibrary.class);

  private GtfsRealtimeEntitySource _entitySource;

  private BlockCalendarService _blockCalendarService;
   
  private BlockGeospatialService _blockGeospatialService;
  
  /**
   * This is primarily here to assist with unit testing.
   */
  private long _currentTime = 0;

  private boolean _validateCurrentTime = true;
  public void setValidateCurrentTime(boolean validate) {
    _validateCurrentTime = validate;
  }
  private boolean validateCurrentTime() {
    return _validateCurrentTime;
  }

  private StopModificationStrategy _stopModificationStrategy = null;

  private boolean _scheduleAdherenceFromLocation = false;

  private boolean _useLabelAsVehicleId = false;
  
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
    setCurrentTime(currentTime, 0);
  }
  
  public void setCurrentTime(long currentTime, int originOffsetHours) {
    if (originOffsetHours != 0) {
      Calendar c = Calendar.getInstance();
      c.setTime(new Date(currentTime));
      c.roll(Calendar.HOUR, originOffsetHours);
      _currentTime = c.getTimeInMillis();
      _log.info("currentTime set to " + new Date(_currentTime) + " from offset " + originOffsetHours);
    } else {
    _currentTime = currentTime;
    }
    
  }
  
  public void setStopModificationStrategy(StopModificationStrategy strategy) {
    _stopModificationStrategy = strategy;
  }
  
  public void setScheduleAdherenceFromLocation(boolean scheduleAdherenceFromLocation) {
    _scheduleAdherenceFromLocation = scheduleAdherenceFromLocation;
  }
  
  public void setBlockGeospatialService(BlockGeospatialService blockGeospatialService) {
    _blockGeospatialService = blockGeospatialService;
  }

  /**
   * use the vehicle label as the id.
   * @param useLabelAsVehicleId
   */
  public void setUseLabelAsVehicleId(boolean useLabelAsVehicleId) {
    _useLabelAsVehicleId = useLabelAsVehicleId;
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
   * @return
   */
  public List<CombinedTripUpdatesAndVehiclePosition> groupTripUpdatesAndVehiclePositions(
      FeedMessage tripUpdateMessage, FeedMessage vehiclePositionsMessage) {
    return groupTripUpdatesAndVehiclePositions(null, tripUpdateMessage, vehiclePositionsMessage);
  }
  
  public List<CombinedTripUpdatesAndVehiclePosition> groupTripUpdatesAndVehiclePositions(MonitoredResult result,
      FeedMessage tripUpdateMessage, FeedMessage vehiclePositionsMessage) {

    List<CombinedTripUpdatesAndVehiclePosition> updates = new ArrayList<CombinedTripUpdatesAndVehiclePosition>();
    Map<String, TripUpdate> bestTripByVehicleId = new HashMap<String, TripUpdate>();
    ListMultimap<String, TripUpdate> tripUpdatesByVehicleId = ArrayListMultimap.create();
    Map<String, VehiclePosition> vehiclePositionsByVehicleId = new HashMap<String, VehiclePosition>();
    AssignmentInfo assignmentInfo = getAssignmentInfo(tripUpdateMessage, vehiclePositionsMessage);

    ListMultimap<BlockDescriptor, TripUpdate> anonymousTripUpdatesByBlock = ArrayListMultimap.<BlockDescriptor, TripUpdate> create();
    Map<BlockDescriptor, VehiclePosition> anonymousVehiclePositionsByBlock = new HashMap<BlockDescriptor, VehiclePosition>();

    Set<BlockDescriptor> badAnonymousVehiclePositions = new HashSet<BlockDescriptor>();

    for (FeedEntity fe : tripUpdateMessage.getEntityList()) {
      if (!fe.hasTripUpdate()) {
        continue;
      }

      TripUpdate tu = fe.getTripUpdate();

      if (tu.hasVehicle() && tu.getVehicle().hasId() && StringUtils.isNotBlank(tu.getVehicle().getId())) {
        // Trip update has a vehicle ID - index by vehicle ID
        String vehicleId = getVehicleId(tu);

        tripUpdatesByVehicleId.put(vehicleId, tu);

        if (!bestTripByVehicleId.containsKey(vehicleId)) {
          bestTripByVehicleId.put(vehicleId, tu);
        } else {
          // upcoming merge will fix this
          _log.debug("Multiple TripUpdates for vehicle {}; taking best.",
              vehicleId);

            if (Boolean.TRUE.equals(tripMoreAppropriate(assignmentInfo, tu, bestTripByVehicleId.get(vehicleId), vehicleId))) {
              bestTripByVehicleId.put(vehicleId, tu);
          }
        }
      } else {
        /*
         * Trip update does not have a vehicle ID - index by TripDescriptor
         * (includes start date and time).
         */
        TripDescriptor td = tu.getTrip();
        long time = tu.hasTimestamp() ? tu.getTimestamp() * 1000 : currentTime();
        BlockDescriptor bd = getTripDescriptorAsBlockDescriptor(result, td, time);

        if (bd == null) {
          continue;
        }

        // if this block has an assigned vehicle skip this anonymous update
        // its likely for a future trip
        TripEntry tripEntry = _entitySource.getTrip(td.getTripId());
        if (tripEntry != null && tripEntry.getBlock() != null) {
          String blockId = tripEntry.getBlock().getId().toString();
          if (assignmentInfo.preferredVehicleByBlockId.containsKey(blockId)) {
            String preferredVehicleId = assignmentInfo.preferredVehicleByBlockId.get(blockId);
            if (!td.getTripId().equals(preferredVehicleId)) {
              _log.info("skipping anonymous update " + td.getTripId()
                      + " as a vehicle assignment exists for vehicle "
              + assignmentInfo.preferredVehicleByBlockId.get(blockId)
              + " for trip "
              + assignmentInfo.preferredTripByVehicleId.get(preferredVehicleId));
              continue;
            }
          }
        }

        if (!anonymousTripUpdatesByBlock.containsKey(bd)) {
          anonymousTripUpdatesByBlock.put(bd, tu);
        } else {
          _log.debug(
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
        String vehicleId = getVehicleId(vp);

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
        long time = vp.hasTimestamp() ? vp.getTimestamp() * 1000 : currentTime();
        BlockDescriptor bd = getTripDescriptorAsBlockDescriptor(result, td, time);

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
          _log.debug(
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
    for (Map.Entry<String, Collection<TripUpdate>> e : tripUpdatesByVehicleId.asMap().entrySet()) {
      CombinedTripUpdatesAndVehiclePosition update = new CombinedTripUpdatesAndVehiclePosition();

      String vehicleId = e.getKey();
      Collection<TripUpdate> tripUpdates = e.getValue();
      TripUpdate tu = bestTripByVehicleId.get(vehicleId);

      long time = tu.hasTimestamp() ? tu.getTimestamp() * 1000 : currentTime();
      update.block = getTripDescriptorAsBlockDescriptor(result, tu.getTrip(), time);
      update.tripUpdates = new ArrayList<TripUpdate>(tripUpdates);
      update.bestTrip = tu.getTrip().getTripId();

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
          vehicleId = getVehicleId(tu);
          break;
        }
      }

      if (vehicleId == null && update.vehiclePosition != null
          && update.vehiclePosition.hasVehicle()
          && update.vehiclePosition.getVehicle().hasId()) {
        vehicleId = getVehicleId(update.vehiclePosition);
      }

      if (vehicleId != null && update.block != null) {
        update.block.setVehicleId(vehicleId);
      }
    }

    return updates;
  }

  // take hints from the vehicle position feed and hold on to for later grouping
  private AssignmentInfo getAssignmentInfo(FeedMessage tripUpdateMessage, FeedMessage vehiclePositionsMessage) {
    Map<String, String> preferredTripByVehicleId = new HashMap<>();
    Map<String, String> preferredVehicleByBlockId = new HashMap<>();
    if (vehiclePositionsMessage != null) {
      for (FeedEntity fe : vehiclePositionsMessage.getEntityList()) {
        if (!fe.hasVehicle()) {
          continue;
        }

        if (fe.hasVehicle()
                && fe.getVehicle().hasVehicle()
                && fe.getVehicle().getVehicle().hasId()
                && fe.getVehicle().hasTrip()) {

          String vehicleId = fe.getVehicle().getVehicle().getId();
          String tripId = fe.getVehicle().getTrip().getTripId();
          if (preferredTripByVehicleId.containsKey(vehicleId)) {
            _log.warn("vehicle " + vehicleId
                    + " on trip " + tripId + " already reported on"
                    + preferredTripByVehicleId.get(vehicleId));
            continue;
          }
          preferredTripByVehicleId.put(vehicleId,
                  tripId);
          TripEntry tripEntry = _entitySource.getTrip(tripId);
          if (tripEntry != null) {
            if (tripEntry.getBlock() != null) {
              String blockId = tripEntry.getBlock().getId().toString();
              preferredVehicleByBlockId.put(blockId,
                      vehicleId);
            }
          }
        }
      }
    }



    return new AssignmentInfo(preferredTripByVehicleId,  preferredVehicleByBlockId);

  }

  private long getTripStartTime(String tripId) {
    TripEntry tripEntry = _entitySource.getTrip(tripId);
    long min = Long.MAX_VALUE;
    if (tripEntry == null) return min;
    for (StopTimeEntry stopTime : tripEntry.getStopTimes()) {
      if (stopTime.getArrivalTime() < min)
        min = stopTime.getArrivalTime();
    }
    return min;
  }
  
  private Boolean tripMoreAppropriate(AssignmentInfo assignmentInfo, TripUpdate newTrip, TripUpdate original, String vehicleId) {

    String preferredTripId = assignmentInfo.preferredTripByVehicleId.get(vehicleId);

    if (preferredTripId != null) {
      if (preferredTripId.equals(newTrip.getTrip().getTripId()))
        return true;
      if (preferredTripId.equals(original.getTrip().getTripId()))
        return false;
      return null;
    }

    long closestTemporalUpdateNewTrip = closestTemporalUpdate(newTrip);
    long closestTemporalUpdateOriginal = closestTemporalUpdate(original);
    
    if (closestTemporalUpdateNewTrip < closestTemporalUpdateOriginal)
      return true;
    
    return false;
  }

  private long closestTemporalUpdate(TripUpdate t) {
    long closest = Long.MAX_VALUE;
    for (StopTimeUpdate stu : t.getStopTimeUpdateList()) {
      if (stu.hasArrival()) {
        long delta = Math.abs(stu.getArrival().getTime() * 1000 - getCurrentTime());
        if (delta < closest) {
          closest = delta;
        }
      } else if (stu.hasDeparture()) {
        long delta = Math.abs(stu.getDeparture().getTime() * 1000 - getCurrentTime());
        if (delta < closest) {
          closest = delta;
        }
      }
    }
    return closest;
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
    record.setTimeOfRecord(currentTime()); // this is just the default -- if we have tripUpdates this will be re-written

    BlockDescriptor blockDescriptor = update.block;
    if (update.block == null) return null;
    String vehicleId = update.block.getVehicleId();
    record.setBlockId(blockDescriptor.getBlockInstance().getBlock().getBlock().getId());
    // this is the default, trip updates may cancel this trip
    record.setStatus(blockDescriptor.getScheduleRelationship().toString());


    applyTripUpdatesToRecord(result, blockDescriptor, update.tripUpdates, record, vehicleId, update.bestTrip);

    if (update.vehiclePosition != null) {
      applyVehiclePositionToRecord(result, blockDescriptor, update.vehiclePosition, record);
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

    if (!blockTripsById.containsKey(tripId)) {
      _log.debug("getBlockStartTimeForTripStartTime(" + instance + ", " + tripId + ", "
      + tripStartTime + ") did not find matching trip; aborting");
      return -1;
    }

    int rawTripStartTime = blockTripsById.get(tripId).getDepartureTimeForIndex(
        0);

    int adjustedBlockStartTime = rawBlockStartTime
        + (tripStartTime - rawTripStartTime);

    return adjustedBlockStartTime;
  }

  private BlockDescriptor getTripDescriptorAsBlockDescriptor(MonitoredResult result,
      TripDescriptor trip, long currentTime) {
    if (!trip.hasTripId()) {
      return null;
    }
    TripEntry tripEntry = _entitySource.getTrip(trip.getTripId());
    if (tripEntry == null) {
      if (result != null) {
        _log.debug("discarding: reporting unmatched trip with id=" + trip.getTripId());
        result.addUnmatchedTripId(trip.getTripId());
      } else {
        _log.debug("discarding: no trip found with id=" + trip.getTripId());
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
    		_log.debug("Could not parse service date " + trip.getStartDate(), ex);
    	}
    }
    
    if (serviceDate != null) {
    	instance = _blockCalendarService.getBlockInstance(block.getId(),
    			serviceDate.getAsDate().getTime());
    	if (instance == null) {
    		_log.debug("block " + block.getId() + " does not exist on service date "
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
    		_log.debug("could not find any active instances for the specified block="
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
    		_log.debug("invalid stopTime of " + trip.getStartTime() + " for trip " + trip);
    		return null;
    	}
    	blockStartTime = getBlockStartTimeForTripStartTime(instance,
    			tripEntry.getId(), tripStartTime);
    	if (blockStartTime < 0) {
          _log.debug("invalid blockStartTime for trip " + trip + " for instance=" + instance);
          return null;
        }
    	blockDescriptor.setStartTime(blockStartTime);
    }
    return blockDescriptor;
  }

  
  private void applyTripUpdatesToRecord(MonitoredResult result, BlockDescriptor blockDescriptor,
      List<TripUpdate> tripUpdates, VehicleLocationRecord record, String vehicleId, String bestTripId) {

    BlockInstance instance = blockDescriptor.getBlockInstance();

    BlockConfigurationEntry blockConfiguration = instance.getBlock();
    List<BlockTripEntry> blockTrips = blockConfiguration.getTrips();
    Map<String, List<TripUpdate>> tripUpdatesByTripId = MappingLibrary.mapToValueList(
        tripUpdates, "trip.tripId");

    long t = currentTime();
    int currentTime = (int) ((t - instance.getServiceDate()) / 1000);
    BestScheduleDeviation best = new BestScheduleDeviation();
    long lastStopScheduleTime = Long.MIN_VALUE;
    boolean singleTimepointRecord = false;

    List<TimepointPredictionRecord> timepointPredictions = new ArrayList<TimepointPredictionRecord>();

    for (BlockTripEntry blockTrip : blockTrips) {
      TripEntry trip = blockTrip.getTrip();
      AgencyAndId tripId = trip.getId();
      List<TripUpdate> updatesForTrip = tripUpdatesByTripId.get(tripId.getId());
      
      boolean tripUpdateHasDelay = false;

      // onBestTrip is only relevant if bestTripId is set, which indicates that the TripUpdates
      // came from the vehicleId map (as opposed to block index).
      boolean onBestTrip = bestTripId == null || tripId.getId().equals(bestTripId);

      if (updatesForTrip != null) {
        for (TripUpdate tripUpdate : updatesForTrip) {
          /**
           * TODO: delete this code once all upstream systems have been
           * migrated the new "delay" and "timestamp" fields.
           */
          if (tripUpdate.hasExtension(GtfsRealtimeOneBusAway.obaTripUpdate) && onBestTrip) {
            OneBusAwayTripUpdate obaTripUpdate = tripUpdate.getExtension(GtfsRealtimeOneBusAway.obaTripUpdate);
            if (obaTripUpdate.hasDelay()) {
              /**
               * TODO: Improved logic around picking the "best" schedule deviation
               */
              int delay = obaTripUpdate.getDelay();
              best.delta = 0;
              best.isInPast = false;
              best.scheduleDeviation = delay;
              best.tripId = tripId;
              tripUpdateHasDelay = true;
            }

            if (obaTripUpdate.hasTimestamp() && onBestTrip) {
              best.timestamp = obaTripUpdate.getTimestamp() * 1000;
            }
          }

          if (tripUpdate.hasDelay() && onBestTrip) {
            /**
             * TODO: Improved logic around picking the "best" schedule deviation
             */
            best.delta = 0;
            best.isInPast = false;
            best.scheduleDeviation = tripUpdate.getDelay();
            best.tripId = tripId;
            tripUpdateHasDelay = true;
          }
          if (tripUpdate.hasTimestamp() && onBestTrip) {
            best.timestamp = tripUpdate.getTimestamp() * 1000;
          }

          if (tripId != null) {
            best.isCanceled = tripUpdate.getTrip().getScheduleRelationship().equals(TripDescriptor.ScheduleRelationship.CANCELED);
            record.setStatus(tripUpdate.getTrip().getScheduleRelationship().toString());
            _log.debug("schedule=" + tripUpdate.getTrip().getScheduleRelationship() + "; isCanceled=" + best.isCanceled);
          }

          for (StopTimeUpdate stopTimeUpdate : tripUpdate.getStopTimeUpdateList()) {
            BlockStopTimeEntry blockStopTime = getBlockStopTimeForStopTimeUpdate(result,
                    tripUpdate, stopTimeUpdate, blockTrip.getStopTimes(),
                    instance.getServiceDate());

            // loop through and store last stop time on trip
            List<BlockStopTimeEntry> stopTimes = blockTrip.getStopTimes();
            for (BlockStopTimeEntry bste : stopTimes) {
              long scheduleTime = instance.getServiceDate() + bste.getStopTime().getArrivalTime() * 1000;
              if (scheduleTime > lastStopScheduleTime) {
                lastStopScheduleTime = scheduleTime;
              }
            }

            if (blockStopTime == null)
              continue;

            StopTimeEntry stopTime = blockStopTime.getStopTime();

            TimepointPredictionRecord tpr = new TimepointPredictionRecord();
            tpr.setTimepointId(stopTime.getStop().getId());
            tpr.setTripId(stopTime.getTrip().getId());
            if (!stopTimeUpdate.getScheduleRelationship().equals(StopTimeUpdate.ScheduleRelationship.SKIPPED)) {
              tpr.setTimepointScheduledTime(instance.getServiceDate() + stopTime.getArrivalTime() * 1000);
            }
            if (stopTimeUpdate.hasStopSequence()) {
              tpr.setStopSequence(stopTimeUpdate.getStopSequence());
            }
            if (stopTimeUpdate.getScheduleRelationship().equals(StopTimeUpdate.ScheduleRelationship.SKIPPED)) {
              tpr.setScheduleRealtionship(StopTimeUpdate.ScheduleRelationship.SKIPPED_VALUE); // set tpr scheduleRelationship enum to SKIPPED
              timepointPredictions.add(tpr);
              _log.debug("SKIPPED stop:" + tpr.getTimepointId() + "  seq: " + tpr.getStopSequence() + " trip: " + tpr.getTripId());
            } else {
              tpr.setScheduleRealtionship(StopTimeUpdate.ScheduleRelationship.SCHEDULED_VALUE);
            }

            if (!stopTimeUpdate.getScheduleRelationship().equals(StopTimeUpdate.ScheduleRelationship.SKIPPED)) {
              int currentArrivalTime = computeArrivalTime(stopTime,
                      stopTimeUpdate, instance.getServiceDate());
              int currentDepartureTime = computeDepartureTime(stopTime,
                      stopTimeUpdate, instance.getServiceDate());

              if (currentArrivalTime >= 0) {
                if (onBestTrip) {
                  updateBestScheduleDeviation(currentTime,
                          stopTime.getArrivalTime(), currentArrivalTime, best, tripId, vehicleId);
                }

                long timepointPredictedTime = instance.getServiceDate() + (currentArrivalTime * 1000L);
                tpr.setTimepointPredictedArrivalTime(timepointPredictedTime);
              }

              if (currentDepartureTime >= 0) {
                if (onBestTrip) {
                  updateBestScheduleDeviation(currentTime,
                          stopTime.getDepartureTime(), currentDepartureTime, best, tripId, vehicleId);
                }

                long timepointPredictedTime = instance.getServiceDate() + (currentDepartureTime * 1000L);
                tpr.setTimepointPredictedDepartureTime(timepointPredictedTime);
              }

              if (tpr.getTimepointPredictedArrivalTime() != -1 ||
                      tpr.getTimepointPredictedDepartureTime() != -1) {
                // sadly we can only consume this tpr if onBestTrip
                // TODO refactor TDS to support timepoint records for multiple trips
                if (onBestTrip)
                  timepointPredictions.add(tpr);
              }

            } // end not skipped
          }
        }
      }


      if (timepointPredictions.size() == 1 && tripUpdates.get(0).getStopTimeUpdateList().size() == 1) {
        singleTimepointRecord = true;
      }
      // If we have a TripUpdate delay and timepoint predictions, interpolate
      // timepoint predictions for close, unserved stops. See GtfsRealtimeTripLibraryTest
      // for full explanation
      // tripUpdateHasDelay = true => best.scheduleDeviation is TripUpdate delay
      if ((timepointPredictions.size() > 0 && tripUpdateHasDelay)
              || singleTimepointRecord) {
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
          long scheduledArrivalTime = instance.getServiceDate() + stopTime.getArrivalTime() * 1000;
          long time = best.timestamp != 0 ? best.timestamp : currentTime();

            /*
             * if the timpepointrecord needs interpolated (one before, one after),
             * OR
             * we have a single Timepoint record and the arrival is
              * in the future and before the last stop
             */
            if ((predictedDepartureTime > time && predictedDepartureTime < tprStartTime)
                    || (singleTimepointRecord
                    && (predictedDepartureTime > time
                    && scheduledArrivalTime <= lastStopScheduleTime))) {
            TimepointPredictionRecord tpr = new TimepointPredictionRecord();
            tpr.setTimepointId(stopTime.getStop().getId());
            tpr.setTripId(stopTime.getTrip().getId());
            tpr.setStopSequence(stopTime.getGtfsSequence());
            tpr.setTimepointPredictedArrivalTime(predictedArrivalTime);
            tpr.setTimepointPredictedDepartureTime(predictedDepartureTime);
            tpr.setTimepointScheduledTime(scheduledArrivalTime);
            tpr.setScheduleRealtionship(StopTimeUpdate.ScheduleRelationship.SCHEDULED_VALUE);
            timepointPredictions.add(tpr);
          }
        }
      }
    }

    record.setServiceDate(instance.getServiceDate());
    if (blockDescriptor.getStartTime() != null) {
      record.setBlockStartTime(blockDescriptor.getStartTime());
    }

    if(blockDescriptor.getScheduleRelationship() != null && !best.isCanceled)
      record.setStatus(blockDescriptor.getScheduleRelationship().toString());

    if (!best.isCanceled)
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
    // instead of illegal state exception we return -1 to not corrupt the read
    _log.debug("expected at least an arrival or departure time or delay for update: "
            + stopTimeUpdate);
    return -1;
  }

  private int computeArrivalTime(StopTimeEntry stopTime,
      StopTimeUpdate stopTimeUpdate, long serviceDate) {
    if (!stopTimeUpdate.hasArrival())
      return -1;
    StopTimeEvent arrival = stopTimeUpdate.getArrival();
    if (arrival.hasTime())
      return (int) (arrival.getTime() - serviceDate / 1000);
    //prefer time to delay usage to be consistent with elsewhere
    if (arrival.hasDelay())
      return stopTime.getArrivalTime() + arrival.getDelay();

    // instead of illegal state exception we return -1 to not corrupt the read
    return -1;
  }

  private int computeDepartureTime(StopTimeEntry stopTime,
      StopTimeUpdate stopTimeUpdate, long serviceDate) {
    if (!stopTimeUpdate.hasDeparture())
      return -1;
    StopTimeEvent departure = stopTimeUpdate.getDeparture();
    if (departure.hasTime())
      return (int) (departure.getTime() - serviceDate / 1000);
    //prefer time to delay usage to be consistent with elsewhere
    if (departure.hasDelay())
      return stopTime.getDepartureTime() + departure.getDelay();
    // instead of throwing an exception here, simply return -1
    // so as to not stop the rest of the processing
    return -1;
  }

  private void updateBestScheduleDeviation(int currentTime,
      int expectedStopTime, int actualStopTime, BestScheduleDeviation best, AgencyAndId tripId, String vehicleId) {

    int delta = Math.abs(currentTime - actualStopTime);
    boolean isInPast = currentTime > actualStopTime;
    int scheduleDeviation = actualStopTime - expectedStopTime;
    
    if (delta < best.delta || (!isInPast && best.isInPast)) {
      best.delta = delta;
      best.isInPast = isInPast;
      best.scheduleDeviation = scheduleDeviation;
      best.tripId = tripId;
    }
  }

  private void applyVehiclePositionToRecord(MonitoredResult result,
      BlockDescriptor blockDescriptor,
      VehiclePosition vehiclePosition,
      VehicleLocationRecord record) {
    Position position = vehiclePosition.getPosition();
    if (vehiclePosition.hasTimestamp()) {
      record.setTimeOfLocationUpdate(TimeUnit.SECONDS.toMillis(vehiclePosition.getTimestamp())); //vehicle timestamp is in seconds
    }
    record.setCurrentLocationLat(position.getLatitude());
    record.setCurrentLocationLon(position.getLongitude());
    if (result != null) {
      result.addLatLon(position.getLatitude(), position.getLongitude());
    }
    if (_scheduleAdherenceFromLocation) {
      CoordinatePoint location = new CoordinatePoint(position.getLatitude(), position.getLongitude());
      double totalDistance = blockDescriptor.getBlockInstance().getBlock().getTotalBlockDistance();
      long timestamp = vehiclePosition.hasTimestamp() ? record.getTimeOfLocationUpdate() : record.getTimeOfRecord();
      ScheduledBlockLocation loc = _blockGeospatialService.getBestScheduledBlockLocationForLocation(
          blockDescriptor.getBlockInstance(), location, timestamp, 0, totalDistance);
      
      long serviceDateTime = record.getServiceDate();
      long effectiveScheduleTime = loc.getScheduledTime() + (serviceDateTime/1000);
      double deviation =  timestamp/1000 - effectiveScheduleTime;
      double oldDeviation = record.getScheduleDeviation();
      record.setScheduleDeviation(deviation);
      _log.debug("deviation reset to {} from {} for vehicle {}", deviation, oldDeviation, vehiclePosition.getVehicle().getId());

    }
  }
  
  private static long getEarliestTimeInRecords(Collection<TimepointPredictionRecord> records) {
    long min = Long.MAX_VALUE;
    for (TimepointPredictionRecord tpr : records) {
      if (tpr.getTimepointPredictedArrivalTime() != -1) {
        min = Math.min(min, tpr.getTimepointPredictedArrivalTime());
      }
      else if (tpr.getTimepointPredictedDepartureTime() != -1) {
        min = Math.min(min, tpr.getTimepointPredictedDepartureTime());
      }
    }
    return min;
  }

  private long currentTime() {
    if (_currentTime != 0) {
      // if the feed clock is off by more than an hour we most likely have a timezone issue
      if (validateCurrentTime() && Math.abs(_currentTime - SystemTime.currentTimeMillis()) > 60 * 60 * 1000) {
        _log.error("timestamp invalid at " + new Date(_currentTime) + ", overriding with system time");
        _currentTime = SystemTime.currentTimeMillis();
      }
      return _currentTime;
    }
    return SystemTime.currentTimeMillis();
  }

  private String getVehicleId(TripUpdate tu) {
    if (_useLabelAsVehicleId && tu.hasVehicle() && tu.getVehicle().hasLabel())
      return tu.getVehicle().getLabel();
    return tu.getVehicle().getId();
  }
  private String getVehicleId(VehiclePosition vp) {
    if (_useLabelAsVehicleId && vp.hasVehicle() && vp.getVehicle().hasLabel())
      return vp.getVehicle().getLabel();
    return vp.getVehicle().getId();
  }

    public VehicleOccupancyRecord createVehicleOccupancyRecordForUpdate(MonitoredResult result,
                                                                        CombinedTripUpdatesAndVehiclePosition update) {
      if (update == null) return null;
      if (update.vehiclePosition == null) return null;
      if (update.vehiclePosition.hasOccupancyStatus()
              && update.vehiclePosition.hasVehicle()
              && update.vehiclePosition.getVehicle().hasId()
              && update.bestTrip != null) {  // if we are not on an active trip, we don't store
        VehicleOccupancyRecord vor = new VehicleOccupancyRecord();
        // here we assume the vehicle's agency matches that of its block
        vor.setVehicleId(new AgencyAndId(update.block.getBlockInstance().getBlock().getBlock().getId().getAgencyId(), update.block.getVehicleId()));
        vor.setOccupancyStatus(OccupancyStatus.valueOf(update.vehiclePosition.getOccupancyStatus().name()));
        TripEntry trip = _entitySource.getTrip(update.bestTrip);
        // link this occupancy to route+direction so it will expire at end of trip
        if (trip != null && trip.getRoute() != null) {
          vor.setRouteId(AgencyAndIdLibrary.convertToString(trip.getRoute().getId()));
          vor.setDirectionId(trip.getDirectionId());
        }
        if (vor.getOccupancyStatus() == null) {
          // the valueOf failed to match, the spec may have added new fields...
          _log.warn("unmatched occupancy status " + update.vehiclePosition.getOccupancyStatus().name());
          return null;
        }
        return vor;
      }
      return null;
    }

    private static class BestScheduleDeviation {
    public int delta = Integer.MAX_VALUE;
    public int scheduleDeviation = 0;
    public boolean isInPast = true;
    public long timestamp = 0;
    public AgencyAndId tripId = null;
    public boolean isCanceled = false;
  }

  private static class AssignmentInfo {
    private Map<String, String> preferredTripByVehicleId;
    private Map<String, String> preferredVehicleByBlockId;
    public AssignmentInfo(Map<String, String> preferredTripByVehicleId,
                          Map<String, String> preferredVehicleByBlockId) {
     this.preferredTripByVehicleId = preferredTripByVehicleId;
     this.preferredVehicleByBlockId = preferredVehicleByBlockId;
    }
  }
}
