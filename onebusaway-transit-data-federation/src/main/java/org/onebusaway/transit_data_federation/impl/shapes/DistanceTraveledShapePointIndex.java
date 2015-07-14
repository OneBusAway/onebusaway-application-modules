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

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.model.ShapePoints;

import java.util.Arrays;

public class DistanceTraveledShapePointIndex extends AbstractShapePointIndex {

  private double _shapeDistanceTraveled;

  private int _fromIndex = -1;

  private int _toIndex = -1;

  public DistanceTraveledShapePointIndex(double shapeDistanceTraveled) {
    _shapeDistanceTraveled = shapeDistanceTraveled;
  }

  public DistanceTraveledShapePointIndex(double shapeDistanceTraveled,
      int fromIndex, int toIndex) {
    _shapeDistanceTraveled = shapeDistanceTraveled;
    _fromIndex = fromIndex;
    _toIndex = toIndex;
  }

  @Override
  public int getIndex(ShapePoints points) {
    points.ensureDistTraveled();

    int index = 0;

    /**
     * Use index hints when available
     */
    if (_fromIndex < 0 || _toIndex < 0 || _fromIndex > _toIndex)
      index = Arrays.binarySearch(points.getDistTraveled(),
          _shapeDistanceTraveled);
    else
      index = Arrays.binarySearch(points.getDistTraveled(), _fromIndex,
          _toIndex, _shapeDistanceTraveled);

    if (index < 0)
      index = -(index + 1);
    return index;
  }

  @Override
  public CoordinatePoint getPoint(ShapePoints points) {

    int n = points.getSize();

    if (n == 0)
      throw new IndexOutOfBoundsException();

    int index = getIndex(points);

    double[] lats = points.getLats();
    double[] lons = points.getLons();
    double[] dist = points.getDistTraveled();

    if (index == 0)
      return new CoordinatePoint(lats[0], lons[0]);
    if (index == n)
      return new CoordinatePoint(lats[n - 1], lons[n - 1]);

    if (dist[index] == dist[index - 1])
      return new CoordinatePoint(lats[n - 1], lons[n - 1]);

    double ratio = (_shapeDistanceTraveled - dist[index - 1])
        / (dist[index] - dist[index - 1]);
    double lat = ratio * (lats[index] - lats[index - 1]) + lats[index - 1];
    double lon = ratio * (lons[index] - lons[index - 1]) + lons[index - 1];
    return new CoordinatePoint(lat, lon);
  }

  @Override
  public PointAndOrientation getPointAndOrientation(ShapePoints points) {

    int n = points.getSize();

    if (n == 0)
      throw new IndexOutOfBoundsException();

    if (n == 1)
      return computePointAndOrientation(points, 0, 0, 0);

    int index = getIndex(points);

    double[] lats = points.getLats();
    double[] lons = points.getLons();
    double[] dist = points.getDistTraveled();

    if (index == 0)
      return computePointAndOrientation(points, 0, 0, 1);
    if (index == n)
      return computePointAndOrientation(points, n - 1, n - 2, n - 1);

    if (dist[index] == dist[index - 1]) {
      double lat = lats[index];
      double lon = lons[index];
      return new PointAndOrientation(lat, lon, 0);
    }

    double ratio = (_shapeDistanceTraveled - dist[index - 1])
        / (dist[index] - dist[index - 1]);
    double lat = ratio * (lats[index] - lats[index - 1]) + lats[index - 1];
    double lon = ratio * (lons[index] - lons[index - 1]) + lons[index - 1];

    return new PointAndOrientation(lat, lon, computeOrientation(points,
        index - 1, index));
  }
}