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
