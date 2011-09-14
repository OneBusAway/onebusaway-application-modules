package org.onebusaway.transit_data_federation.impl.shapes;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.onebusaway.geospatial.model.XYPoint;

public class ShapePointsLibraryTest {

  @Test
  public void test01() {

    ShapePointsLibrary spl = new ShapePointsLibrary();
    spl.setLocalMinimumThreshold(10);

    List<XYPoint> points = new ArrayList<XYPoint>();
    points.add(p(0, 0));
    points.add(p(1, 0));
    points.add(p(2, 0));
    points.add(p(3, 0));
    points.add(p(4, 0));

    double[] shapePointDistances = shapePointDistances(points);

    XYPoint target = p(1, 1);

    List<PointAndIndex> result = spl.computePotentialAssignments(points,
        shapePointDistances, target, 0, points.size());

    assertEquals(1, result.size());

    PointAndIndex pi = result.get(0);
    assertEquals(1.0, pi.point.getX(), 0.0);
    assertEquals(0.0, pi.point.getY(), 0.0);

    target = p(2, 1);

    result = spl.computePotentialAssignments(points, shapePointDistances,
        target, 0, points.size());

    assertEquals(1, result.size());

    pi = result.get(0);
    assertEquals(2.0, pi.point.getX(), 0.0);
    assertEquals(0.0, pi.point.getY(), 0.0);
  }
  
  @Test
  public void test02() {

    ShapePointsLibrary spl = new ShapePointsLibrary();
    spl.setLocalMinimumThreshold(5);

    List<XYPoint> points = new ArrayList<XYPoint>();
    points.add(p(0, 0));
    points.add(p(10, 0));
    points.add(p(10, 1));
    points.add(p(0, 1));

    double[] shapePointDistances = shapePointDistances(points);

    XYPoint target = p(1, 0.5);

    List<PointAndIndex> result = spl.computePotentialAssignments(points,
        shapePointDistances, target, 0, points.size());

    assertEquals(2, result.size());

    PointAndIndex pi = result.get(0);
    assertEquals(1.0, pi.point.getX(), 0.0);
    assertEquals(0.0, pi.point.getY(), 0.0);
    
    pi = result.get(1);
    assertEquals(1.0, pi.point.getX(), 0.0);
    assertEquals(1.0, pi.point.getY(), 0.0);
  }
  
  @Test
  public void test03() {

    ShapePointsLibrary spl = new ShapePointsLibrary();
    spl.setLocalMinimumThreshold(15);

    List<XYPoint> points = new ArrayList<XYPoint>();
    points.add(p(0, 0));
    points.add(p(10, 0));
    points.add(p(10, 1));
    points.add(p(0, 1));

    double[] shapePointDistances = shapePointDistances(points);

    XYPoint target = p(1, 0.5);

    List<PointAndIndex> result = spl.computePotentialAssignments(points,
        shapePointDistances, target, 0, points.size());

    assertEquals(3, result.size());

    PointAndIndex pi = result.get(0);
    assertEquals(10.0, pi.point.getX(), 0.0);
    assertEquals(0.5, pi.point.getY(), 0.0);
    
    pi = result.get(1);
    assertEquals(1.0, pi.point.getX(), 0.0);
    assertEquals(0.0, pi.point.getY(), 0.0);
  }

  private XYPoint p(double x, double y) {
    return new XYPoint(x, y);
  }

  private double[] shapePointDistances(List<XYPoint> points) {
    double[] distances = new double[points.size()];
    double d = 0;
    for (int i = 0; i < points.size(); i++) {
      XYPoint point = points.get(i);
      distances[i] = d;
      if (i > 0)
        d += point.getDistance(points.get(i - 1));
    }
    return distances;
  }
}
