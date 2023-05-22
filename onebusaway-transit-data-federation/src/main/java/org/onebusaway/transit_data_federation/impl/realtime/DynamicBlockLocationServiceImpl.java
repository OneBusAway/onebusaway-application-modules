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

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.impl.blocks.IndexAdapters;
import org.onebusaway.transit_data_federation.impl.shapes.DistanceTraveledShapePointIndex;
import org.onebusaway.transit_data_federation.impl.shapes.PointAndOrientation;
import org.onebusaway.transit_data_federation.impl.shapes.ShapePointIndex;
import org.onebusaway.transit_data_federation.impl.time.GenericBinarySearch;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.DynamicBlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.DynamicBlockLocationService;
import org.onebusaway.transit_data_federation.services.shapes.ShapePointService;
import org.onebusaway.transit_data_federation.services.transit_graph.*;
import org.onebusaway.utility.InterpolationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.onebusaway.transit_data_federation.impl.realtime.DynamicBlockIndexServiceImpl.CACHE_TIMEOUT;

@Component
/**
 * Counterpart to (Static)BlockLocationService, handling dynamic trips
 * ( ADDED / DUPLICATED )
 * TODO: the location logic is copied from StaticBlockLocationServiceImpl -- find a way
 * to refactor into a common location service.
 */
public class DynamicBlockLocationServiceImpl implements DynamicBlockLocationService {

  private static Logger _log = LoggerFactory.getLogger(DynamicBlockLocationServiceImpl.class);

  private Map<AgencyAndId, BlockLocation> _blockIdToBlockLocation = new PassiveExpiringMap<>(CACHE_TIMEOUT);
  private Map<AgencyAndId, RecordAndLocation> _vehicleIdToRecordAndLocation = new PassiveExpiringMap<>(CACHE_TIMEOUT);

  @Autowired
  @Qualifier("dynamicBlockIndexServiceImpl")
  private DynamicBlockIndexService _blockIndexService;
  @Autowired
  private ShapePointService _shapePointService;

  @Override
  public void register(BlockLocation blockLocation) {
    AgencyAndId blockId = blockLocation.getBlockInstance().getBlock().getBlock().getId();
    _blockIdToBlockLocation.put(blockId, blockLocation);
  }

  @Override
  public BlockLocation getLocationForBlockInstance(BlockInstance blockInstance, TargetTime time) {
    // this is a 1-1, much simpler than the static configuration
    return _blockIdToBlockLocation.get(blockInstance.getBlock().getBlock().getId());
  }

  @Override
  public void handleVehicleLocationRecord(VehicleLocationRecord record) {
    // whoops: how are we going to look up a block location for a dynamic trip???
    BlockInstance blockInstance = _blockIndexService.getDynamicBlockInstance(record.getBlockId());
    if (blockInstance == null) {
      _log.warn("unknown blockId={}", record.getBlockId());
      return;
    }

    ScheduledBlockLocation scheduledBlockLocation = getScheduledBlockLocationForVehicleLocationRecord(
            record, blockInstance);

    putBlockLocationRecord(blockInstance, record, scheduledBlockLocation);

  }

