package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.transit_data_federation.model.ProjectedPoint;

import edu.washington.cs.rse.geospatial.GeoPoint;
import edu.washington.cs.rse.geospatial.IGeoPoint;
import edu.washington.cs.rse.geospatial.UTMLibrary;
import edu.washington.cs.rse.geospatial.UTMProjection;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

public class ProjectedPointFactory {

  public static ProjectedPoint forward(CoordinatePoint latlon) {
    int zone = UTMLibrary.getUTMZoneForLongitude(latlon.getLon());
    return forward(latlon, zone);
  }

  public static ProjectedPoint forward(CoordinatePoint latlon, int zone) {
    UTMProjection projection = new UTMProjection(zone);
    IGeoPoint point = projection.forward(latlon);
    return new ProjectedPoint(latlon.getLat(), latlon.getLon(), point.getX(),
        point.getY(), zone);
  }

  public static ProjectedPoint ensureSrid(ProjectedPoint point, int srid) {
    if (srid == point.getSrid())
      return point;
    return forward(point.toCoordinatePoint(), srid);
  }

  public static ProjectedPoint reverse(double x, double y, int srid) {
    UTMProjection projection = new UTMProjection(srid);
    GeoPoint p = new GeoPoint(projection, x, y, 0);
    CoordinatePoint latlon = p.getCoordinates();
    return new ProjectedPoint(latlon.getLat(), latlon.getLon(), x, y, srid);
  }
}
