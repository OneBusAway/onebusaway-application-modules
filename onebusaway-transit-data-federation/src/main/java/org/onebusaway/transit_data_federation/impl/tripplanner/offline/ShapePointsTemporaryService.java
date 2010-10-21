package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.ShapePointsFactory;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ShapePointsTemporaryService {

  private static Logger _log = LoggerFactory.getLogger(ShapePointsTemporaryService.class);

  private GtfsRelationalDao _gtfsDao;

  private Map<AgencyAndId, ShapePoints> _shapePointsCache = new HashMap<AgencyAndId, ShapePoints>();

  private Map<AgencyAndId, ShapePoint> _firstPointByShapeId = new HashMap<AgencyAndId, ShapePoint>();

  private Map<AgencyAndId, ShapePoint> _lastPointByShapeId = new HashMap<AgencyAndId, ShapePoint>();

  @Autowired
  public void setGtfsDao(GtfsRelationalDao gtfsDao) {
    _gtfsDao = gtfsDao;
  }

  /**
   * @param shapeId
   * @return the set of shape points for the specified shape id, or null if none
   *         available
   */
  public ShapePoints getShapePoints(AgencyAndId shapeId) {

    if (_shapePointsCache.containsKey(shapeId))
      return _shapePointsCache.get(shapeId);

    ShapePointsFactory factory = new ShapePointsFactory();
    List<ShapePoint> rawPoints = _gtfsDao.getShapePointsForShapeId(shapeId);
    rawPoints = new ArrayList<ShapePoint>(rawPoints);
    Collections.sort(rawPoints);
    for (ShapePoint rawPoint : rawPoints)
      factory.addPoint(rawPoint.getLat(), rawPoint.getLon());

    ShapePoints shapePoints = factory.create();

    if (shapePoints.isEmpty()) {
      _log.warn("no shape points defined for shapeId=" + shapeId);
      shapePoints = null;
    }

    _shapePointsCache.put(shapeId, shapePoints);

    if (shapePoints != null) {
      ensurePoint(_firstPointByShapeId, shapeId, shapePoints, 0);
      ensurePoint(_lastPointByShapeId, shapeId, shapePoints,
          shapePoints.getSize() - 1);
    }

    return shapePoints;
  }

  /**
   * 
   * @param shapeId
   * @return the first point of the specified shape, or null if not available
   */
  public ShapePoint getFirstPointForShapeId(AgencyAndId shapeId) {
    return _firstPointByShapeId.get(shapeId);
  }

  /**
   * 
   * @param shapeId
   * @return the last point of the specified shape, or null if not available
   */
  public ShapePoint getLastPointForShapeId(AgencyAndId shapeId) {
    return _lastPointByShapeId.get(shapeId);
  }

  public void clearCache() {
    _shapePointsCache.clear();
  }

  public double[] computeGapDistancesBetweenTrips(List<TripEntry> trips) {

    double[] tripGapDistances = new double[trips.size()];

    for (int index = 0; index < trips.size() - 1; index++) {
      TripEntry tripA = trips.get(index);
      TripEntry tripB = trips.get(index + 1);
      CoordinatePoint from = getEndOfShape(tripA);
      CoordinatePoint to = getStartOfShape(tripB);
      tripGapDistances[index] = SphericalGeometryLibrary.distance(from, to);
    }

    return tripGapDistances;
  }

  /****
   * Private Methods
   ****/

  private CoordinatePoint getEndOfShape(TripEntry trip) {

    AgencyAndId shapeId = trip.getShapeId();

    if (shapeId != null) {
      ShapePoint lastShapePoint = getLastPointForShapeId(shapeId);
      if (lastShapePoint != null)
        return new CoordinatePoint(lastShapePoint.getLat(),
            lastShapePoint.getLon());
    }

    List<StopTimeEntry> stopTimes = trip.getStopTimes();
    StopTimeEntry lastStopTime = stopTimes.get(stopTimes.size() - 1);
    return lastStopTime.getStop().getStopLocation();
  }

  private CoordinatePoint getStartOfShape(TripEntry trip) {

    AgencyAndId shapeId = trip.getShapeId();

    if (shapeId != null) {
      ShapePoint firstShapePoint = getFirstPointForShapeId(shapeId);
      if (firstShapePoint != null)
        return new CoordinatePoint(firstShapePoint.getLat(),
            firstShapePoint.getLon());
    }

    List<StopTimeEntry> stopTimes = trip.getStopTimes();
    StopTimeEntry firstStopTime = stopTimes.get(0);
    return firstStopTime.getStop().getStopLocation();
  }

  private void ensurePoint(Map<AgencyAndId, ShapePoint> pointsByShapeId,
      AgencyAndId shapeId, ShapePoints shapePoints, int index) {

    ShapePoint point = pointsByShapeId.get(shapeId);
    if (point != null)
      return;

    point = new ShapePoint();
    point.setLat(shapePoints.getLatForIndex(index));
    point.setLon(shapePoints.getLonForIndex(index));
    point.setDistTraveled(shapePoints.getDistTraveledForIndex(index));

    pointsByShapeId.put(shapeId, point);
  }
}
