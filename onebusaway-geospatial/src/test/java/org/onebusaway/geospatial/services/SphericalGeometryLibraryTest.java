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

import org.junit.Test;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;

public class SphericalGeometryLibraryTest {

  @Test
  public void testDistanceFast() {
    double d1 = SphericalGeometryLibrary.distanceFaster(47.66855200022102,
        -122.2901701927185, 47.67031486931084, -122.28911876678467);
    assertEquals(158.73, d1, 0.01);

    double d2 = SphericalGeometryLibrary.distanceFaster(47.670300419806246,
        -122.29019165039062, 47.67579094347484, -122.30066299438477);
    assertEquals(914.30, d2, 0.01);

    double d3 = SphericalGeometryLibrary.distanceFaster(47.2561898535,
        -122.439996129, 47.2555220025, -122.439844215);
    assertEquals(52.96, d3, 0.01);
  }

  @Test
  public void testDistance() {

    double d1 = SphericalGeometryLibrary.distance(47.66855200022102,
        -122.2901701927185, 47.67031486931084, -122.28911876678467);
    assertEquals(211.24, d1, 0.01);

    double d2 = SphericalGeometryLibrary.distance(47.670300419806246,
        -122.29019165039062, 47.67579094347484, -122.30066299438477);
    assertEquals(993.70, d2, 0.01);

    double d3 = SphericalGeometryLibrary.distance(47.2561898535,
        -122.439996129, 47.2555220025, -122.439844215);
    assertEquals(75.14, d3, 0.01);
  }

  @Test
  public void testBounds() {

    CoordinateBounds bounds = SphericalGeometryLibrary.bounds(
        47.97527158291236, -122.3527193069458, 400);

    double d1 = SphericalGeometryLibrary.distance(bounds.getMaxLat(),
        bounds.getMaxLon(), bounds.getMinLat(), bounds.getMaxLon());
    assertEquals(800, d1, 0.01);

    double d2 = SphericalGeometryLibrary.distance(bounds.getMaxLat(),
        bounds.getMinLon(), bounds.getMinLat(), bounds.getMinLon());
    assertEquals(800, d2, 0.01);

    double d3 = SphericalGeometryLibrary.distance(bounds.getMinLat(),
        bounds.getMaxLon(), bounds.getMinLat(), bounds.getMinLon());
    assertEquals(800, d3, 0.1);

    double d4 = SphericalGeometryLibrary.distance(bounds.getMaxLat(),
        bounds.getMaxLon(), bounds.getMaxLat(), bounds.getMinLon());
    assertEquals(800, d4, 0.1);
  }

  @Test
  public void testBoundsLatAndLon() {

    CoordinateBounds bounds = SphericalGeometryLibrary.bounds(
        47.97527158291236, -122.3527193069458, 400, 200);

    double d1 = SphericalGeometryLibrary.distance(bounds.getMaxLat(),
        bounds.getMaxLon(), bounds.getMinLat(), bounds.getMaxLon());
    assertEquals(800, d1, 0.01);

    double d2 = SphericalGeometryLibrary.distance(bounds.getMaxLat(),
        bounds.getMinLon(), bounds.getMinLat(), bounds.getMinLon());
    assertEquals(800, d2, 0.01);

    double d3 = SphericalGeometryLibrary.distance(bounds.getMinLat(),
        bounds.getMaxLon(), bounds.getMinLat(), bounds.getMinLon());
    assertEquals(400, d3, 0.1);

    double d4 = SphericalGeometryLibrary.distance(bounds.getMaxLat(),
        bounds.getMaxLon(), bounds.getMaxLat(), bounds.getMinLon());
    assertEquals(400, d4, 0.1);
  }

  @Test
  public void testGetCenterOfBounds() {
    CoordinateBounds b = new CoordinateBounds(-1.0, -2.0, 4.0, 3.6);
    CoordinatePoint p = SphericalGeometryLibrary.getCenterOfBounds(b);
    assertEquals(1.5, p.getLat(), 0.0);
    assertEquals(0.8, p.getLon(), 0.0);
  }

  @Test
  public void testGetOrientation() {
    double lat0 = 47.6538286680302;
    double lon0 = -122.30780805044554;

    double orientation = SphericalGeometryLibrary.getOrientation(lat0, lon0,
        47.65385757615516, -122.30632747106932);
    assertEquals(1.1, orientation, 0.1);

    orientation = SphericalGeometryLibrary.getOrientation(lat0, lon0,
        47.65415388351328, -122.3063596575775);
    assertEquals(12.6, orientation, 0.1);

    orientation = SphericalGeometryLibrary.getOrientation(lat0, lon0,
        47.65441405444146, -122.30704630308531);
    assertEquals(37.5, orientation, 0.1);

    orientation = SphericalGeometryLibrary.getOrientation(lat0, lon0,
        47.65435623879166, -122.30780805044554);
    assertEquals(90.0, orientation, 0.1);

    orientation = SphericalGeometryLibrary.getOrientation(lat0, lon0,
        47.65405270558003, -122.30883265428923);
    assertEquals(167.6, orientation, 0.1);

    orientation = SphericalGeometryLibrary.getOrientation(lat0, lon0,
        47.653510677599435, -122.30930472307585);
    assertEquals(192.0, orientation, 0.1);
    
    orientation = SphericalGeometryLibrary.getOrientation(lat0, lon0,
        47.65312041396976, -122.30780805044554);
    assertEquals(270.0, orientation, 0.1);
  }

  @Test
  public void testProjectPointToSegmentApproximate() {

    CoordinatePoint p = new CoordinatePoint(40.737284, -73.955430);
    CoordinatePoint seg1 = new CoordinatePoint(40.737997, -73.955472);
    CoordinatePoint seg2 = new CoordinatePoint(40.734575, -73.954979);

    CoordinatePoint r = SphericalGeometryLibrary.projectPointToSegmentAppropximate(
        p, seg1, seg2);

    assertEquals(40.73729256997116, r.getLat(), 0.0);
    assertEquals(-73.95537051431788, r.getLon(), 0.0);
  }
}
