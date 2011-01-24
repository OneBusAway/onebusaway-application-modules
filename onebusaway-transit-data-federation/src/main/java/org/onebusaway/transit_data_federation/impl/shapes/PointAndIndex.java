package org.onebusaway.transit_data_federation.impl.shapes;

import org.onebusaway.geospatial.model.XYPoint;

public class PointAndIndex implements Comparable<PointAndIndex> {

  public final XYPoint point;
  public final int index;
  public final double distanceFromTarget;
  public final double distanceAlongShape;

  public PointAndIndex(XYPoint point, int index, double distanceFromTarget, double distanceAlongShape) {
    this.point = point;
    this.index = index;
    this.distanceFromTarget = distanceFromTarget;
    this.distanceAlongShape = distanceAlongShape;
  }

  @Override
  public int compareTo(PointAndIndex o) {
    if (distanceAlongShape == o.distanceAlongShape)
      return 0;
    return distanceAlongShape < o.distanceAlongShape ? -1 : 1;
  }
}