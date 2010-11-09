package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.shapes.DistanceTraveledShapePointIndex;
import org.onebusaway.transit_data_federation.impl.shapes.PointAndOrientation;
import org.onebusaway.transit_data_federation.impl.shapes.ShapePointIndex;
import org.onebusaway.transit_data_federation.impl.time.GenericBinarySearch;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.ShapePointService;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocationService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.utility.InterpolationLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ScheduledBlockLocationServiceImpl implements
    ScheduledBlockLocationService {

  private ShapePointService _shapePointService;

  @Autowired
  public void setShapePointService(ShapePointService shapePointService) {
    _shapePointService = shapePointService;
  }

  /****
   * {@link ScheduledBlockLocationService} Interface
   ****/

  @Override
  public ScheduledBlockLocation getScheduledBlockLocationFromDistanceAlongBlock(
      BlockConfigurationEntry blockConfig, double distanceAlongBlock) {

    if (distanceAlongBlock < 0.0
        || distanceAlongBlock > blockConfig.getTotalBlockDistance())
      return null;

    List<BlockStopTimeEntry> stopTimes = blockConfig.getStopTimes();
    int n = stopTimes.size();

    int stopTimeIndex = GenericBinarySearch.search(blockConfig, n,
        distanceAlongBlock, BlockConfigDistanceAlongBlockIndexAdapter.INSTANCE);

    // Are we out beyond our last stop-time?
    if (stopTimeIndex == n) {

      // If we only have one stop time, we can't interpolate the schedule time
      if (n == 1)
        return null;

      BlockStopTimeEntry blockFrom = stopTimes.get(n - 2);
      BlockStopTimeEntry blockTo = stopTimes.get(n - 1);
      return interpolateLocation(blockFrom, blockTo, distanceAlongBlock);
    }

    // Are we before out first stop-time?
    if (stopTimeIndex == 0) {

      // If we only have one stop time, we can't interpolate the schedule time
      if (n == 1)
        return null;

      BlockStopTimeEntry blockFrom = stopTimes.get(0);
      BlockStopTimeEntry blockTo = stopTimes.get(1);

      return interpolateLocation(blockFrom, blockTo, distanceAlongBlock);
    }

    BlockStopTimeEntry blockBefore = stopTimes.get(stopTimeIndex - 1);
    BlockStopTimeEntry blockAfter = stopTimes.get(stopTimeIndex);

    StopTimeEntry before = blockBefore.getStopTime();
    StopTimeEntry after = blockAfter.getStopTime();

    double ratio = (distanceAlongBlock - blockBefore.getDistaceAlongBlock())
        / (blockAfter.getDistaceAlongBlock() - blockBefore.getDistaceAlongBlock());

    int scheduleTime = (int) (before.getDepartureTime() + (after.getArrivalTime() - before.getDepartureTime())
        * ratio);

    return getScheduledBlockLocationFromScheduleTimeAndStopTimeIndex(stopTimes,
        scheduleTime, stopTimeIndex);
  }

  @Override
  public ScheduledBlockLocation getScheduledBlockLocationFromScheduledTime(
      BlockConfigurationEntry blockConfig, int scheduleTime) {

    List<BlockStopTimeEntry> stopTimes = blockConfig.getStopTimes();
    int n = stopTimes.size();
    int index = GenericBinarySearch.search(blockConfig, n, scheduleTime,
        BlockConfigDepartureTimeIndexAdapter.INSTANCE);

    return getScheduledBlockLocationFromScheduleTimeAndStopTimeIndex(stopTimes,
        scheduleTime, index);
  }

  /****
   * Private Methods
   ****/

  private ScheduledBlockLocation getScheduledBlockLocationFromScheduleTimeAndStopTimeIndex(
      List<BlockStopTimeEntry> stopTimes, int scheduleTime, int stopTimeIndex) {

    // Did we have a direct hit?
    if (0 <= stopTimeIndex && stopTimeIndex < stopTimes.size()) {

      BlockStopTimeEntry blockStopTime = stopTimes.get(stopTimeIndex);
      StopTimeEntry stopTime = blockStopTime.getStopTime();

      /**
       * Is the vehicle currently at a layover at the stop?
       */
      if (stopTime.getArrivalTime() <= scheduleTime
          && scheduleTime <= stopTime.getDepartureTime()) {

        return getScheduledBlockLocationWhenAtStopTime(blockStopTime, stopTime,
            scheduleTime);
      }
    }

    /**
     * If the stopTimeIndex is zero, and we weren't at a layover at the first
     * block stop time (see above), then we are looking for the scheduled
     * location before the scheduled start of the block
     */
    if (stopTimeIndex == 0) {
      return getScheduledBlockLocationBeforeStartOfBlock(stopTimes,
          scheduleTime, stopTimeIndex);
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

  private ScheduledBlockLocation getScheduledBlockLocationWhenAtStopTime(
      BlockStopTimeEntry blockStopTime, StopTimeEntry stopTime, int scheduleTime) {
    StopEntry stop = stopTime.getStop();

    ScheduledBlockLocation result = new ScheduledBlockLocation();

    int shapePointIndex = stopTime.getShapePointIndex();

    PointAndOrientation po = getLocationAlongShape(blockStopTime.getTrip(),
        blockStopTime.getDistaceAlongBlock(), shapePointIndex,
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
    result.setDistanceAlongBlock(blockStopTime.getDistaceAlongBlock());
    result.setActiveTrip(blockStopTime.getTrip());
    result.setInService(true);
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

    result.setNextStop(blockAfter);
    result.setNextStopTimeOffset(toTimeOffset);

    double ratio = (scheduleTime - fromTime) / ((double) (toTime - fromTime));

    double fromDistance = blockBefore.getDistaceAlongBlock();
    double toDistance = blockAfter.getDistaceAlongBlock();

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

  private ScheduledBlockLocation getScheduledBlockLocationBeforeStartOfBlock(
      List<BlockStopTimeEntry> stopTimes, int scheduleTime, int stopTimeIndex) {

    BlockStopTimeEntry blockStopTime = stopTimes.get(stopTimeIndex);
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
          stopTime.getDepartureTime(), blockStopTime.getDistaceAlongBlock(),
          secondStopTime.getArrivalTime(),
          secondBlockStopTime.getDistaceAlongBlock(), scheduleTime);

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
    result.setNextStop(blockStopTime);
    result.setNextStopTimeOffset(stopTime.getArrivalTime() - scheduleTime);
    result.setScheduledTime(scheduleTime);
    result.setDistanceAlongBlock(distanceAlongBlock);
    result.setActiveTrip(blockStopTime.getTrip());
    result.setInService(inService);
    return result;
  }

  private ScheduledBlockLocation interpolateLocation(
      BlockStopTimeEntry blockFrom, BlockStopTimeEntry blockTo,
      double distanceAlongBlock) {

    if (distanceAlongBlock < 0.0)
      return null;

    StopTimeEntry from = blockFrom.getStopTime();
    StopTimeEntry to = blockTo.getStopTime();

    double r = (distanceAlongBlock - blockFrom.getDistaceAlongBlock())
        / (blockTo.getDistaceAlongBlock() - blockFrom.getDistaceAlongBlock());
    int scheduledTime = (int) (r
        * (to.getArrivalTime() - from.getDepartureTime()) + from.getDepartureTime());

    if (r > 1)
      scheduledTime += to.getSlackTime();

    BlockTripEntry activeTrip = distanceAlongBlock < blockTo.getDistaceAlongBlock()
        ? blockFrom.getTrip() : blockTo.getTrip();

    BlockStopTimeEntry closestStop = r < 0.5 ? blockFrom : blockTo;

    BlockStopTimeEntry nextStop = null;
    int shapePointIndexFrom = -1;
    int shapePointIndexTo = -1;

    if (r <= 0) {

      /**
       * Location along the block is before the two stop times
       */
      nextStop = blockFrom;
      shapePointIndexFrom = 0;
      shapePointIndexTo = nextShapePointIndex(from);

    } else if (r <= 1.0) {

      /**
       * Location along the block is between the two stop times
       */
      nextStop = blockTo;
      shapePointIndexFrom = from.getShapePointIndex();
      shapePointIndexTo = nextShapePointIndex(to);

    } else {

      /**
       * Location along the block is after the two stop times
       */
      shapePointIndexFrom = to.getShapePointIndex();
      shapePointIndexTo = Integer.MAX_VALUE;
    }

    ScheduledBlockLocation location = new ScheduledBlockLocation();
    location.setActiveTrip(activeTrip);
    location.setClosestStop(closestStop);
    location.setClosestStopTimeOffset(closestStop.getStopTime().getArrivalTime()
        - scheduledTime);
    location.setNextStop(nextStop);
    if (nextStop != null)
      location.setNextStopTimeOffset(nextStop.getStopTime().getArrivalTime()
          - scheduledTime);
    location.setInService(nextStop != null);
    location.setDistanceAlongBlock(distanceAlongBlock);
    location.setScheduledTime(scheduledTime);

    /**
     * In this case, distance along block and distance along trip are the same
     * because we are still in the first trip of the block
     */

    PointAndOrientation po = getLocationAlongShape(activeTrip,
        distanceAlongBlock, shapePointIndexFrom, shapePointIndexTo);

    if (po != null) {
      location.setLocation(po.getPoint());
      location.setOrientation(po.getOrientation());
    }

    return location;
  }

  private int nextShapePointIndex(StopTimeEntry stopTime) {
    int index = stopTime.getShapePointIndex();
    if (index != -1)
      index++;
    return index;
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
    shapePointIndexTo = Math.min(shapePointIndexTo, shapePoints.getSize());

    double distanceAlongTrip = distanceAlongBlock
        - activeBlockTrip.getDistanceAlongBlock();

    ShapePointIndex shapePointIndexMethod = new DistanceTraveledShapePointIndex(
        distanceAlongTrip, shapePointIndexFrom, shapePointIndexTo);
    return shapePointIndexMethod.getPointAndOrientation(shapePoints);
  }
}
