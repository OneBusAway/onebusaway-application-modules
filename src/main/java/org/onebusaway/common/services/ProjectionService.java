package org.onebusaway.common.services;

import edu.washington.cs.rse.geospatial.ICoordinateProjection;
import edu.washington.cs.rse.geospatial.IGeoPoint;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import com.vividsolutions.jts.geom.Point;

import java.util.List;

public interface ProjectionService {
  
  public ICoordinateProjection getProjection();

  /****
   * Lat Lon Methods
   ****/

  public Point getLatLonAsPoint(double lat, double lon);

  /****
   * Point Methods
   ****/

  public CoordinatePoint getPointAsLatLong(Point p);

  public List<CoordinatePoint> getPointsAsLatLongs(Iterable<Point> points,
      int size);

  public IGeoPoint getPointAsGeoPoint(Point point);

  /****
   * Coordinate Point Methods
   ****/

  public Point getCoordinatePointAsPoint(CoordinatePoint coordinatePoint);

  public List<Point> getCoordinatePointsAsPoints(List<CoordinatePoint> points);

  public List<Point> getCoordinatePointsAsPoints(
      Iterable<CoordinatePoint> points, int size);

  /****
   * GeoPoint Methods
   ****/

  public Point getGeoPointAsPoint(IGeoPoint point);

  /****
   * X Y Methods
   ****/

  public Point getXYAsPoint(double x, double y);

  public IGeoPoint getXYAsGeoPoint(double x, double y);
  
  public CoordinatePoint getXYAsLatLong(double x, double y);

}
