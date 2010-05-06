package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import org.onebusaway.common.web.common.client.model.PathBean;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsDao;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class TripPathFactoryImpl {

  private static final int SHAPE_POINT_PATH_BUFFER = 1500;

  private GtfsDao _dao;

  @Autowired
  public void setGtfsDao(GtfsDao dao) {
    _dao = dao;
  }

  public PathBean getPathFromStopTimeAndLocation(StopTime stFrom,
      CoordinatePoint location) {
    return getPath(stFrom, getStopTimeAsPoint(stFrom), null, location);
  }

  public PathBean getPathFromLocationAndStopTimeAndLocation(
      CoordinatePoint location, StopTime stTo) {
    return getPath(null, location, stTo, getStopTimeAsPoint(stTo));
  }

  public PathBean getStopTimesAsPath(StopTime stFrom, StopTime stTo) {
    CoordinatePoint pointFrom = getStopTimeAsPoint(stFrom);
    CoordinatePoint pointTo = getStopTimeAsPoint(stTo);
    return getPath(stFrom, pointFrom, stTo, pointTo);

  }

  private CoordinatePoint getStopTimeAsPoint(StopTime st) {
    Stop stop = st.getStop();
    return new CoordinatePoint(stop.getLat(), stop.getLon());
  }

  private PathBean getPath(StopTime stFrom, CoordinatePoint pointFrom,
      StopTime stTo, CoordinatePoint pointTo) {

    if (stFrom == null && stTo == null) {
      double[] lat = {pointFrom.getLat(), pointTo.getLat()};
      double[] lon = {pointFrom.getLon(), pointTo.getLon()};
      return new PathBean(lat, lon);
    }

    //System.out.println("points in=" + (System.currentTimeMillis() - t));
    List<ShapePoint> points = getShapePoints(stFrom, stTo);
    //System.out.println("points out=" + (System.currentTimeMillis() - t));

    int preIndex = 0;
    int postIndex = points.size();

    if (stFrom != null) {
      double distanceFrom = stFrom.getShapeDistanceTraveled();
      for (preIndex = 0; preIndex < points.size(); preIndex++) {
        ShapePoint point = points.get(preIndex);
        if (distanceFrom <= point.getDistTraveled())
          break;
      }
    }

    if (stTo != null) {
      double distanceTo = stTo.getShapeDistanceTraveled();
      for (postIndex = points.size(); postIndex > 0; postIndex--) {
        ShapePoint point = points.get(postIndex - 1);
        if (point.getDistTraveled() <= distanceTo)
          break;
      }
    }

    int count = postIndex - preIndex;
    double[] lat = new double[count + 2];
    double[] lon = new double[count + 2];

    for (int i = 0; i < count; i++) {
      ShapePoint point = points.get(i + preIndex);
      lat[i + 1] = point.getLat();
      lon[i + 1] = point.getLon();
    }

    CoordinatePoint p1 = getInterpolatedEndpoint(stFrom, pointFrom, points,
        preIndex - 1);
    lat[0] = p1.getLat();
    lon[0] = p1.getLon();

    CoordinatePoint p2 = getInterpolatedEndpoint(stTo, pointTo, points,
        postIndex - 1);
    lat[count + 1] = p2.getLat();
    lon[count + 1] = p2.getLon();

    //System.out.println("path2=" + (System.currentTimeMillis() - t));

    return new PathBean(lat, lon);
  }

  private List<ShapePoint> getShapePoints(StopTime stFrom, StopTime stTo) {

    if (stFrom == null && stTo != null) {

      Trip trip = stTo.getTrip();
      if (trip.getShapeId() == null)
        return new ArrayList<ShapePoint>();
      double distanceTo = stTo.getShapeDistanceTraveled();
      return _dao.getShapePointsByShapeIdAndDistanceTo(trip.getShapeId(),
          distanceTo + SHAPE_POINT_PATH_BUFFER);

    } else if (stFrom != null && stTo == null) {

      Trip trip = stFrom.getTrip();
      if (trip.getShapeId() == null)
        return new ArrayList<ShapePoint>();
      double distanceFrom = stFrom.getShapeDistanceTraveled();
      return _dao.getShapePointsByShapeIdAndDistanceFrom(trip.getShapeId(),
          distanceFrom - SHAPE_POINT_PATH_BUFFER);

    } else {

      Trip tripA = stFrom.getTrip();
      Trip tripB = stTo.getTrip();

      if (!tripA.equals(tripB))
        throw new IllegalStateException("trip mismatch");

      if (tripA.getShapeId() == null)
        return new ArrayList<ShapePoint>();

      double distanceFrom = stFrom.getShapeDistanceTraveled();
      double distanceTo = stTo.getShapeDistanceTraveled();

      /*
      System.out.println("trip=" + tripA.getShapeId() + " from="
          + (distanceFrom - SHAPE_POINT_PATH_BUFFER) + " to="
          + (distanceTo + SHAPE_POINT_PATH_BUFFER));
      long t = System.currentTimeMillis();
      */

      List<ShapePoint> p = _dao.getShapePointsByShapeIdAndDistanceRange(
          tripA.getShapeId(), distanceFrom - SHAPE_POINT_PATH_BUFFER,
          distanceTo + SHAPE_POINT_PATH_BUFFER);
      /*
      System.out.println("points=" + (System.currentTimeMillis() - t));
      */
      return p;
    }
  }

  private CoordinatePoint getInterpolatedEndpoint(StopTime stopTime,
      CoordinatePoint point, List<ShapePoint> points, int index) {

    if (stopTime == null || index < 0 || index + 1 >= points.size())
      return point;

    double distanceTraveled = stopTime.getShapeDistanceTraveled();
    ShapePoint a = points.get(index);
    ShapePoint b = points.get(index + 1);
    double ratio = (distanceTraveled - a.getDistTraveled())
        / (b.getDistTraveled() - a.getDistTraveled());
    double lat = ratio * b.getLat() + (1 - ratio) * a.getLat();
    double lon = ratio * b.getLon() + (1 - ratio) * a.getLon();
    return new CoordinatePoint(lat, lon);
  }

}
