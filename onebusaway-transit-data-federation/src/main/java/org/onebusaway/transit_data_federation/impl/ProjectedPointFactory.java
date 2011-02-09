package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.XYPoint;
import org.onebusaway.geospatial.services.UTMLibrary;
import org.onebusaway.geospatial.services.UTMProjection;
import org.onebusaway.transit_data_federation.model.ProjectedPoint;

public class ProjectedPointFactory {

  public static ProjectedPoint forward(double lat, double lon) {
    return forward(new CoordinatePoint(lat, lon));
  }

  public static ProjectedPoint forward(CoordinatePoint latlon) {
    int zone = UTMLibrary.getUTMZoneForLongitude(latlon.getLon());
    return forward(latlon, zone);
  }

  public static ProjectedPoint forward(CoordinatePoint latlon, int zone) {
    UTMProjection projection = new UTMProjection(zone);
    XYPoint point = projection.forward(latlon);
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
    XYPoint p = new XYPoint(x, y);
    CoordinatePoint latlon = projection.reverse(p);
    return new ProjectedPoint(latlon.getLat(), latlon.getLon(), x, y, srid);
  }
}
