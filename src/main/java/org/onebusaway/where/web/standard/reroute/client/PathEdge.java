package org.onebusaway.where.web.standard.reroute.client;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Polyline;

public class PathEdge {
  private final PathNode _from;
  private final PathNode _to;
  private final Polyline _line;

  public PathEdge(PathNode from, PathNode to, Polyline line) {

    LatLng p1 = from.getPoint();
    LatLng p2 = to.getPoint();
    LatLng l1 = line.getVertex(0);
    LatLng l2 = line.getVertex(line.getVertexCount() - 1);

    if (p1.distanceFrom(l1) + p2.distanceFrom(l2) > p1.distanceFrom(l2) + p2.distanceFrom(l1)) {
      PathNode temp = from;
      from = to;
      to = temp;
    }

    _from = from;
    _to = to;
    _line = line;
  }

  public PathNode getFrom() {
    return _from;
  }

  public PathNode getTo() {
    return _to;
  }

  public Polyline getLine() {
    return _line;
  }

  public PathNode getOppositeEndPoint(PathNode node) {
    if (node.equals(_from))
      return _to;
    else if (node.equals(_to))
      return _from;
    else
      throw new IllegalStateException("unknown node");
  }

  @Override
  public String toString() {
    return "edge(from=" + _from + " to=" + _to + ")";
  }
}
