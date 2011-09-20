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
package org.onebusaway.geospatial.services;

import static org.junit.Assert.assertEquals;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolygonBean;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GeospatialTestSupport {

  public static Comparator<CoordinatePoint> POINTS_COMPARATOR = new CoordinatePointComparator();

  public static Comparator<CoordinateBounds> BOUNDS_COMPARATOR = new CoordinateBoundsComparator();
  
  public static Comparator<EncodedPolygonBean> POLYGON_COMPARATOR = new EncodedPolygonBeanComparator();

  public static void assertEqualsPoints(CoordinatePoint p1, CoordinatePoint p2,
      double delta) {
    assertEquals(p1.getLat(), p2.getLat(), delta);
    assertEquals(p1.getLon(), p2.getLon(), delta);
  }

  public static void assertEqualsPointLists(List<CoordinatePoint> a,
      List<CoordinatePoint> b, double delta) {
    assertEquals(a.size(), b.size());
    for (int i = 0; i < a.size(); i++)
      assertEqualsPoints(a.get(i), b.get(i), delta);
  }

  public static List<CoordinatePoint> shiftPoints(List<CoordinatePoint> points) {

    if (points.size() < 2)
      return points;

    int minIndex = 0;
    CoordinatePoint minPoint = null;

    for (int index = 0; index < points.size(); index++) {
      CoordinatePoint p = points.get(index);
      if (minPoint == null || POINTS_COMPARATOR.compare(p, minPoint) < 0) {
        minIndex = index;
        minPoint = p;
      }
    }

    List<CoordinatePoint> shifted = new ArrayList<CoordinatePoint>();
    for (int i = minIndex; i < points.size(); i++)
      shifted.add(points.get(i));
    for (int i = 0; i < minIndex; i++)
      shifted.add(points.get(i));

    return shifted;
  }

  private static class CoordinatePointComparator implements
      Comparator<CoordinatePoint> {

    public int compare(CoordinatePoint p1, CoordinatePoint p2) {

      int rc = Double.compare(p1.getLat(), p2.getLat());

      if (rc == 0)
        rc = Double.compare(p1.getLon(), p2.getLon());

      return rc;
    }
  }

  private static class CoordinateBoundsComparator implements
      Comparator<CoordinateBounds> {

    public int compare(CoordinateBounds o1, CoordinateBounds o2) {

      int rc = Double.compare(o1.getMinLat(), o2.getMinLat());

      if (rc == 0)
        rc = Double.compare(o1.getMaxLat(), o2.getMaxLat());

      if (rc == 0)
        rc = Double.compare(o1.getMinLon(), o2.getMinLon());

      if (rc == 0)
        rc = Double.compare(o1.getMaxLon(), o2.getMaxLon());

      return rc;
    }
  }

  private static class EncodedPolygonBeanComparator implements
      Comparator<EncodedPolygonBean> {

    public int compare(EncodedPolygonBean o1, EncodedPolygonBean o2) {
      return o1.getOuterRing().getPoints().compareTo(
          o2.getOuterRing().getPoints());
    }
  }

}