  @Override
  public void handleVehicleLocationRecords(List<VehicleLocationRecord> records) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void resetVehicleLocation(AgencyAndId vehicleId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void handleRawPosition(AgencyAndId vehicleId, double lat, double lon, long timestamp) {
    throw new UnsupportedOperationException();
  }

  private void putBlockLocationRecord(BlockInstance blockInstance, VehicleLocationRecord record, ScheduledBlockLocation scheduledBlockLocation) {
    if (record.getVehicleId() != null) {
      _vehicleIdToRecordAndLocation.put(record.getVehicleId(), new RecordAndLocation(record, scheduledBlockLocation));
    }
  }

  private ScheduledBlockLocation getScheduledBlockLocationForVehicleLocationRecord(VehicleLocationRecord record, BlockInstance blockInstance) {
    // we don't have a specified time, so use the timeOfRecord to see if the trip is
    // active now
    BlockConfigurationEntry blockConfig = blockInstance.getBlock();
    long serviceDate = blockInstance.getServiceDate();

    long targetTime = record.getTimeOfRecord();

    int scheduledTime = (int) ((targetTime - serviceDate) / 1000);
    int effectiveScheduledTime = (int) (scheduledTime - record.getScheduleDeviation());
    ScheduledBlockLocation scheduledBlockLocation = getScheduledBlockLocationFromScheduledTime(blockConfig, effectiveScheduledTime);
    return scheduledBlockLocation;

  }

  public ScheduledBlockLocation getScheduledBlockLocationFromScheduledTime(
          BlockConfigurationEntry blockConfig, int scheduleTime) {
    List<BlockStopTimeEntry> stopTimes = blockConfig.getStopTimes();
    if (stopTimes == null || stopTimes.isEmpty()) {
      _log.warn("no stop times for block={}", blockConfig.getBlock().getId());
      return null;
    }
    int n = stopTimes.size();
    int index = GenericBinarySearch.search(blockConfig, n, scheduleTime,
            IndexAdapters.BLOCK_STOP_TIME_DEPARTURE_INSTANCE);
    return getScheduledBlockLocationFromScheduleTimeAndStopTimeIndex(stopTimes,
            scheduleTime, index);

  }

  private ScheduledBlockLocation getScheduledBlockLocationFromScheduleTimeAndStopTimeIndex(
          List<BlockStopTimeEntry> stopTimes, int scheduleTime, int stopTimeIndex) {

    // Did we have a direct hit?
    if (0 <= stopTimeIndex && stopTimeIndex < stopTimes.size()) {

      BlockStopTimeEntry blockStopTime = stopTimes.get(stopTimeIndex);
      StopTimeEntry stopTime = blockStopTime.getStopTime();
      BlockStopTimeEntry previousBlockStopTime = null;

      if (stopTimeIndex > 0) {
        previousBlockStopTime = stopTimes.get(stopTimeIndex - 1);
      }

      /**
       * Is the vehicle currently at a layover at the stop?
       */
      if (stopTime.getArrivalTime() <= scheduleTime
              && scheduleTime <= stopTime.getDepartureTime()) {

        return getScheduledBlockLocationWhenAtStopTime(blockStopTime, previousBlockStopTime,
                stopTime, scheduleTime, stopTimeIndex);
      }
    }
    /**
     * If the stopTimeIndex is zero, and we weren't at a layover at the first
            * block stop time (see above), then we are looking for the scheduled
     * location before the scheduled start of the block
     */
    if (stopTimeIndex == 0) {
      return getScheduledBlockLocationBeforeStartOfBlock(stopTimes,
              scheduleTime);
    }
    /**
     * If the stopTimeIndex is beyond the last stop time, we don't attempt to
     * determine a scheduled location, since we're beyond the end of the block
     */
    if (stopTimeIndex == stopTimes.size()) {
      // Out of bounds for these stop times
      return null;
    }

    return getScheduledBlockLocationBetweenStopTimes(stopTimes, scheduleTime,
            stopTimeIndex);

  }
    private ScheduledBlockLocation getScheduledBlockLocationWhenAtStopTime (
            BlockStopTimeEntry blockStopTime, BlockStopTimeEntry previousBlockStopTime,
            StopTimeEntry stopTime,int scheduleTime, int stopTimeIndex){
      StopEntry stop = stopTime.getStop();

      ScheduledBlockLocation result = new ScheduledBlockLocation();

      int shapePointIndex = stopTime.getShapePointIndex();

      PointAndOrientation po = getLocationAlongShape(blockStopTime.getTrip(),
              blockStopTime.getDistanceAlongBlock(), shapePointIndex,
              shapePointIndex + 1);
      if (po != null) {
        result.setLocation(po.getPoint());
        result.setOrientation(po.getOrientation());
      } else {
        CoordinatePoint location = new CoordinatePoint(stop.getStopLat(),
                stop.getStopLon());
        result.setLocation(location);
        result.setOrientation(0);
      }

      result.setClosestStop(blockStopTime);
      result.setClosestStopTimeOffset(0);
      result.setNextStop(blockStopTime);
      result.setNextStopTimeOffset(0);
      result.setScheduledTime(scheduleTime);
      result.setDistanceAlongBlock(blockStopTime.getDistanceAlongBlock());
      result.setActiveTrip(blockStopTime.getTrip());
      result.setInService(true);
      result.setStopTimeIndex(stopTimeIndex);

      // If there is more than 1 stop, grab the previous stop
      if (blockStopTime.hasPreviousStop()) {
        result.setPreviousStop(previousBlockStopTime);
      }

      return result;
    }

  private ScheduledBlockLocation getScheduledBlockLocationBeforeStartOfBlock(
          List<BlockStopTimeEntry> stopTimes, int scheduleTime) {

    /**
     * The first block stop time
     */
    BlockStopTimeEntry blockStopTime = stopTimes.get(0);
    StopTimeEntry stopTime = blockStopTime.getStopTime();

    double distanceAlongBlock = Double.NaN;
    boolean inService = false;

    /**
     * If we have more than one stop time in the block (we'd hope!), then we
     * attempt to interpolate the distance along the block
     */
    if (stopTimes.size() > 1) {

      BlockStopTimeEntry secondBlockStopTime = stopTimes.get(1);
      StopTimeEntry secondStopTime = secondBlockStopTime.getStopTime();

      distanceAlongBlock = InterpolationLibrary.interpolatePair(
              stopTime.getDepartureTime(), blockStopTime.getDistanceAlongBlock(),
              secondStopTime.getArrivalTime(),
              secondBlockStopTime.getDistanceAlongBlock(), scheduleTime);

      if (distanceAlongBlock >= 0)
        inService = true;
      else
        distanceAlongBlock = 0.0;
    }

    PointAndOrientation po = null;

    if (!Double.isNaN(distanceAlongBlock))
      po = getLocationAlongShape(blockStopTime.getTrip(), distanceAlongBlock,
              0, nextShapePointIndex(stopTime));

    ScheduledBlockLocation result = new ScheduledBlockLocation();

    if (po != null) {
      result.setLocation(po.getPoint());
      result.setOrientation(po.getOrientation());
    }

    result.setClosestStop(blockStopTime);
    result.setClosestStopTimeOffset(stopTime.getArrivalTime() - scheduleTime);
    result.setPreviousStop(null);
    result.setNextStop(blockStopTime);
    result.setNextStopTimeOffset(stopTime.getArrivalTime() - scheduleTime);
    result.setScheduledTime(scheduleTime);
    result.setDistanceAlongBlock(distanceAlongBlock);
    result.setActiveTrip(blockStopTime.getTrip());
    result.setInService(inService);
    result.setStopTimeIndex(0);
    return result;
  }

  private ScheduledBlockLocation getScheduledBlockLocationBetweenStopTimes(
          List<BlockStopTimeEntry> stopTimes, int scheduleTime, int stopTimeIndex) {

    BlockStopTimeEntry blockBefore = stopTimes.get(stopTimeIndex - 1);
    BlockStopTimeEntry blockAfter = stopTimes.get(stopTimeIndex);

    StopTimeEntry before = blockBefore.getStopTime();
    StopTimeEntry after = blockAfter.getStopTime();

    ScheduledBlockLocation result = new ScheduledBlockLocation();
    result.setScheduledTime(scheduleTime);
    result.setInService(true);
    result.setStopTimeIndex(stopTimeIndex);

    int fromTime = before.getDepartureTime();
    int toTime = after.getArrivalTime();

    int fromTimeOffset = fromTime - scheduleTime;
    int toTimeOffset = toTime - scheduleTime;

    if (Math.abs(fromTimeOffset) < Math.abs(toTimeOffset)) {
      result.setClosestStop(blockBefore);
      result.setClosestStopTimeOffset(fromTimeOffset);
    } else {
      result.setClosestStop(blockAfter);
      result.setClosestStopTimeOffset(toTimeOffset);
    }
    result.setPreviousStop(blockBefore);
    result.setNextStop(blockAfter);
    result.setNextStopTimeOffset(toTimeOffset);

    double ratio = (scheduleTime - fromTime) / ((double) (toTime - fromTime));

    double fromDistance = blockBefore.getDistanceAlongBlock();
    double toDistance = blockAfter.getDistanceAlongBlock();

    double distanceAlongBlock = ratio * (toDistance - fromDistance)
            + fromDistance;

    result.setDistanceAlongBlock(distanceAlongBlock);

    int shapePointIndexFrom = -1;
    int shapePointIndexTo = -1;

    /**
     * Are we between trips? Where is the transition point?
     */
    if (!before.getTrip().equals(after.getTrip())) {

      if (distanceAlongBlock >= blockAfter.getTrip().getDistanceAlongBlock()) {
        result.setActiveTrip(blockAfter.getTrip());
        shapePointIndexFrom = 0;
        shapePointIndexTo = nextShapePointIndex(after);
      } else {
        result.setActiveTrip(blockBefore.getTrip());
        shapePointIndexFrom = before.getShapePointIndex();
        shapePointIndexTo = Integer.MAX_VALUE;
      }
    } else {
      result.setActiveTrip(blockBefore.getTrip());
      shapePointIndexFrom = before.getShapePointIndex();
      shapePointIndexTo = nextShapePointIndex(after);
    }

    BlockTripEntry activeTrip = result.getActiveTrip();

    PointAndOrientation po = getLocationAlongShape(activeTrip,
            distanceAlongBlock, shapePointIndexFrom, shapePointIndexTo);

    if (po != null) {
      result.setLocation(po.getPoint());
      result.setOrientation(po.getOrientation());
      return result;
    }

    StopEntry beforeStop = before.getStop();
    StopEntry afterStop = after.getStop();
    double latFrom = beforeStop.getStopLat();
    double lonFrom = beforeStop.getStopLon();
    double latTo = afterStop.getStopLat();
    double lonTo = afterStop.getStopLon();
    double lat = (latTo - latFrom) * ratio + latFrom;
    double lon = (lonTo - lonFrom) * ratio + lonFrom;

    CoordinatePoint location = new CoordinatePoint(lat, lon);
    result.setLocation(location);

    double orientation = SphericalGeometryLibrary.getOrientation(latFrom,
            lonFrom, latTo, lonTo);
    result.setOrientation(orientation);

    return result;
  }

  private PointAndOrientation getLocationAlongShape(
          BlockTripEntry activeBlockTrip, double distanceAlongBlock,
          int shapePointIndexFrom, int shapePointIndexTo) {

    TripEntry activeTrip = activeBlockTrip.getTrip();
    AgencyAndId shapeId = activeTrip.getShapeId();

    if (shapeId == null)
      return null;

    ShapePoints shapePoints = _shapePointService.getShapePointsForShapeId(shapeId);

    if (shapePoints == null || shapePoints.isEmpty())
      return null;

    /**
     * We allow callers of this method to specify an arbitrarily high
     * shapePointIndexTo, knowing we'll bound it by the max number of points
     */
    shapePointIndexFrom = Math.min(shapePointIndexFrom, shapePoints.getSize());
    shapePointIndexTo = Math.min(shapePointIndexTo, shapePoints.getSize());

    double distanceAlongTrip = distanceAlongBlock
            - activeBlockTrip.getDistanceAlongBlock();

    ShapePointIndex shapePointIndexMethod = new DistanceTraveledShapePointIndex(
            distanceAlongTrip, shapePointIndexFrom, shapePointIndexTo);
    return shapePointIndexMethod.getPointAndOrientation(shapePoints);
  }

  private int nextShapePointIndex(StopTimeEntry stopTime) {
    int index = stopTime.getShapePointIndex();
    if (index != -1)
      index++;
    return index;
  }

  private static class RecordAndLocation {
    private VehicleLocationRecord record;
    private ScheduledBlockLocation location;
    public RecordAndLocation(VehicleLocationRecord record, ScheduledBlockLocation location) {
      this.record = record;
      this.location = location;
    }
  }
}
