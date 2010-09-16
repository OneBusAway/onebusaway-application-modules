package org.onebusaway.transit_data_federation.impl.realtime;

import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.shapes.DistanceTraveledShapePointIndex;
import org.onebusaway.transit_data_federation.impl.shapes.ShapePointIndex;
import org.onebusaway.transit_data_federation.impl.time.StopTimeSearchOperations;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeOp;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.ShapePointService;
import org.onebusaway.transit_data_federation.services.realtime.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.ScheduledBlockLocationService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScheduledBlockLocationServiceImpl implements
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
  public ScheduledBlockLocation getScheduledBlockPositionFromDistanceAlongBlock(
      List<StopTimeEntry> stopTimes, double distanceAlongBlock) {

    StopTimeOp stopTimeOp = StopTimeOp.DISTANCE_ALONG_BLOCK;

    int stopTimeIndex = StopTimeSearchOperations.searchForStopTime(stopTimes,
        distanceAlongBlock, stopTimeOp);

    // Are we out beyond our last stop-time?
    if (stopTimeIndex == stopTimes.size())
      return null;

    // Are we before out first stop-time?
    if (stopTimeIndex == 0) {
      StopTimeEntry stopTime = stopTimes.get(0);
      if (stopTime.getDistaceAlongBlock() == distanceAlongBlock)
        return getScheduledBlockLocationFromScheduleTimeAndStopTimeIndex(
            stopTimes, stopTime.getArrivalTime(), stopTimeIndex);
      else
        return null;
    }

    StopTimeEntry before = stopTimes.get(stopTimeIndex - 1);
    StopTimeEntry after = stopTimes.get(stopTimeIndex);

    double ratio = (distanceAlongBlock - before.getDistaceAlongBlock())
        / (after.getDistaceAlongBlock() - before.getDistaceAlongBlock());

    int scheduleTime = (int) (before.getDepartureTime() + (after.getArrivalTime() - before.getDepartureTime())
        * ratio);

    return getScheduledBlockLocationFromScheduleTimeAndStopTimeIndex(stopTimes,
        scheduleTime, stopTimeIndex);
  }

  @Override
  public ScheduledBlockLocation getScheduledBlockPositionFromScheduledTime(
      List<StopTimeEntry> stopTimes, int scheduleTime) {

    StopTimeOp stopTimeOp = StopTimeOp.DEPARTURE;

    int index = StopTimeSearchOperations.searchForStopTime(stopTimes,
        scheduleTime, stopTimeOp);

    return getScheduledBlockLocationFromScheduleTimeAndStopTimeIndex(stopTimes,
        scheduleTime, index);
  }

  /****
   * Private Methods
   ****/

  private ScheduledBlockLocation getScheduledBlockLocationFromScheduleTimeAndStopTimeIndex(
      List<StopTimeEntry> stopTimes, int scheduleTime, int stopTimeIndex) {

    // Did we have a direct hit?
    if (0 <= stopTimeIndex && stopTimeIndex < stopTimes.size()) {

      StopTimeEntry stopTime = stopTimes.get(stopTimeIndex);

      if (stopTime.getArrivalTime() <= scheduleTime
          && scheduleTime <= stopTime.getDepartureTime()) {

        StopEntry stop = stopTime.getStop();
        CoordinatePoint location = new CoordinatePoint(stop.getStopLat(),
            stop.getStopLon());
        ScheduledBlockLocation result = new ScheduledBlockLocation();
        result.setLocation(location);
        result.setClosestStop(stopTime);
        result.setClosestStopTimeOffset(0);
        result.setScheduledTime(scheduleTime);
        result.setDistanceAlongBlock(stopTime.getDistaceAlongBlock());
        result.setActiveTrip(stopTime.getTrip());
        return result;
      }
    }

    if (stopTimeIndex == 0 || stopTimeIndex == stopTimes.size()) {
      // Out of bounds for these stop times
      return null;
    }

    StopTimeEntry before = stopTimes.get(stopTimeIndex - 1);
    StopTimeEntry after = stopTimes.get(stopTimeIndex);

    ScheduledBlockLocation result = new ScheduledBlockLocation();
    result.setScheduledTime(scheduleTime);
    
    int fromTime = before.getDepartureTime();
    int toTime = after.getArrivalTime();

    int fromTimeOffset = fromTime - scheduleTime;
    int toTimeOffset = toTime - scheduleTime;

    if (Math.abs(fromTimeOffset) < Math.abs(toTimeOffset)) {
      result.setClosestStop(before);
      result.setClosestStopTimeOffset(fromTimeOffset);
    } else {
      result.setClosestStop(after);
      result.setClosestStopTimeOffset(toTimeOffset);
    }

    double ratio = (scheduleTime - fromTime) / ((double) (toTime - fromTime));

    double fromDistance = before.getTrip().getDistanceAlongBlock()
        + before.getShapeDistTraveled();
    double toDistance = after.getTrip().getDistanceAlongBlock()
        + after.getShapeDistTraveled();

    double distanceAlongBlock = ratio * (toDistance - fromDistance)
        + fromDistance;
    result.setDistanceAlongBlock(distanceAlongBlock);

    // Are we between trips?
    if (!before.getTrip().equals(after.getTrip())) {
      if (distanceAlongBlock >= after.getTrip().getDistanceAlongBlock())
        result.setActiveTrip(after.getTrip());
      else
        result.setActiveTrip(before.getTrip());
    } else {
      result.setActiveTrip(before.getTrip());
    }

    TripEntry activeTrip = result.getActiveTrip();

    AgencyAndId shapeId = activeTrip.getShapeId();

    // Do we have enough information to use shape distance traveled?
    if (shapeId != null) {

      ShapePoints shapePoints = _shapePointService.getShapePointsForShapeId(shapeId);

      if (shapePoints != null && !shapePoints.isEmpty()) {
        double distance = distanceAlongBlock
            - activeTrip.getDistanceAlongBlock();
        ShapePointIndex shapePointIndexMethod = new DistanceTraveledShapePointIndex(
            distance);
        CoordinatePoint location = shapePointIndexMethod.getPoint(shapePoints);
        result.setLocation(location);
        return result;
      }
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

    return result;
  }

}
