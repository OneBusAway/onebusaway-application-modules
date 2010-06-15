package org.onebusaway.transit_data_federation.impl;

import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.shapes.DistanceTraveledShapePointIndex;
import org.onebusaway.transit_data_federation.impl.shapes.ShapePointIndex;
import org.onebusaway.transit_data_federation.impl.time.StopTimeSearchOperations;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeOp;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.TripPosition;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.model.predictions.ScheduleDeviation;
import org.onebusaway.transit_data_federation.model.predictions.TripTimePrediction;
import org.onebusaway.transit_data_federation.services.ShapePointService;
import org.onebusaway.transit_data_federation.services.TripPositionService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.predictions.TripTimePredictionService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripInstanceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TripPositionServiceImpl implements TripPositionService {

  private TripTimePredictionService _tripTimePredictionService;

  private NarrativeService _narrativeService;

  private ShapePointService _shapePointService;

  private boolean _interpolateWhenNoShapeInfoPresent = true;

  @Autowired
  public void setTripTimePredictionService(
      TripTimePredictionService tripTimePredictionService) {
    _tripTimePredictionService = tripTimePredictionService;
  }

  @Autowired
  public void setNarrativeService(NarrativeService narrativeService) {
    _narrativeService = narrativeService;
  }

  @Autowired
  public void setShapePointService(ShapePointService shapePointService) {
    _shapePointService = shapePointService;
  }

  public void setInterpolateWhenNoShapeInfoPresent(
      boolean interpolateWhenNoShapeInfoPresent) {
    _interpolateWhenNoShapeInfoPresent = interpolateWhenNoShapeInfoPresent;
  }

  public TripPosition getPositionForTripInstance(
      TripInstanceProxy tripInstance, long targetTime) {

    ScheduleDeviation scheduleDeviation = _tripTimePredictionService.getScheduledDeviationPrediction(
        tripInstance.getTrip().getId(), tripInstance.getServiceDate(),
        targetTime);

    return getTripPosition(tripInstance, scheduleDeviation, targetTime);
  }

  @Override
  public TripPosition getPositionForTripInstance(
      TripInstanceProxy tripInstance, TripTimePrediction prediction,
      long targetTime) {

    ScheduleDeviation scheduleDeviation = new ScheduleDeviation();
    scheduleDeviation.setPredicted(true);
    scheduleDeviation.setScheduleDeviation(prediction.getScheduleDeviation());
    scheduleDeviation.setVehicleId(prediction.getVehicleId());

    return getTripPosition(tripInstance, scheduleDeviation, targetTime);
  }

  /****
   * Private Methods
   ****/

  private TripPosition getTripPosition(TripInstanceProxy tripInstance,
      ScheduleDeviation scheduleDeviation, long targetTime) {
    
    TripEntry tripEntry = tripInstance.getTrip();
    long serviceDate = tripInstance.getServiceDate();

    TripPosition position = new TripPosition();

    position.setScheduleDeviation(scheduleDeviation);

    int effectiveScheduledTime = (int) ((targetTime - serviceDate) / 1000)
        - scheduleDeviation.getScheduleDeviation();
    List<StopTimeEntry> stopTimes = tripEntry.getStopTimes();
    StopTimeOp stopTimeOp = StopTimeOp.DEPARTURE;

    int index = StopTimeSearchOperations.searchForStopTime(stopTimes,
        effectiveScheduledTime, stopTimeOp);

    // Did we have a direct hit?
    if (0 <= index && index < stopTimes.size()) {
      StopTimeEntry stopTime = stopTimes.get(index);
      if (stopTime.getArrivalTime() <= effectiveScheduledTime
          && effectiveScheduledTime <= stopTime.getDepartureTime()) {
        StopEntry stop = stopTime.getStop();
        CoordinatePoint location = new CoordinatePoint(stop.getStopLat(),
            stop.getStopLon());
        position.setPosition(location);
        position.setClosestStop(stopTime);
        position.setClosestStopTimeOffset(0);
        return position;
      }
    }

    if (index == 0 || index == stopTimes.size()) {
      // Out of bounds for this trip
      return null;
    }

    StopTimeEntry before = stopTimes.get(index - 1);
    StopTimeEntry after = stopTimes.get(index);

    // Right now, StopTime shapeDistanceTraveled is not stored in the
    // memory-resident StopTimeEntry
    StopTimeNarrative beforeStopTime = _narrativeService.getStopTimeForEntry(before);
    StopTimeNarrative afterStopTime = _narrativeService.getStopTimeForEntry(after);
    TripNarrative tripNarrative = _narrativeService.getTripForId(tripEntry.getId());

    AgencyAndId shapeId = tripNarrative.getShapeId();

    int fromTime = before.getDepartureTime();
    int toTime = after.getArrivalTime();
    
    int fromTimeOffset = fromTime - effectiveScheduledTime;
    int toTimeOffset = toTime - effectiveScheduledTime;
    
    if( Math.abs(fromTimeOffset) < Math.abs(toTimeOffset)) {
      position.setClosestStop(before);
      position.setClosestStopTimeOffset(fromTimeOffset);
    }
    else {
      position.setClosestStop(after);
      position.setClosestStopTimeOffset(toTimeOffset);
    }

    double ratio = (effectiveScheduledTime - fromTime)
        / ((double) (toTime - fromTime));

    // Do we have enough information to use shape distance traveled?
    if (shapeId != null && beforeStopTime.getShapeDistTraveled() >= 0
        && afterStopTime.getShapeDistTraveled() >= 0) {

      ShapePoints shapePoints = _shapePointService.getShapePointsForShapeId(shapeId);

      if (!shapePoints.isEmpty()) {
        ShapePointIndex shapePointIndexMethod = getShapeDistanceTraveled(
            beforeStopTime, afterStopTime, ratio);
        CoordinatePoint location = shapePointIndexMethod.getPoint(shapePoints);
        position.setPosition(location);
        return position;
      }
    }

    if (!_interpolateWhenNoShapeInfoPresent)
      return null;

    StopEntry beforeStop = before.getStop();
    StopEntry afterStop = after.getStop();
    double latFrom = beforeStop.getStopLat();
    double lonFrom = beforeStop.getStopLon();
    double latTo = afterStop.getStopLat();
    double lonTo = afterStop.getStopLon();
    double lat = (latTo - latFrom) * ratio + latFrom;
    double lon = (lonTo - lonFrom) * ratio + lonFrom;

    CoordinatePoint location = new CoordinatePoint(lat, lon);
    position.setPosition(location);
    return position;
  }

  private ShapePointIndex getShapeDistanceTraveled(
      StopTimeNarrative beforeStopTime, StopTimeNarrative afterStopTime,
      double ratio) {

    double fromDistance = beforeStopTime.getShapeDistTraveled();
    double toDistance = afterStopTime.getShapeDistTraveled();

    double distance = ratio * (toDistance - fromDistance) + fromDistance;
    return new DistanceTraveledShapePointIndex(distance);
  }
}
