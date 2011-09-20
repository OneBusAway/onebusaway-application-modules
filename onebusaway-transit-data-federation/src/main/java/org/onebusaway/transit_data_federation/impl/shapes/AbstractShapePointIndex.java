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
package org.onebusaway.transit_data_federation.impl.shapes;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data_federation.model.ShapePoints;

public abstract class AbstractShapePointIndex implements ShapePointIndex {

  @Override
  public CoordinatePoint getPoint(ShapePoints points) {
    return getPointAndOrientation(points).getPoint();
  }

  protected PointAndOrientation computePointAndOrientation(
      ShapePoints shapePoints, int pointIndex, int orientationIndexFrom,
      int orientationIndexTo) {

    double lat = shapePoints.getLatForIndex(pointIndex);
    double lon = shapePoints.getLonForIndex(pointIndex);

    double orientation = computeOrientation(shapePoints, orientationIndexFrom,
        orientationIndexTo);

    return new PointAndOrientation(lat, lon, orientation);
  }

  protected double computeOrientation(ShapePoints shapePoints, int indexFrom,
      int indexTo) {

    if (indexFrom == indexTo)
      return Double.NaN;

    double latFrom = shapePoints.getLatForIndex(indexFrom);
    double lonFrom = shapePoints.getLonForIndex(indexFrom);
    double latTo = shapePoints.getLatForIndex(indexTo);
    double lonTo = shapePoints.getLonForIndex(indexTo);
    return SphericalGeometryLibrary.getOrientation(latFrom, lonFrom, latTo,
        lonTo);
  }
}
