package org.onebusaway.transit_data_federation.impl.tripplanner;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.Stop;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;
import edu.washington.cs.rse.geospatial.latlon.CoordinateRectangle;

public class DistanceLibrary {

  public static final double distance(CoordinatePoint a, CoordinatePoint b) {
    return SphericalGeometryLibrary.distance(a.getLat(), a.getLon(),
        b.getLat(), b.getLon());
  }

  public static final double distance(Stop a, Stop b) {
    return SphericalGeometryLibrary.distance(a.getLat(), a.getLon(),
        b.getLat(), b.getLon());
  }

  public static final CoordinateRectangle bounds(CoordinatePoint point,
      double distance) {
    return rectangle(SphericalGeometryLibrary.bounds(point.getLat(),
        point.getLon(), distance));
  }

  public static final CoordinateRectangle rectangle(CoordinateBounds bounds) {
    return new CoordinateRectangle(bounds.getMinLat(), bounds.getMinLon(),
        bounds.getMaxLat(), bounds.getMaxLon());
  }
}
