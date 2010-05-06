package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.shapes.DistanceTraveledShapePointIndex;
import org.onebusaway.transit_data_federation.impl.shapes.ShapePointIndex;
import org.onebusaway.transit_data_federation.impl.time.StopTimeSearchOperations;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeOp;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.ShapePointService;
import org.onebusaway.transit_data_federation.services.TripPositionService;
import org.onebusaway.transit_data_federation.services.narrative.StopTimeNarrativeService;
import org.onebusaway.transit_data_federation.services.narrative.TripNarrativeService;
import org.onebusaway.transit_data_federation.services.predictions.TripTimePredictionService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripInstanceProxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TripPositionServiceImpl implements TripPositionService {

  private TripTimePredictionService _tripTimePredictionService;

  private StopTimeNarrativeService _stopTimeNarrativeService;

  private TripNarrativeService _tripNarrativeService;

  private ShapePointService _shapePointService;

  private boolean _interpolateWhenNoShapeInfoPresent = true;

  @Autowired
  public void setTripTimePredictionService(
      TripTimePredictionService tripTimePredictionService) {
    _tripTimePredictionService = tripTimePredictionService;
  }

  @Autowired
  public void setStopTimeNarrativeService(
      StopTimeNarrativeService stopTimeNarrativeService) {
    _stopTimeNarrativeService = stopTimeNarrativeService;
  }

  @Autowired
  public void setTripNarrativeService(TripNarrativeService tripNarrativeService) {
    _tripNarrativeService = tripNarrativeService;
  }

  @Autowired
  public void setShapePointService(ShapePointService shapePointService) {
    _shapePointService = shapePointService;
  }
  
  public void setInterpolateWhenNoShapeInfoPresent(boolean interpolateWhenNoShapeInfoPresent) {
    _interpolateWhenNoShapeInfoPresent = interpolateWhenNoShapeInfoPresent;
  }

  public CoordinatePoint getPositionForTripInstance(
      TripInstanceProxy tripInstance, long targetTime) {

    TripEntry tripEntry = tripInstance.getTrip();
    long serviceDate = tripInstance.getServiceDate();
    int scheduleDeviation = _tripTimePredictionService.getScheduledDeviationPrediction(
        tripEntry.getId(), serviceDate, targetTime);

    int effectiveScheduledTime = (int) ((targetTime - serviceDate) / 1000) - scheduleDeviation;
    List<StopTimeEntry> stopTimes = tripEntry.getStopTimes();
    StopTimeOp stopTimeOp = StopTimeOp.DEPARTURE;

    int index = StopTimeSearchOperations.searchForStopTime(stopTimes, effectiveScheduledTime,
        stopTimeOp);

    // Did we have a direct hit?
    if (0 <= index && index < stopTimes.size()) {
      StopTimeEntry stopTime = stopTimes.get(index);
      if (stopTime.getArrivalTime() <= effectiveScheduledTime
          && effectiveScheduledTime <= stopTime.getDepartureTime()) {
        StopEntry stop = stopTime.getStop();
        return new CoordinatePoint(stop.getStopLat(), stop.getStopLon());
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
    StopTimeNarrative beforeStopTime = _stopTimeNarrativeService.getStopTimeForEntry(before);
    StopTimeNarrative afterStopTime = _stopTimeNarrativeService.getStopTimeForEntry(after);
    TripNarrative tripNarrative = _tripNarrativeService.getTripForId(tripEntry.getId());

    AgencyAndId shapeId = tripNarrative.getShapeId();

    int fromTime = before.getDepartureTime();
    int toTime = after.getArrivalTime();

    double ratio = (effectiveScheduledTime - fromTime)
        / ((double) (toTime - fromTime));

    // Do we have enough information to use shape distance traveled?
    if (shapeId != null && beforeStopTime.getShapeDistTraveled() >= 0
        && afterStopTime.getShapeDistTraveled() >= 0) {

      ShapePoints shapePoints = _shapePointService.getShapePointsForShapeId(shapeId);

      if (!shapePoints.isEmpty()) {
        ShapePointIndex shapePointIndexMethod = getShapeDistanceTraveled(
            beforeStopTime, afterStopTime, ratio);
        return shapePointIndexMethod.getPoint(shapePoints);
      }
    }

    if( ! _interpolateWhenNoShapeInfoPresent )
      return null;
    
    StopEntry beforeStop = before.getStop();
    StopEntry afterStop = after.getStop();
    double latFrom = beforeStop.getStopLat();
    double lonFrom = beforeStop.getStopLon();
    double latTo = afterStop.getStopLat();
    double lonTo = afterStop.getStopLon();
    double lat = (latTo - latFrom) * ratio + latFrom;
    double lon = (lonTo - lonFrom) * ratio + lonFrom;
    return new CoordinatePoint(lat, lon);
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
