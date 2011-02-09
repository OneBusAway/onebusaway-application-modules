/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.shapes;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.model.ShapePoints;

public interface ShapePointIndex {
  public int getIndex(ShapePoints points);
  public CoordinatePoint getPoint(ShapePoints points);
  public PointAndOrientation getPointAndOrientation(ShapePoints points);
}