package org.onebusaway.where.web.standard.reroute.client;

import com.google.gwt.maps.client.geom.LatLng;

public class PathNode {

  private final LatLng _point;

  public PathNode(LatLng point) {
    _point = point;
  }

  public LatLng getPoint() {
    return _point;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof PathNode))
      return false;
    PathNode node = (PathNode) obj;
    return _point.getLatitude() == node._point.getLatitude() && _point.getLongitude() == node._point.getLongitude();
  }

  @Override
  public int hashCode() {
    return new Double(_point.getLatitude()).hashCode() + new Double(_point.getLongitude()).hashCode();
  }

  @Override
  public String toString() {
    return "node(lat=" + _point.getLatitude() + " lon=" + _point.getLongitude() + ")";
  }
}
