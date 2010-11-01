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
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
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
      List<BlockStopTimeEntry> stopTimes, double distanceAlongBlock) {

    if (distanceAlongBlock < 0)
      return null;

    int stopTimeIndex = GenericBinarySearch.search(stopTimes,
        distanceAlongBlock,
        BlockStopTimeDistanceAlongBlockValueAdapter.INSTANCE);

    // Are we out beyond our last stop-time?
    if (stopTimeIndex == stopTimes.size()) {

      // If we only have one stop time, we can't interpolate the schedule time
      if (stopTimes.size() == 1)
        return null;

      BlockStopTimeEntry blockFrom = stopTimes.get(stopTimes.size() - 2);
      BlockStopTimeEntry blockTo = stopTimes.get(stopTimes.size() - 1);
      return interpolateLocation(blockFrom, blockTo, distanceAlongBlock);
    }

    // Are we before out first stop-time?
    if (stopTimeIndex == 0) {

      // If we only have one stop time, we can't interpolate the schedule time
      if (stopTimes.size() == 1)
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
      List<BlockStopTimeEntry> stopTimes, int scheduleTime) {

    int index = GenericBinarySearch.search(stopTimes, scheduleTime,
        BlockStopTimeDepartureTimeValueAdapter.INSTANCE);

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

      if (stopTime.getArrivalTime() <= scheduleTime
          && scheduleTime <= stopTime.getDepartureTime()) {

        StopEntry stop = stopTime.getStop();

        ScheduledBlockLocation result = new ScheduledBlockLocation();

        PointAndOrientation po = getLocationAlongShape(blockStopTime.getTrip(),
            blockStopTime.getDistaceAlongBlock());
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
        return result;
      }
    }

    if (stopTimeIndex == 0 || stopTimeIndex == stopTimes.size()) {
      // Out of bounds for these stop times
      return null;
    }

    BlockStopTimeEntry blockBefore = stopTimes.get(stopTimeIndex - 1);
    BlockStopTimeEntry blockAfter = stopTimes.get(stopTimeIndex);

    StopTimeEntry before = blockBefore.getStopTime();
    StopTimeEntry after = blockAfter.getStopTime();

    ScheduledBlockLocation result = new ScheduledBlockLocation();
    result.setScheduledTime(scheduleTime);

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

    // Are we between trips? Where is the transition point?
    if (!before.getTrip().equals(after.getTrip())) {
      if (distanceAlongBlock >= blockAfter.getTrip().getDistanceAlongBlock()) {
        result.setActiveTrip(blockAfter.getTrip());
      } else {
        result.setActiveTrip(blockBefore.getTrip());
      }
    } else {
      result.setActiveTrip(blockBefore.getTrip());
    }

    BlockTripEntry activeTrip = result.getActiveTrip();

    PointAndOrientation po = getLocationAlongShape(activeTrip,
        distanceAlongBlock);

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

  private ScheduledBlockLocation interpolateLocation(
      BlockStopTimeEntry blockFrom, BlockStopTimeEntry blockTo,
      double distanceAlongBlock) {

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
    if (r <= 0)
      nextStop = blockFrom;
    else if (r <= 1.0)
      nextStop = blockTo;

    ScheduledBlockLocation location = new ScheduledBlockLocation();
    location.setActiveTrip(activeTrip);
    location.setClosestStop(closestStop);
    location.setClosestStopTimeOffset(closestStop.getStopTime().getArrivalTime()
        - scheduledTime);
    location.setNextStop(nextStop);
    if (nextStop != null)
      location.setNextStopTimeOffset(nextStop.getStopTime().getArrivalTime()
          - scheduledTime);
    location.setDistanceAlongBlock(distanceAlongBlock);
    location.setScheduledTime(scheduledTime);
    // In this case, distance along block and distance along trip are the same
    // because we are still in the first trip of the block

    PointAndOrientation po = getLocationAlongShape(activeTrip,
        distanceAlongBlock);
    if (po != null) {
      location.setLocation(po.getPoint());
      location.setOrientation(po.getOrientation());
    }

    return location;
  }

  private PointAndOrientation getLocationAlongShape(
      BlockTripEntry activeBlockTrip, double distanceAlongBlock) {

    TripEntry activeTrip = activeBlockTrip.getTrip();
    AgencyAndId shapeId = activeTrip.getShapeId();

    if (shapeId == null)
      return null;

    ShapePoints shapePoints = _shapePointService.getShapePointsForShapeId(shapeId);

    if (shapePoints == null || shapePoints.isEmpty())
      return null;

    double distanceAlongTrip = distanceAlongBlock
        - activeBlockTrip.getDistanceAlongBlock();

    ShapePointIndex shapePointIndexMethod = new DistanceTraveledShapePointIndex(
        distanceAlongTrip);
    return shapePointIndexMethod.getPointAndOrientation(shapePoints);
  }
}
