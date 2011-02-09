package org.onebusaway.transit_data_federation.impl.tripplanner;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.Stop;

public class DistanceLibrary {

  public static final double distance(Stop a, Stop b) {
    return SphericalGeometryLibrary.distance(a.getLat(), a.getLon(),
        b.getLat(), b.getLon());
  }

  public static final CoordinateBounds bounds(CoordinatePoint point,
      double distance) {
    return SphericalGeometryLibrary.bounds(point.getLat(), point.getLon(),
        distance);
  }
}
