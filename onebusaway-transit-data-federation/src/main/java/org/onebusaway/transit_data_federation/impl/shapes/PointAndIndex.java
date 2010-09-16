package org.onebusaway.transit_data_federation.impl.shapes;

import org.onebusaway.geospatial.model.XYPoint;

public class PointAndIndex implements Comparable<PointAndIndex> {

  public XYPoint point;
  public int index;
  public double distanceAlongShape;

  public PointAndIndex(XYPoint point, int index, double distanceAlongShape) {
    this.point = point;
    this.index = index;
    this.distanceAlongShape = distanceAlongShape;
  }

  @Override
  public int compareTo(PointAndIndex o) {
    if (distanceAlongShape == o.distanceAlongShape)
      return 0;
    return distanceAlongShape < o.distanceAlongShape ? -1 : 1;
  }
}