package org.onebusaway.transit_data_federation.impl.shapes;

import org.onebusaway.geospatial.model.CoordinatePoint;

public class PointAndOrientation {

  private final CoordinatePoint point;

  private final double orientation;

  public PointAndOrientation(double lat, double lon, double orientation) {
    this.point = new CoordinatePoint(lat,lon);
    this.orientation = orientation;
  }

  public CoordinatePoint getPoint() {
    return point;
  }

  public double getOrientation() {
    return orientation;
  }
}
