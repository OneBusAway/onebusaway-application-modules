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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.google.transit.realtime.GtfsRealtimeCrowding;
import com.google.transit.realtime.GtfsRealtimeMTARR;
import com.google.transit.realtime.GtfsRealtimeNYCT;
import com.google.transit.realtime.GtfsRealtimeOneBusAway;
import org.apache.commons.lang.StringUtils;
import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.collections.Min;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.realtime.api.OccupancyStatus;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.realtime.api.VehicleOccupancyRecord;
import org.onebusaway.transit_data.model.StopDirectionSwap;
import org.onebusaway.transit_data.model.TransitDataConstants;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.transit_graph.*;
import org.onebusaway.transit_data_federation.services.transit_graph.dynamic.DynamicTripEntryImpl;
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

public class GtfsRealtimeTripLibrary {

  private static final Logger _log = LoggerFactory.getLogger(GtfsRealtimeTripLibrary.class);

  private static Pattern _pattern = Pattern.compile("^(-{0,1}\\d+):(\\d{2}):(\\d{2})$");
  private GtfsRealtimeEntitySource _entitySource;
  private GtfsRealtimeServiceSource _serviceSource;

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

  private boolean _filterUnassigned = false;

  public void setEntitySource(GtfsRealtimeEntitySource entitySource) {
    _entitySource = entitySource;
  }

  public void setServiceSource(GtfsRealtimeServiceSource serviceSource) {
    _serviceSource = serviceSource;
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
    if (_entitySource != null) {
      _entitySource.setCurrentTime(_currentTime);
    }
    
  }
  
  public void setStopModificationStrategy(StopModificationStrategy strategy) {
    _stopModificationStrategy = strategy;
  }
  
  public void setScheduleAdherenceFromLocation(boolean scheduleAdherenceFromLocation) {
    _scheduleAdherenceFromLocation = scheduleAdherenceFromLocation;
  }
  
  /**
   * use the vehicle label as the id.
   * @param useLabelAsVehicleId
   */
  public void setUseLabelAsVehicleId(boolean useLabelAsVehicleId) {
    _useLabelAsVehicleId = useLabelAsVehicleId;
  }

