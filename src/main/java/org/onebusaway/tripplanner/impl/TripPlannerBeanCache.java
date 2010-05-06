package org.onebusaway.tripplanner.impl;

import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TripPlannerBeanCache {

  /*****************************************************************************
   * Queries to Execute
   ****************************************************************************/

  private Set<String> _tripIds = new HashSet<String>();

  private Set<String> _stopIds = new HashSet<String>();

  private Set<Integer> _stopTimeIds = new HashSet<Integer>();

  /*****************************************************************************
   * Results
   ****************************************************************************/

  private Map<String, Trip> _tripsById = new HashMap<String, Trip>();

  private Map<String, Stop> _stopsById = new HashMap<String, Stop>();

  private Map<Integer, StopTime> _stopTimesById = new HashMap<Integer, StopTime>();

  private Map<String, List<ShapePoint>> _shapePointsByShapeId = new HashMap<String, List<ShapePoint>>();

  public void addTrip(String tripId) {
    _tripIds.add(tripId);
  }

  public void addStop(String stopId) {
    _stopIds.add(stopId);
  }

  public void addStopTime(Integer stopTimeId) {
    _stopTimeIds.add(stopTimeId);
  }

  public void go(GtfsDao dao) {

    List<Trip> trips = dao.getTripsByIds(_tripIds);

    for (Trip trip : trips)
      _tripsById.put(trip.getId(), trip);

    List<Stop> stops = dao.getStopsByIds(_stopIds);

    for (Stop stop : stops)
      _stopsById.put(stop.getId(), stop);

    List<StopTime> stopTimes = dao.getStopTimesByIds(_stopTimeIds);

    for (StopTime stopTime : stopTimes)
      _stopTimesById.put(stopTime.getId(), stopTime);

    Set<String> shapeIds = new HashSet<String>();

    for (Trip trip : trips) {
      if (trip.getShapeId() != null)
        shapeIds.add(trip.getShapeId());
    }

    List<ShapePoint> shapePoints = dao.getShapePointsByShapeIds(shapeIds);

    for (ShapePoint point : shapePoints) {
      String shapeId = point.getId().getShapeId();
      List<ShapePoint> points = _shapePointsByShapeId.get(shapeId);
      if (points == null) {
        points = new ArrayList<ShapePoint>();
        _shapePointsByShapeId.put(shapeId, points);
      }
      points.add(point);
    }

  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  public Trip getTripForId(String tripId) {
    return _tripsById.get(tripId);
  }

  public Stop getStopForId(String stopId) {
    return _stopsById.get(stopId);
  }

  public StopTime getStopTimeForId(Integer id) {
    return _stopTimesById.get(id);
  }

  public List<ShapePoint> getShapePointsForShapeId(String shapeId) {
    return _shapePointsByShapeId.get(shapeId);
  }
}
