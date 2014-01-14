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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.XYPoint;

public class UTMProjectionTest {

  @Test
  public void testProjectionUW() throws NumberFormatException, IOException {
    assertUTMPoint(47.65458077, -122.30502529, 552186.99, 5278143.40);
  }

  @Test
  public void testProjectionBryant() throws NumberFormatException, IOException {

    assertUTMPoint(47.66933, -122.289114, 553366.76, 5279793.45);
  }

  @Test
  public void testMultiProjection() {

    assertUTMPoint(47.65458077, -122.30502529, 552186.99, 5278143.40);
    assertUTMPoint(47.66933, -122.289114, 553366.76, 5279793.45);

    List<CoordinatePoint> points = new ArrayList<CoordinatePoint>();

    points.add(new CoordinatePoint(47.65458077, -122.30502529));
    points.add(new CoordinatePoint(47.66933, -122.289114));

    List<XYPoint> results = new ArrayList<XYPoint>();

    UTMProjection projection = new UTMProjection(10);
    projection.forward(points, results, 2);

    XYPoint p0 = results.get(0);
    XYPoint p1 = results.get(1);

    assertEquals(552186.99, p0.getX(), 0.01);
    assertEquals(5278143.40, p0.getY(), 0.01);

    assertEquals(553366.76, p1.getX(), 0.01);
    assertEquals(5279793.45, p1.getY(), 0.01);
  }

  private void assertUTMPoint(double lat, double lon, double x, double y) {
    CoordinatePoint point = new CoordinatePoint(lat, lon);
    int zone = UTMLibrary.getUTMZoneForLongitude(lon);

    UTMProjection projection = new UTMProjection(zone);
    XYPoint p = projection.forward(point);

    assertEquals(x, p.getX(), 0.01);
    assertEquals(y, p.getY(), 0.01);
  }

}
