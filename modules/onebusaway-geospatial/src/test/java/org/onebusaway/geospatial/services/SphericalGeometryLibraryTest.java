package org.onebusaway.geospatial.services;

import static org.junit.Assert.assertEquals;

import org.onebusaway.geospatial.model.CoordinateBounds;

import org.junit.Test;

public class SphericalGeometryLibraryTest {

  @Test
  public void testDistance() {

    double d1 = SphericalGeometryLibrary.distance(47.66855200022102,
        -122.2901701927185, 47.67031486931084, -122.28911876678467);
    assertEquals(211.24, d1, 0.01);

    double d2 = SphericalGeometryLibrary.distance(47.670300419806246,
        -122.29019165039062, 47.67579094347484, -122.30066299438477);
    assertEquals(993.70, d2, 0.01);
  }

  @Test
  public void testBounds() {

    CoordinateBounds bounds = SphericalGeometryLibrary.bounds(47.97527158291236,
        -122.3527193069458, 400);

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
}