  public void setFilterUnassigned(boolean flag) {
    _filterUnassigned = flag;
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
    try {
      return groupTripUpdatesAndVehiclePositionsInternal(result, tripUpdateMessage, vehiclePositionsMessage);
    } catch (Throwable t) {
      _log.error("source-exception {}", t, t);
      return new ArrayList<>();
    }
  }
    public List<CombinedTripUpdatesAndVehiclePosition> groupTripUpdatesAndVehiclePositionsInternal(MonitoredResult result,
      FeedMessage tripUpdateMessage, FeedMessage vehiclePositionsMessage) {

    List<CombinedTripUpdatesAndVehiclePosition> updates = new ArrayList<CombinedTripUpdatesAndVehiclePosition>();
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
      BlockDescriptor bd = null;
      if (tu.hasTrip() && TransitDataConstants.STATUS_DUPLICATED.equals(tu.getTrip().getScheduleRelationship().toString())) {
        AddedTripInfo addedTripInfo = _serviceSource.getDuplicatedTripService().handleDuplicatedDescriptor(tu);
        bd = _serviceSource.getDynamicTripBuilder().createBlockDescriptor(addedTripInfo, getCurrentTime());
        if (bd == null) continue; // we failed
        anonymousTripUpdatesByBlock.put(bd, tu);
        continue; // don't let this trip update be processed
      }

      if (getVehicleId(tu) != null) {
        // Trip update has a vehicle ID - index by vehicle ID
        String vehicleId = getVehicleId(tu);
        tripUpdatesByVehicleId.put(vehicleId, addStartDateTime(tu));
      } else {
        /*
         * Trip update does not have a vehicle ID - index by TripDescriptor
         * (includes start date and time).
         */
        TripDescriptor td = tu.getTrip();
        long time = tu.hasTimestamp() ? ensureMillis(tu.getTimestamp()) : _currentTime;
        if (bd == null) {
          bd = getTripDescriptorAsBlockDescriptor(result, td, time, null);
        }

        if (bd == null) {
          bd = handleDynamicTripUpdate(tu);
          if (bd == null) continue; // we failed

          if (bd.getVehicleId() != null) {
            tripUpdatesByVehicleId.put(bd.getVehicleId(), tu);
          } else {
            // if this trip has a vehiclePosition it will be matched later
            anonymousTripUpdatesByBlock.put(bd, tu);
          }
        }

        // if this block has an assigned vehicle consume the tripUpdate
        // if the block/vehicle matches (we support multiple updates per block)
        TripEntry tripEntry = _entitySource.getTrip(td.getTripId());
        if (tripEntry != null && tripEntry.getBlock() != null) {
          String blockId = tripEntry.getBlock().getId().toString();
          if (assignmentInfo.preferredVehicleByBlockId.containsKey(blockId)) {
            String preferredVehicleId = assignmentInfo.preferredVehicleByBlockId.get(blockId);
            _log.debug("adding anonymous trip update {} into vehicle {}", td.getTripId(), preferredVehicleId);
            // this is a multimap; it supports multiple updates per block/vehicle
            tripUpdatesByVehicleId.put(preferredVehicleId, tu);
          } else {
            anonymousTripUpdatesByBlock.put(bd, tu);
          }
        } else {
          // accept multiple updates here -- though the may be lost if we can't map them
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

          long otherTimestamp = ensureMillis(otherUpdate.getTimestamp());

          if (ensureMillis(vp.getTimestamp()) > otherTimestamp) {
            vehiclePositionsByVehicleId.put(vehicleId, vp);
          }

        }
      } else if (vp.hasTrip()) {
        /*
         * Vehicle position does not have vehicle ID but has TripDescriptor, so
         * use that, but only if there is only one.
         */

        TripDescriptor td = vp.getTrip();
        long time = vp.hasTimestamp() ? ensureMillis(vp.getTimestamp()) : _currentTime;
        BlockDescriptor bd = getTripDescriptorAsBlockDescriptor(result, td, time, null);

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

      // use the first trip to find the block, but pass through all tripUpdates
      TripUpdate firstTrip = tripUpdates.iterator().next();
      long time = firstTrip.hasTimestamp() ? ensureMillis(firstTrip.getTimestamp()) : _currentTime;
      update.block = getTripDescriptorAsBlockDescriptor(result, firstTrip.getTrip(), time, vehicleId);
      if (isNycDynamicTrip(firstTrip)) {
        update.block = handleDynamicTripUpdate(firstTrip);
      }
      // pass through multiple trip updates per block
      update.setTripUpdates(new ArrayList<>(tripUpdates));

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
      update.setTripUpdates(new ArrayList<TripUpdate>(e.getValue()));

      if (update.getTripUpdatesSize() == 1
          && anonymousVehiclePositionsByBlock.containsKey(bd)) {
        update.vehiclePosition = anonymousVehiclePositionsByBlock.get(bd);
      }

      updates.add(update);
    }

    for (CombinedTripUpdatesAndVehiclePosition update : updates) {
      // Set vehicle ID in block if missing
      if ( update.block != null && update.block.getVehicleId() == null) {
        String vehicleId = null;

        for (TripUpdate tu : update.getTripUpdates()) {
          vehicleId = getVehicleId(tu);
          if (vehicleId != null)
            break;
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
    }

    return updates;
  }

  long ensureMillis(long timestamp) {
    // some feeds use millis, but the specification says seconds
    // logic here is if the timestamp is not within 100 years then it is the wrong scale
    long diff = System.currentTimeMillis()/1000 - timestamp;
    if (Math.abs(diff) > 100l * 365 * 24 * 60 * 60) {
      return timestamp;
    }
    return timestamp * 1000;
  }

  private BlockDescriptor handleDynamicTripUpdate(TripUpdate tu) {
    try {
      TripDescriptor td = tu.getTrip();
      // we didn't match to bundle, are we an added trip?
      if (td.hasExtension(GtfsRealtimeNYCT.nyctTripDescriptor)) {
        GtfsRealtimeNYCT.NyctTripDescriptor nyctTripDescriptor = td.getExtension(GtfsRealtimeNYCT.nyctTripDescriptor);
        _log.debug("parsing trip {}", td.getTripId());
        AddedTripInfo addedTripInfo = _serviceSource.getAddedTripService().handleNyctDescriptor(_serviceSource, tu, nyctTripDescriptor, _currentTime);
        if (addedTripInfo == null) return null;
        long tripStartTimeMillis = addedTripInfo.getServiceDate() + (addedTripInfo.getTripStartTime() * 1000);
        if (_filterUnassigned && !nyctTripDescriptor.getIsAssigned()) {
          // we are filtering on unassigned and this trip is marked as unassigned
          return null;
        }
        if (!nyctTripDescriptor.getIsAssigned()
                && tripStartTimeMillis < _currentTime) {
          // don't let unassigned trips in the past show up
          return null;
        }
        // convert to blockDescriptor
        return _serviceSource.getDynamicTripBuilder().createBlockDescriptor(addedTripInfo, getCurrentTime());

      } else {
        if (td.getScheduleRelationship().equals(TripDescriptor.ScheduleRelationship.ADDED)) {
          AddedTripInfo addedTripInfo = _serviceSource.getAddedTripService().handleAddedDescriptor(_serviceSource, _entitySource.getAgencyIds().get(0), tu, _currentTime);
          if (addedTripInfo != null) {
            return _serviceSource.getDynamicTripBuilder().createBlockDescriptor(addedTripInfo, getCurrentTime());
          }
        }
      }
      return null;
    } catch (Throwable t) {
      _log.error("source-exception {}", t, t);
      return null;
    }
  }

  // in order to support multiple trip updates per block we need
  // to internally require trip_start_time which means we formally
  // require trip_start_date;
  private TripUpdate addStartDateTime(TripUpdate tu) {
    if (!tu.hasTrip() || !tu.getTrip().hasTripId()) {
      throw new IllegalStateException("unidentifiable trip " + tu);
    }
    if (tu.getTrip().hasStartTime()) {
      //nothing to do
      return tu;
    }
    if (isNycDynamicTrip(tu)) {
      return tu; // we can get this from descriptor
    }

    TripEntry trip = _entitySource.getTrip(tu.getTrip().getTripId());
    if (trip == null || trip.getStopTimes() == null || trip.getStopTimes().isEmpty()) {
      _log.error("no stoptimes for trip {} on agencies {}, cannot determine start time", tu.getTrip().getTripId(), _entitySource.getAgencyIds());
      return tu;
    }
    StopTimeEntry stopTimeEntry = trip.getStopTimes().get(0);
    int arrivalTime = stopTimeEntry.getArrivalTime();

    ServiceDate serviceDate = null;
    String dateString = null;
    if (tu.getTrip().hasStartDate())
      dateString = tu.getTrip().getStartDate();
    if (dateString == null || dateString.length() == 0)
      dateString = "00000000"; // reference from epoch
    try {
      serviceDate = ServiceDate.parseString(dateString);
    } catch (ParseException e) {
      _log.error("invalid date format |" + tu.getTrip().getStartDate() +
              "| for trip |" + tu.getTrip().getTripId() + "|");
      return tu;
    }
    Date startTime = new Date(serviceDate.getAsDate().getTime() + (arrivalTime * 1000));
    SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm:ss");
    TripDescriptor.Builder tdBuilder = tu.getTrip().toBuilder();
    tdBuilder.setStartTime(sdfTime.format(startTime));
    TripUpdate.Builder builder = tu.toBuilder();
    return builder.setTrip(tdBuilder.build()).build();
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
    record.setTimeOfRecord(_currentTime); // this is just the default -- if we have tripUpdates this will be re-written

    BlockDescriptor blockDescriptor = update.block;
    if (update.block == null) return null;
    record.setMutated(update.block.getMutated());
    String vehicleId = update.block.getVehicleId(); // todo this is messy as its unqualified and rewritten later
    record.setBlockId(blockDescriptor.getBlockInstance().getBlock().getBlock().getId());
    // this is the default, trip updates may cancel this trip
    record.setStatus(blockDescriptor.getScheduleRelationship().toString());

    if (TransitDataConstants.STATUS_ADDED.equals(update.block.getScheduleRelationship().toString())
    || TransitDataConstants.STATUS_DUPLICATED.equals(update.block.getScheduleRelationship().toString())
    || isNycDynamicTrip(update)) {
      applyDynamicTripUpdatesToRecord(result, blockDescriptor, update.getTripUpdates(), record, vehicleId);
    } else {
      applyTripUpdatesToRecord(result, blockDescriptor, update.getTripUpdates(), record, vehicleId);
    }

    if (update.vehiclePosition != null) {
      applyVehiclePositionToRecord(result, blockDescriptor, update.vehiclePosition, record);
    }

    /**
     * By default, we use the block id as the vehicle id
     */
    if (record.getVehicleId() == null) {
      record.setVehicleId(record.getBlockId());
    }

    if (result != null) {
      if (record.getTripId() != null) {
        if (record.getStatus().equals(TransitDataConstants.STATUS_ADDED)) {
          result.addAddedTripId(record.getTripId().toString());
        } else if (record.getStatus().equals(TransitDataConstants.STATUS_DUPLICATED)) {
          result.addDuplicatedTripId(record.getTripId().toString());
        } else if (record.getStatus().equals(TransitDataConstants.STATUS_CANCELED)) {
          result.addCancelledTripId(record.getTripId().toString());
        } else {
          if (isTripActive(update)) {
            result.addMatchedTripId(record.getTripId().toString());
          }
        }
      } else if (record.getBlockId() != null) {
        if (record.getStatus().equals(TransitDataConstants.STATUS_CANCELED)) {
          result.addCancelledTripId(record.getBlockId().toString());
        } else {
          if (isTripActive(update)) {
            // here we take a matched block as if it were a trip
            result.addMatchedTripId(record.getBlockId().toString());
          }
        }
      } else {
        if (isTripActive(update)) {
          // we don't have a tripId, use the BlockId instead
          result.addMatchedTripId(record.getBlockId().toString());
        }
      }
    }
    
    if (blockDescriptor.getVehicleId() != null) {
      String agencyId = record.getBlockId().getAgencyId();
      try {
        AgencyAndId vehicleAgencyAndId = AgencyAndIdLibrary.convertFromString(blockDescriptor.getVehicleId());
        record.setVehicleId(vehicleAgencyAndId);
      } catch (IllegalStateException ise) {
        record.setVehicleId(new AgencyAndId(agencyId,
                blockDescriptor.getVehicleId()));
      }
    }

    return record;
  }

  private boolean isTripActive(CombinedTripUpdatesAndVehiclePosition update) {
    if (update.getTripUpdates().isEmpty())
      return false;
    // how far in the future a prediction can be while still being considered active
    long windowFuture = 60 * 60;
    TripUpdate tripUpdate = update.getTripUpdates().get(0);
    int tripUpdateCount = update.getTripUpdates().get(0).getStopTimeUpdateCount();
    if (tripUpdateCount == 0)
      return false;
    long firstPrediction = -1;
    long lastPrediction = -1;
    StopTimeUpdate firstStopTime = tripUpdate.getStopTimeUpdate(0);
    StopTimeUpdate lastStopTime = tripUpdate.getStopTimeUpdate(tripUpdateCount-1);

    if (lastStopTime.hasArrival())
      if (!lastStopTime.getArrival().hasTime()
              && lastStopTime.getArrival().hasDelay()
              && tripUpdate.getTrip().getScheduleRelationship()
              .equals(TripDescriptor.ScheduleRelationship.SCHEDULED))
    return true;

    if (firstStopTime.hasArrival())
      firstPrediction = firstStopTime.getArrival().getTime();
    else if (firstStopTime.hasDeparture())
      firstPrediction = firstStopTime.getDeparture().getTime();

    if (lastStopTime.hasDeparture())
      lastPrediction = lastStopTime.getDeparture().getTime();
    else if (lastStopTime.hasArrival())
      lastPrediction = lastStopTime.getArrival().getTime();
    if (firstPrediction < 0 || lastPrediction < 0)
      return false;
    long currentTime = _currentTime/1000;
    // part 1: currentTime:14:10 + 01:00 - firstPrediction:14:11 is positive
    // part 2: currentTime:14:10 - lastPrediction:14:50 is negative
    boolean active = (
            currentTime + windowFuture > firstPrediction
            && lastPrediction > currentTime
            );
    return active;
  }

  private boolean isNycDynamicTrip(CombinedTripUpdatesAndVehiclePosition update) {
    // check the trip hasExtension nyct_trip_descriptor
    if (!update.getTripUpdates().isEmpty())
      if (update.getTripUpdates().get(0).hasTrip())
        return isNycDynamicTrip(update.getTripUpdates().get(0));
    return false;
  }

  private boolean isNycDynamicTrip(TripUpdate tu) {
    if (tu.hasTrip()) {
      if (tu.getTrip().hasExtension(GtfsRealtimeNYCT.nyctTripDescriptor))
        return true;
      if (tu.getTrip().hasScheduleRelationship()) {
        return tu.getTrip().getScheduleRelationship().equals(TripDescriptor.ScheduleRelationship.ADDED)
                || tu.getTrip().getScheduleRelationship().equals(TripDescriptor.ScheduleRelationship.DUPLICATED);
      }
    }
    return false;
  }



  private void applyDynamicTripUpdatesToRecord(MonitoredResult result,
                                               BlockDescriptor blockDescriptor,
                                               List<TripUpdate> tripUpdates,
                                               VehicleLocationRecord record,
                                               String vehicleId) {
    try {
      boolean isDuplicated = blockDescriptor.getScheduleRelationship().equals(BlockDescriptor.ScheduleRelationship.DUPLICATED);
      String agencyId = blockDescriptor.getBlockInstance().getBlock().getBlock().getId().getAgencyId();
      record.setStatus(blockDescriptor.getScheduleRelationship().toString());
      record.setServiceDate(blockDescriptor.getBlockInstance().getServiceDate());
      record.setTimeOfRecord(_currentTime);
      if (blockDescriptor.getVehicleId() != null) {
        record.setVehicleId(new AgencyAndId(agencyId, blockDescriptor.getVehicleId()));
      }
      if (blockDescriptor.getStartTime() != null) {
        record.setBlockStartTime(blockDescriptor.getStartTime());
      } else {
        record.setBlockStartTime(getFirstStpTime(blockDescriptor));
      }
      List<TimepointPredictionRecord> timepointPredictions = new ArrayList<TimepointPredictionRecord>();
      for (TripUpdate tripUpdate : tripUpdates) {
        if (record.getTripId() == null) {
          // if duplicated alter tripId so its unique
          if (isDuplicated) {
            record.setTripId(new AgencyAndId(agencyId, markDuplicated(tripUpdate.getTrip().getTripId())));
          } else {
            record.setTripId(new AgencyAndId(agencyId, tripUpdate.getTrip().getTripId()));
          }
        }
        // TODO: this be actual index but that proves difficult with
        // variability of realtime.
        int sequence = 0;
        for (StopTimeUpdate stu : tripUpdate.getStopTimeUpdateList()) {
          TimepointPredictionRecord tpr = new TimepointPredictionRecord();
          AgencyAndId correctedStopId = enforceWrongWayConcurrency(blockDescriptor, new AgencyAndId(agencyId, tripUpdate.getTrip().getTripId()),
                  new AgencyAndId(agencyId,stu.getStopId()));
          tpr.setTimepointId(correctedStopId);
          StopEntry testStop = this._entitySource.getStop(tpr.getTimepointId());
          if (testStop == null) {
            _log.debug("discarding stu for unknown stop {}", tpr.getTimepointId());
            continue;
          }

          // if duplicated alter tripId so its unique
          if (isDuplicated) {
            tpr.setTripId(new AgencyAndId(agencyId, markDuplicated(tripUpdate.getTrip().getTripId())));
          } else {
            tpr.setTripId(new AgencyAndId(agencyId, tripUpdate.getTrip().getTripId()));
          }
          tpr.setStopSequence(-1); // don't set the stop sequence if its not from GTFS
          sequence++;
          switch (stu.getScheduleRelationship()) {
            case SCHEDULED:
              tpr.setScheduleRealtionship(TimepointPredictionRecord.ScheduleRelationship.SCHEDULED.getValue());
              break;
            case SKIPPED:
              tpr.setScheduleRealtionship(TimepointPredictionRecord.ScheduleRelationship.SKIPPED.getValue());
              break;
            default:
              tpr.setScheduleRealtionship(TimepointPredictionRecord.ScheduleRelationship.SCHEDULED.getValue());
          }
          if (stu.hasArrival() && stu.getArrival().hasTime())
            tpr.setTimepointPredictedArrivalTime(stu.getArrival().getTime() * 1000);
          if (stu.hasDeparture() && stu.getDeparture().hasTime())
            tpr.setTimepointPredictedDepartureTime(stu.getDeparture().getTime() * 1000);
          if (stu.hasExtension(GtfsRealtimeNYCT.nyctStopTimeUpdate)) {
            GtfsRealtimeNYCT.NyctStopTimeUpdate ext = stu.getExtension(GtfsRealtimeNYCT.nyctStopTimeUpdate);
            if (ext.hasScheduledTrack()) {
              tpr.setScheduledTrack(ext.getScheduledTrack());
            }
            if (ext.hasActualTrack()) {
              tpr.setActualTrack(ext.getActualTrack());
            }
          }
          if (stu.hasExtension(GtfsRealtimeMTARR.mtaRailroadStopTimeUpdate)) {
            GtfsRealtimeMTARR.MtaRailroadStopTimeUpdate ext = stu.getExtension(GtfsRealtimeMTARR.mtaRailroadStopTimeUpdate);
            if (ext.hasTrack()) {
              tpr.setActualTrack(ext.getTrack());
            }
            if (ext.hasTrainStatus()) {
              tpr.setStatus(ext.getTrainStatus());
            }
          }

          timepointPredictions.add(tpr);
        }

        record.setTimepointPredictions(timepointPredictions);
        record.setScheduleDeviation(calculateScheduleDeviation(blockDescriptor.getBlockInstance(), timepointPredictions));
      }
    } catch (Throwable t) {
      _log.error("source-exception {}", t, t);
    }
  }

  private AgencyAndId enforceWrongWayConcurrency(BlockDescriptor blockDescriptor, AgencyAndId tripId, AgencyAndId stopId) {
    for (BlockTripEntry blockTripEntry : blockDescriptor.getBlockInstance().getBlock().getTrips()) {
      if (blockTripEntry.getTrip().getId().equals(tripId)) {
        AgencyAndId routeId = blockTripEntry.getTrip().getRoute().getId();
        String directionId = blockTripEntry.getTrip().getDirectionId();

        StopDirectionSwap stopDirectionSwap = _serviceSource.getStopSwapService().findStopDirectionSwap(routeId, directionId, stopId);
        if (stopDirectionSwap == null)
          return stopId;
        return stopDirectionSwap.getToStop();

      }
    }
  return stopId;
  }

  private String markDuplicated(String tripId) {
    return tripId + "_Dup";
  }

  private int getFirstStpTime(BlockDescriptor blockDescriptor) {
    if (blockDescriptor.getBlockInstance() != null)
      if (!blockDescriptor.getBlockInstance().getBlock().getTrips().isEmpty())
        if (!blockDescriptor.getBlockInstance().getBlock().getTrips().get(0).getStopTimes().isEmpty())
          return blockDescriptor.getBlockInstance().getBlock().getTrips().get(0).getStopTimes().get(0).getStopTime().getDepartureTime();
    return -1;
  }

  /**
   * scheduleDeviation - in seconds (+deviation is late, -deviation is
   *    *          early)
   */
  private double calculateScheduleDeviation(BlockInstance blockInstance, List<TimepointPredictionRecord> timepointPredictions) {
    int predictionSize = timepointPredictions.size();
    int stopTimesSize = blockInstance.getBlock().getTrips().get(0).getStopTimes().size();
    if (predictionSize < 1) {
      _log.debug("not enough data to calculate deviation");
      return 0.0;
    }
    TimepointPredictionRecord timepointPredictionRecord = timepointPredictions.get(predictionSize - 1);
    BlockStopTimeEntry blockStopTimeEntry = blockInstance.getBlock().getTrips().get(0).getStopTimes().get(stopTimesSize - 1);

    // we assume linear interpolation of stops, so compare last stops for schedule deviation
    AgencyAndId predictionStopId = timepointPredictionRecord.getTimepointId();
    AgencyAndId stopTimeStopId = blockStopTimeEntry.getStopTime().getStop().getId();
    if (predictionStopId.equals(stopTimeStopId)) {
      if (timepointPredictionRecord.getTimepointPredictedArrivalTime() > 0)
        return calculateScheduleDeviation(blockInstance.getServiceDate(), timepointPredictionRecord.getTimepointPredictedArrivalTime(),
                blockStopTimeEntry.getStopTime().getArrivalTime());
      if (timepointPredictionRecord.getTimepointPredictedDepartureTime() > 0)
        return calculateScheduleDeviation(blockInstance.getServiceDate(), timepointPredictionRecord.getTimepointPredictedDepartureTime(),
                blockStopTimeEntry.getStopTime().getDepartureTime());
    }

    // we didn't match on the last stop, do a simple search for same stops
    for (BlockStopTimeEntry stopTime : blockInstance.getBlock().getTrips().get(0).getStopTimes()) {
      stopTimeStopId = stopTime.getStopTime().getStop().getId();
      for (TimepointPredictionRecord timepointPrediction : timepointPredictions) {
        predictionStopId = timepointPrediction.getTimepointId();
        if (stopTimeStopId.equals(predictionStopId)) {
          if (timepointPrediction.getTimepointPredictedArrivalTime() > 0) {
            return calculateScheduleDeviation(blockInstance.getServiceDate(), timepointPredictionRecord.getTimepointPredictedArrivalTime(),
                    blockStopTimeEntry.getStopTime().getArrivalTime());
          }
          if (timepointPrediction.getTimepointPredictedDepartureTime() > 0) {
            return calculateScheduleDeviation(blockInstance.getServiceDate(), timepointPredictionRecord.getTimepointPredictedDepartureTime(),
                    blockStopTimeEntry.getStopTime().getDepartureTime());
          }
        }
      }
    }
    return 0;  // nothing matched, assume on time
  }

  private double calculateScheduleDeviation(long serviceDate, long predictionMillis, int stopTimeSeconds) {
    long stopTime = serviceDate + (stopTimeSeconds * 1000);
    double deviation = (predictionMillis / 1000) - (stopTime / 1000);
    return deviation;
  }



  private BlockDescriptor getTripDescriptorAsBlockDescriptor(MonitoredResult result,
      TripDescriptor trip, long currentTime, String vehicleId) {
    try {
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

      BlockServiceDate _blockserviceDate = _serviceSource.getBlockFinder().getBlockServiceDateFromTrip(tripEntry, currentTime);
      if (_blockserviceDate == null) {
        // service date is mandatory, we need to abort
        _log.error("could not determine service date for trip {}", trip.getTripId());
        return null;
      }

      BlockInstance instance = _blockserviceDate.getBlockInstance();
      ServiceDate serviceDate = _blockserviceDate.getServiceDate();
      Integer startTime = _blockserviceDate.getTripStartTime();

      BlockDescriptor blockDescriptor = new BlockDescriptor();
      blockDescriptor.setBlockInstance(instance);
      blockDescriptor.setStartDate(serviceDate);
      blockDescriptor.setStartTime(startTime);
      if (trip.hasScheduleRelationship()) {
        if (isDynamicTrip(tripEntry)) {
          blockDescriptor.setScheduleRelationship(BlockDescriptor.ScheduleRelationship.ADDED);
        } else {
          blockDescriptor.setScheduleRelationshipValue(trip.getScheduleRelationship().toString());
        }
      } else {
        if (isDynamicTrip(tripEntry)) {
          blockDescriptor.setScheduleRelationship(BlockDescriptor.ScheduleRelationship.ADDED);
        }
      }
      blockDescriptor.setVehicleId(vehicleId);
      return blockDescriptor;
    } catch (Throwable t) {
      _log.error("source-exception {}", t, t);
      return null;
    }
  }

  private boolean isDynamicTrip(TripEntry trip) {
    return trip instanceof DynamicTripEntryImpl;
  }

  
  private void applyTripUpdatesToRecord(MonitoredResult result, BlockDescriptor blockDescriptor,
      List<TripUpdate> tripUpdates, VehicleLocationRecord record, String vehicleId) {
    try {
      BlockInstance instance = blockDescriptor.getBlockInstance();

      BlockConfigurationEntry blockConfiguration = instance.getBlock();
      List<BlockTripEntry> blockTrips = blockConfiguration.getTrips();
      Map<String, List<TripUpdate>> tempTripUpdatesByTripId = MappingLibrary.mapToValueList(
              tripUpdates, "trip.tripId");
      Map<String, List<TripUpdate>> tripUpdatesByTripId = new HashMap<>();
      // if we have fuzzy matching enabled
      if (this._entitySource.getTripIdRegexes() != null) {
        for (String existingTripId : tempTripUpdatesByTripId.keySet()) {
          String newTripId = existingTripId;
          for (String tripIdRegex : this._entitySource.getTripIdRegexes()) {
              newTripId = newTripId.replaceAll(tripIdRegex, "");
            }
          AgencyAndId keyAndId = fuzzyMatchTripId(new AgencyAndId(instance.getBlock().getBlock().getId().getAgencyId(), newTripId));
          if (keyAndId != null) {
            tripUpdatesByTripId.put(keyAndId.getId(), tempTripUpdatesByTripId.get(existingTripId));
          }
        }
      } else {
        // no fuzzy matching, copy the reference
        tripUpdatesByTripId = tempTripUpdatesByTripId;
      }

      long t = _currentTime;
      int currentTime = (int) ((t - instance.getServiceDate()) / 1000);
      // best is just used to calculate instantaneous schedule deviation
      // it no longer selects the "best trip"
      BestScheduleDeviation best = new BestScheduleDeviation();
      long lastStopScheduleTime = Long.MIN_VALUE;
      boolean singleTimepointRecord = false;

      List<TimepointPredictionRecord> timepointPredictions = new ArrayList<TimepointPredictionRecord>();

      for (BlockTripEntry blockTrip : blockTrips) {
        TripEntry trip = blockTrip.getTrip();
        AgencyAndId tripId = trip.getId();
        List<TripUpdate> updatesForTrip = tripUpdatesByTripId.get(tripId.getId());

        if (updatesForTrip != null) {
          for (TripUpdate tripUpdate : updatesForTrip) {

            if (tripUpdate.hasDelay()) {
              // if we have delay assume that is our schedule deviation
              best.delta = 0;
              best.isInPast = false;
              best.scheduleDeviation = tripUpdate.getDelay();
              best.tripId = tripId;
              best.tripUpdateHasDelay = true;
            }
            if (tripUpdate.hasTimestamp()) {
              best.timestamp = ensureMillis(tripUpdate.getTimestamp());
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
                  updateBestScheduleDeviation(currentTime,
                          stopTime.getArrivalTime(), currentArrivalTime, best, tripId, vehicleId);

                  long timepointPredictedTime = instance.getServiceDate() + (currentArrivalTime * 1000L);
                  tpr.setTimepointPredictedArrivalTime(timepointPredictedTime);
                }

                if (currentDepartureTime >= 0) {
                  updateBestScheduleDeviation(currentTime,
                          stopTime.getDepartureTime(), currentDepartureTime, best, tripId, vehicleId);

                  long timepointPredictedTime = instance.getServiceDate() + (currentDepartureTime * 1000L);
                  tpr.setTimepointPredictedDepartureTime(timepointPredictedTime);
                }

                if (tpr.getTimepointPredictedArrivalTime() != -1 ||
                        tpr.getTimepointPredictedDepartureTime() != -1) {
                  // we finally consume timepoints across the block
                  timepointPredictions.add(tpr);
                }

                if (stopTimeUpdate.hasExtension(GtfsRealtimeNYCT.nyctStopTimeUpdate)) {
                  GtfsRealtimeNYCT.NyctStopTimeUpdate ext = stopTimeUpdate.getExtension(GtfsRealtimeNYCT.nyctStopTimeUpdate);
                  if (ext.hasScheduledTrack()) {
                    tpr.setScheduledTrack(ext.getScheduledTrack());
                  }
                  if (ext.hasActualTrack()) {
                    tpr.setActualTrack(ext.getActualTrack());
                  }
                }
                if (stopTimeUpdate.hasExtension(GtfsRealtimeMTARR.mtaRailroadStopTimeUpdate)) {
                  GtfsRealtimeMTARR.MtaRailroadStopTimeUpdate ext = stopTimeUpdate.getExtension(GtfsRealtimeMTARR.mtaRailroadStopTimeUpdate);
                  if (ext.hasTrack()) {
                    tpr.setActualTrack(ext.getTrack());
                  }
                  if (ext.hasTrainStatus()) {
                    tpr.setStatus(ext.getTrainStatus());
                  }
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
        // best.tripUpdateHasDelay = true => best.scheduleDeviation is TripUpdate delay
        if ((timepointPredictions.size() > 0 && best.tripUpdateHasDelay)
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
            long time = best.timestamp != 0 ? best.timestamp : _currentTime;

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

      // pass along the schedule relationship as a status
      if (blockDescriptor.getScheduleRelationship() != null)
        record.setStatus(blockDescriptor.getScheduleRelationship().toString());

      if (!best.isCanceled)
        record.setScheduleDeviation(best.scheduleDeviation);
      if (best.timestamp != 0) {
        record.setTimeOfRecord(best.timestamp);
      }


      record.setTimepointPredictions(timepointPredictions);
    } catch (Throwable t) {
      _log.error("source-exception {}", t, t);
    }
  }

  private AgencyAndId fuzzyMatchTripId(AgencyAndId tripId) {
    TripEntry fuzzyTrip = _entitySource.getFuzzyTrip(tripId);
    if (fuzzyTrip == null) {
      return tripId;
    }
    return fuzzyTrip.getId();
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
    long t = _currentTime;
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

    // if scheduleDeviation comes from delay do not recalculate
    if (best.tripUpdateHasDelay)
      return;
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
      record.setTimeOfLocationUpdate(ensureMillis(vehiclePosition.getTimestamp())); //vehicle timestamp is in seconds
    }
    record.setCurrentLocationLat(position.getLatitude());
    record.setCurrentLocationLon(position.getLongitude());
    if (result != null) {
      result.addLatLon(position.getLatitude(), position.getLongitude());
    }
    if (position.hasBearing()) {
      // GTFS-RT: Bearing, in degrees, clockwise from True North, i.e.,
      // 0 is North and 90 is East. This can be the compass bearing, or the direction towards the next stop or intermediate location.
      // OBA: In degrees, 0º is East, 90º is North, 180º is West, and 270º is South
      double bearing = (position.getBearing() - 90) * -1;
      if (bearing < 0)
        bearing = bearing + 360;
      record.setCurrentOrientation(bearing);
    }
    if (_scheduleAdherenceFromLocation) {
      CoordinatePoint location = new CoordinatePoint(position.getLatitude(), position.getLongitude());
      double totalDistance = blockDescriptor.getBlockInstance().getBlock().getTotalBlockDistance();
      long timestamp = vehiclePosition.hasTimestamp() ? record.getTimeOfLocationUpdate() : record.getTimeOfRecord();
      ScheduledBlockLocation loc = _serviceSource.getBlockGeospatialService().getBestScheduledBlockLocationForLocation(
          blockDescriptor.getBlockInstance(), location, timestamp, 0, totalDistance);
      
      long serviceDateTime = record.getServiceDate();
      long effectiveScheduleTime = loc.getScheduledTime() + (serviceDateTime/1000);
      double deviation =  timestamp/1000 - effectiveScheduleTime;
      double oldDeviation = record.getScheduleDeviation();
      record.setScheduleDeviation(deviation);
      _log.debug("deviation reset to {} from {} for vehicle {}", deviation, oldDeviation, vehiclePosition.getVehicle().getId());

    }

    // MTA Bus Time extension for Vehicle Features such as STROLLER
    if (vehiclePosition.getVehicle().hasExtension(GtfsRealtimeOneBusAway.obaVehicleDescriptor)) {
      GtfsRealtimeOneBusAway.OneBusAwayVehicleDescriptor vehicleDescriptor = vehiclePosition.getVehicle().getExtension(GtfsRealtimeOneBusAway.obaVehicleDescriptor);
      for (String feature : vehicleDescriptor.getVehicleFeatureList()) {
        record.addVehicleFeature(feature);
      }
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

  private String getVehicleId(TripUpdate tu) {
    if (tu.hasVehicle() && tu.getVehicle().hasId() && StringUtils.isNotBlank(tu.getVehicle().getId())) {
      if (_useLabelAsVehicleId && tu.hasVehicle() && tu.getVehicle().hasLabel())
        return tu.getVehicle().getLabel();
      return tu.getVehicle().getId();
    }
    if (tu.hasTrip() && tu.getTrip().hasExtension(GtfsRealtimeNYCT.nyctTripDescriptor)) {
      GtfsRealtimeNYCT.NyctTripDescriptor nyctTripDescriptor = tu.getTrip().getExtension(GtfsRealtimeNYCT.nyctTripDescriptor);
      if (nyctTripDescriptor.hasTrainId())
        return nyctTripDescriptor.getTrainId();
    }
    return null; // we can't find anything resembling a vehicleId
  }
  private String getVehicleId(VehiclePosition vp) {
    if (_useLabelAsVehicleId && vp.hasVehicle() && vp.getVehicle().hasLabel())
      return vp.getVehicle().getLabel();
    return vp.getVehicle().getId();
  }

    public VehicleOccupancyRecord createVehicleOccupancyRecordForUpdate(MonitoredResult result,
                                                                        CombinedTripUpdatesAndVehiclePosition update) {
      // called once per block -- we do not validate if the trip is active
      if (update == null) return null;
      if (update.vehiclePosition == null) return null;
      if (update.vehiclePosition.hasOccupancyStatus()) {
        VehicleOccupancyRecord vor = initalizeVehicleOccupancyRecord(update);
        try {
          vor.setOccupancyStatus(OccupancyStatus.valueOf(update.vehiclePosition.getOccupancyStatus().name()));
        } catch (IllegalArgumentException iae) {
          _log.debug("unknown occupancy value: " + iae);
        }

        if (vor.getOccupancyStatus() == null) {
          // the valueOf failed to match, the spec may have added new fields...
          _log.warn("unmatched occupancy status " + update.vehiclePosition.getOccupancyStatus().name());
          return null;
        }
        return vor;
      }

      // do we have crowding_descriptor?
      if (update.vehiclePosition.hasExtension(GtfsRealtimeCrowding.crowdingDescriptor)) {
        GtfsRealtimeCrowding.CrowdingDescriptor crowdingDescriptor = update.vehiclePosition.getExtension(GtfsRealtimeCrowding.crowdingDescriptor);
        VehicleOccupancyRecord vor = initalizeVehicleOccupancyRecord(update);
        if (crowdingDescriptor.hasEstimatedCount()) {
          vor.setRawCount(crowdingDescriptor.getEstimatedCount());
        }
        if (crowdingDescriptor.hasEstimatedCapacity()) {
          vor.setCapacity(crowdingDescriptor.getEstimatedCapacity());
        }
        return vor;
      }

      return null;
    }

  private VehicleOccupancyRecord initalizeVehicleOccupancyRecord(CombinedTripUpdatesAndVehiclePosition update) {
    VehicleOccupancyRecord vor = new VehicleOccupancyRecord();
    // test if vehicle is fully qualified, if so use it as is
    try {
      vor.setVehicleId(AgencyAndIdLibrary.convertFromString(update.block.getVehicleId()));
    } catch (IllegalStateException ise) {
      // here we assume the vehicle's agency matches that of its block
      vor.setVehicleId(new AgencyAndId(update.block.getBlockInstance().getBlock().getBlock().getId().getAgencyId(), update.block.getVehicleId()));
    }
    TripEntry firstTrip = null;
    if (update.vehiclePosition.hasTrip() && update.vehiclePosition.getTrip().hasTripId()) {
      // use trip from VP, as the combined update may have many trips
      firstTrip = _entitySource.getTrip(update.vehiclePosition.getTrip().getTripId());
    }
    // fall back on trip from combined update
    if (firstTrip == null) {
      firstTrip = _entitySource.getTrip(update.getTripUpdates().get(0).getTrip().getTripId());
    }
    if (firstTrip != null && firstTrip.getRoute() != null) {
      // link this occupancy to route+direction so it will expire at end of trip
      vor.setRouteId(AgencyAndIdLibrary.convertToString(firstTrip.getRoute().getId()));
      vor.setDirectionId(firstTrip.getDirectionId());
    }

    return vor;
  }

  private static class BestScheduleDeviation {
    public int delta = Integer.MAX_VALUE;
    public int scheduleDeviation = 0;
    public boolean isInPast = true;
    public boolean tripUpdateHasDelay = false;
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
