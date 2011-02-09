/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.shapes;

import org.onebusaway.transit_data_federation.model.ShapePoints;

public class FirstShapePointIndex extends AbstractShapePointIndex {
  
  @Override
  public int getIndex(ShapePoints point) {
    return 0;
  }

  @Override
  public PointAndOrientation getPointAndOrientation(ShapePoints points) {

    int n = points.getSize();

    if (n == 0)
      throw new IndexOutOfBoundsException();

    if (n == 1)
      return computePointAndOrientation(points, 0, 0, 0);

    return computePointAndOrientation(points, 0, 0, 1);
  }  
}