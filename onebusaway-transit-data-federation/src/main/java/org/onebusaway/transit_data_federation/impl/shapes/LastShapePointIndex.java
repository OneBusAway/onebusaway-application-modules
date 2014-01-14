/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.shapes;

import org.onebusaway.transit_data_federation.model.ShapePoints;

public class LastShapePointIndex extends AbstractShapePointIndex {

  @Override
  public int getIndex(ShapePoints points) {
    return points.getSize();
  }

  @Override
  public PointAndOrientation getPointAndOrientation(ShapePoints points) {

    int n = points.getSize();

    if (n == 0)
      throw new IndexOutOfBoundsException();

    if (n == 1)
      return computePointAndOrientation(points, 0, 0, 0);

    return computePointAndOrientation(points, n - 1, n - 2, n - 1);
  }
}