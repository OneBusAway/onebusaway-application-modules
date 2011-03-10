/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.shapes;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.model.ShapePoints;

public class LastShapePointIndex implements ShapePointIndex {
  
  @Override
  public int getIndex(ShapePoints points) {
    return points.getSize();
  }

  @Override
  public CoordinatePoint getPoint(ShapePoints points) {
    int n = points.getSize() - 1;
    double[] lats = points.getLats();
    double[] lons = points.getLons();
    return new CoordinatePoint(lats[n],lons[n]);
  }  
}