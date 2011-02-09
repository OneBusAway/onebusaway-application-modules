package org.onebusaway.geospatial.grid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolygonBean;
import org.onebusaway.geospatial.services.PolylineEncoder;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GridFactoryTest {

  private static final double DELTA = 0.001;

  private static Comparator<CoordinatePoint> POINTS_COMPARATOR = new CoordinatePointComparator();

  private static Comparator<CoordinateBounds> BOUNDS_COMPARATOR = new CoordinateBoundsComparator();

  private static Comparator<EncodedPolygonBean> POLYGON_COMPARATOR = new EncodedPolygonBeanComparator();

  @Test
  public void test01() {

    GridFactory factory = new GridFactory(0.1,0.1);

    CoordinateBounds bounds = new CoordinateBounds(5.05, 4.05, 5.15, 4.15);
    factory.addBounds(bounds);

    List<CoordinateBounds> grid = factory.getGrid();

    assertEquals(4, grid.size());

    Collections.sort(grid, BOUNDS_COMPARATOR);

    checkBounds(grid.get(0), new CoordinateBounds(5.0, 4.0, 5.1, 4.1), DELTA);
    checkBounds(grid.get(1), new CoordinateBounds(5.0, 4.1, 5.1, 4.2), DELTA);
    checkBounds(grid.get(2), new CoordinateBounds(5.1, 4.0, 5.2, 4.1), DELTA);
    checkBounds(grid.get(3), new CoordinateBounds(5.1, 4.1, 5.2, 4.2), DELTA);

    List<EncodedPolygonBean> boundary = factory.getBoundary();
    assertEquals(1, boundary.size());

    EncodedPolygonBean polygon = boundary.get(0);
    assertTrue(polygon.getInnerRings().isEmpty());

    List<CoordinatePoint> points = PolylineEncoder.decode(polygon.getOuterRing());
    points = shiftPoints(points);

    assertEquals(8, points.size());

    assertEqualsPoints(new CoordinatePoint(5.0, 4.0), points.get(0), DELTA);
    assertEqualsPoints(new CoordinatePoint(5.1, 4.0), points.get(1), DELTA);
    assertEqualsPoints(new CoordinatePoint(5.2, 4.0), points.get(2), DELTA);
    assertEqualsPoints(new CoordinatePoint(5.2, 4.1), points.get(3), DELTA);
    assertEqualsPoints(new CoordinatePoint(5.2, 4.2), points.get(4), DELTA);
    assertEqualsPoints(new CoordinatePoint(5.1, 4.2), points.get(5), DELTA);
    assertEqualsPoints(new CoordinatePoint(5.0, 4.2), points.get(6), DELTA);
    assertEqualsPoints(new CoordinatePoint(5.0, 4.1), points.get(7), DELTA);

    factory.addBounds(new CoordinateBounds(5.15, 4.05, 5.26, 4.19));

    grid = factory.getGrid();

    assertEquals(6, grid.size());

    Collections.sort(grid, BOUNDS_COMPARATOR);

    checkBounds(grid.get(0), new CoordinateBounds(5.0, 4.0, 5.1, 4.1), DELTA);
    checkBounds(grid.get(1), new CoordinateBounds(5.0, 4.1, 5.1, 4.2), DELTA);
    checkBounds(grid.get(2), new CoordinateBounds(5.1, 4.0, 5.2, 4.1), DELTA);
    checkBounds(grid.get(3), new CoordinateBounds(5.1, 4.1, 5.2, 4.2), DELTA);
    checkBounds(grid.get(4), new CoordinateBounds(5.2, 4.0, 5.3, 4.1), DELTA);
    checkBounds(grid.get(5), new CoordinateBounds(5.2, 4.1, 5.3, 4.2), DELTA);

    boundary = factory.getBoundary();
    assertEquals(1, boundary.size());

    polygon = boundary.get(0);
    assertTrue(polygon.getInnerRings().isEmpty());

    points = PolylineEncoder.decode(polygon.getOuterRing());
    points = shiftPoints(points);

    assertEquals(10, points.size());

    assertEqualsPoints(new CoordinatePoint(5.0, 4.0), points.get(0), DELTA);
    assertEqualsPoints(new CoordinatePoint(5.1, 4.0), points.get(1), DELTA);
    assertEqualsPoints(new CoordinatePoint(5.2, 4.0), points.get(2), DELTA);
    assertEqualsPoints(new CoordinatePoint(5.3, 4.0), points.get(3), DELTA);
    assertEqualsPoints(new CoordinatePoint(5.3, 4.1), points.get(4), DELTA);
    assertEqualsPoints(new CoordinatePoint(5.3, 4.2), points.get(5), DELTA);
    assertEqualsPoints(new CoordinatePoint(5.2, 4.2), points.get(6), DELTA);
    assertEqualsPoints(new CoordinatePoint(5.1, 4.2), points.get(7), DELTA);
    assertEqualsPoints(new CoordinatePoint(5.0, 4.2), points.get(8), DELTA);
    assertEqualsPoints(new CoordinatePoint(5.0, 4.1), points.get(9), DELTA);
  }

  @Test
  public void test02() {

    GridFactory factory = new GridFactory(1,1);

    CoordinateBounds bounds = new CoordinateBounds(1.5, 2.5, 2.5, 3.5);
    factory.addBounds(bounds);

    List<CoordinateBounds> grid = factory.getGrid();

    assertEquals(4, grid.size());

    Collections.sort(grid, BOUNDS_COMPARATOR);

    checkBounds(grid.get(0), new CoordinateBounds(1.0, 2.0, 2.0, 3.0), DELTA);
    checkBounds(grid.get(1), new CoordinateBounds(1.0, 3.0, 2.0, 4.0), DELTA);
    checkBounds(grid.get(2), new CoordinateBounds(2.0, 2.0, 3.0, 3.0), DELTA);
    checkBounds(grid.get(3), new CoordinateBounds(2.0, 3.0, 3.0, 4.0), DELTA);

    List<EncodedPolygonBean> boundary = factory.getBoundary();
    assertEquals(1, boundary.size());

    EncodedPolygonBean polygon = boundary.get(0);
    assertTrue(polygon.getInnerRings().isEmpty());

    List<CoordinatePoint> points = PolylineEncoder.decode(polygon.getOuterRing());
    points = shiftPoints(points);

    assertEquals(8, points.size());

    assertEqualsPoints(new CoordinatePoint(1.0, 2.0), points.get(0), DELTA);
    assertEqualsPoints(new CoordinatePoint(2.0, 2.0), points.get(1), DELTA);
    assertEqualsPoints(new CoordinatePoint(3.0, 2.0), points.get(2), DELTA);
    assertEqualsPoints(new CoordinatePoint(3.0, 3.0), points.get(3), DELTA);
    assertEqualsPoints(new CoordinatePoint(3.0, 4.0), points.get(4), DELTA);
    assertEqualsPoints(new CoordinatePoint(2.0, 4.0), points.get(5), DELTA);
    assertEqualsPoints(new CoordinatePoint(1.0, 4.0), points.get(6), DELTA);
    assertEqualsPoints(new CoordinatePoint(1.0, 3.0), points.get(7), DELTA);

    factory.addBounds(new CoordinateBounds(5.5, 4.5, 5.6, 4.6));

    grid = factory.getGrid();

    assertEquals(5, grid.size());

    Collections.sort(grid, BOUNDS_COMPARATOR);

    checkBounds(grid.get(0), new CoordinateBounds(1.0, 2.0, 2.0, 3.0), DELTA);
    checkBounds(grid.get(1), new CoordinateBounds(1.0, 3.0, 2.0, 4.0), DELTA);
    checkBounds(grid.get(2), new CoordinateBounds(2.0, 2.0, 3.0, 3.0), DELTA);
    checkBounds(grid.get(3), new CoordinateBounds(2.0, 3.0, 3.0, 4.0), DELTA);
    checkBounds(grid.get(4), new CoordinateBounds(5.0, 4.0, 6.0, 5.0), DELTA);

    boundary = factory.getBoundary();

    assertEquals(2, boundary.size());

    Collections.sort(boundary, POLYGON_COMPARATOR);
    
    polygon = boundary.get(0);
    assertTrue(polygon.getInnerRings().isEmpty());

    points = PolylineEncoder.decode(polygon.getOuterRing());
    points = shiftPoints(points);

    assertEquals(8, points.size());

    assertEqualsPoints(new CoordinatePoint(1.0, 2.0), points.get(0), DELTA);
    assertEqualsPoints(new CoordinatePoint(2.0, 2.0), points.get(1), DELTA);
    assertEqualsPoints(new CoordinatePoint(3.0, 2.0), points.get(2), DELTA);
    assertEqualsPoints(new CoordinatePoint(3.0, 3.0), points.get(3), DELTA);
    assertEqualsPoints(new CoordinatePoint(3.0, 4.0), points.get(4), DELTA);
    assertEqualsPoints(new CoordinatePoint(2.0, 4.0), points.get(5), DELTA);
    assertEqualsPoints(new CoordinatePoint(1.0, 4.0), points.get(6), DELTA);
    assertEqualsPoints(new CoordinatePoint(1.0, 3.0), points.get(7), DELTA);

    polygon = boundary.get(1);
    assertTrue(polygon.getInnerRings().isEmpty());

    points = PolylineEncoder.decode(polygon.getOuterRing());
    points = shiftPoints(points);

    assertEquals(4, points.size());

    assertEqualsPoints(new CoordinatePoint(5.0, 4.0), points.get(0), DELTA);
    assertEqualsPoints(new CoordinatePoint(6.0, 4.0), points.get(1), DELTA);
    assertEqualsPoints(new CoordinatePoint(6.0, 5.0), points.get(2), DELTA);
    assertEqualsPoints(new CoordinatePoint(5.0, 5.0), points.get(3), DELTA);
  }

  /****
   * Private Methods
   ****/

  private void checkBounds(CoordinateBounds a, CoordinateBounds b, double delta) {
    assertEquals(a.getMinLat(), b.getMinLat(), delta);
    assertEquals(a.getMaxLat(), b.getMaxLat(), delta);
    assertEquals(a.getMinLon(), b.getMinLon(), delta);
    assertEquals(a.getMaxLon(), b.getMaxLon(), delta);
  }

  private static void assertEqualsPoints(CoordinatePoint p1,
      CoordinatePoint p2, double delta) {
    assertEquals(p1.getLat(), p2.getLat(), delta);
    assertEquals(p1.getLon(), p2.getLon(), delta);
  }

  private static List<CoordinatePoint> shiftPoints(List<CoordinatePoint> points) {

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
