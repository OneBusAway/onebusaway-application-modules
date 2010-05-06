/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.shapes;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.model.ShapePoints;

public class FirstShapePointIndex implements ShapePointIndex {
  
  @Override
  public int getIndex(ShapePoints point) {
    return 0;
  }

  @Override
  public CoordinatePoint getPoint(ShapePoints points) {
    double[] lats = points.getLats();
    double[] lons = points.getLons();
    return new CoordinatePoint(lats[0],lons[0]);
  }
}