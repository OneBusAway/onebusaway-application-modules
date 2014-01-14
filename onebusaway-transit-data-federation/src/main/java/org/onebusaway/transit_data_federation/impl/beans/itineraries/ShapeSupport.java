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
package org.onebusaway.transit_data_federation.impl.beans.itineraries;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.geospatial.services.PolylineEncoder;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.utility.InterpolationLibrary;

public class ShapeSupport {

  public static String getFullPath(ShapePoints shapePoints,
      CoordinatePoint nextPoint) {

    if (nextPoint == null) {
      EncodedPolylineBean bean = PolylineEncoder.createEncodings(
          shapePoints.getLats(), shapePoints.getLons());
      return bean.getPoints();
    } else {
      int n = shapePoints.getSize() + 1;
      double[] lats = new double[n];
      double[] lons = new double[n];
      System.arraycopy(shapePoints.getLats(), 0, lats, 0, n - 1);
      System.arraycopy(shapePoints.getLons(), 0, lons, 0, n - 1);
      lats[n - 1] = nextPoint.getLat();
      lons[n - 1] = nextPoint.getLon();
      EncodedPolylineBean bean = PolylineEncoder.createEncodings(lats, lons);
      return bean.getPoints();
    }
  }

  public static String getPartialPathToStop(ShapePoints shapePoints,
      StopTimeEntry toStop) {

    int indexTo = toStop.getShapePointIndex();

    double[] lats = new double[indexTo + 2];
    double[] lons = new double[indexTo + 2];

    System.arraycopy(shapePoints.getLats(), 0, lats, 0, indexTo + 1);
    System.arraycopy(shapePoints.getLons(), 0, lons, 0, indexTo + 1);

    CoordinatePoint to = interpolate(shapePoints, toStop);
    lats[indexTo + 1] = to.getLat();
    lons[indexTo + 1] = to.getLon();

    EncodedPolylineBean bean = PolylineEncoder.createEncodings(lats, lons);
    return bean.getPoints();
  }

  public static String getPartialPathFromStop(ShapePoints shapePoints,
      StopTimeEntry fromStop, CoordinatePoint nextPoint) {

    int indexFrom = fromStop.getShapePointIndex();
    int indexTo = shapePoints.getSize();
    int size = indexTo - indexFrom;
    int sizeExtended = size;
    if (nextPoint != null)
      sizeExtended++;

    double[] lats = new double[sizeExtended];
    double[] lons = new double[sizeExtended];

    System.arraycopy(shapePoints.getLats(), indexFrom + 1, lats, 1, size - 1);
    System.arraycopy(shapePoints.getLons(), indexFrom + 1, lons, 1, size - 1);

    CoordinatePoint from = interpolate(shapePoints, fromStop);
    lats[0] = from.getLat();
    lons[0] = from.getLon();

    if (nextPoint != null) {
      lats[sizeExtended - 1] = nextPoint.getLat();
      lons[sizeExtended - 1] = nextPoint.getLon();
    }

    EncodedPolylineBean bean = PolylineEncoder.createEncodings(lats, lons);
    return bean.getPoints();
  }

  public static String getPartialPathBetweenStops(ShapePoints shapePoints,
      StopTimeEntry fromStop, StopTimeEntry toStop) {

    int indexFrom = fromStop.getShapePointIndex();
    int indexTo = toStop.getShapePointIndex();
    int size = indexTo - indexFrom + 2;

    double[] lats = new double[size];
    double[] lons = new double[size];

    System.arraycopy(shapePoints.getLats(), indexFrom + 1, lats, 1, size - 2);
    System.arraycopy(shapePoints.getLons(), indexFrom + 1, lons, 1, size - 2);

    CoordinatePoint from = interpolate(shapePoints, fromStop);
    lats[0] = from.getLat();
    lons[0] = from.getLon();

    CoordinatePoint to = interpolate(shapePoints, toStop);
    lats[size - 1] = to.getLat();
    lons[size - 1] = to.getLon();

    EncodedPolylineBean bean = PolylineEncoder.createEncodings(lats, lons);
    return bean.getPoints();
  }

  private static CoordinatePoint interpolate(ShapePoints shapePoints,
      StopTimeEntry stop) {

    double[] lats = shapePoints.getLats();
    double[] lons = shapePoints.getLons();
    double[] distances = shapePoints.getDistTraveled();

    int index = stop.getShapePointIndex();

    if (index + 1 == lats.length)
      return new CoordinatePoint(lats[index], lons[index]);

    double d1 = distances[index];
    double d2 = distances[index + 1];

    if (d1 == d2)
      return new CoordinatePoint(lats[index], lons[index]);

    double ratio = (stop.getShapeDistTraveled() - d1) / (d2 - d1);

    double lat = InterpolationLibrary.interpolatePair(lats[index],
        lats[index + 1], ratio);
    double lon = InterpolationLibrary.interpolatePair(lons[index],
        lons[index + 1], ratio);

    return new CoordinatePoint(lat, lon);
  }
}
