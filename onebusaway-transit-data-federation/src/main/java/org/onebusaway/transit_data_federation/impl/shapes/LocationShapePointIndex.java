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

import org.onebusaway.collections.Min;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.model.ShapePoints;

public class LocationShapePointIndex extends AbstractShapePointIndex {

  private double _lat;

  private double _lon;

  public LocationShapePointIndex(double lat, double lon) {
    _lat = lat;
    _lon = lon;
  }

  @Override
  public int getIndex(ShapePoints points) {
    Min<Integer> m = new Min<Integer>();
    int n = points.getSize();
    double[] lats = points.getLats();
    double[] lons = points.getLons();
    for (int i = 0; i < n; i++) {
      double d = distance(_lat, _lon, lats[i], lons[i]);
      m.add(d, i);
    }
    return m.getMinElement();
  }

  @Override
  public CoordinatePoint getPoint(ShapePoints points) {
    return new CoordinatePoint(_lat, _lon);
  }

  @Override
  public PointAndOrientation getPointAndOrientation(ShapePoints points) {
    return new PointAndOrientation(_lat, _lon, 0);
  }

  private static double distance(double lat1, double lon1, double lat2,
      double lon2) {
    double dLat = lat1 - lat2;
    double dLon = lon1 - lon2;
    return Math.sqrt(dLat * dLat + dLon * dLon);
  }

}