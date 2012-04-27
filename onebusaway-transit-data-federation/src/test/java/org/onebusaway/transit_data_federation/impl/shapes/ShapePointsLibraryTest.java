/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.impl.shapes;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.XYPoint;
import org.onebusaway.geospatial.services.UTMLibrary;
import org.onebusaway.geospatial.services.UTMProjection;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.ShapePointsFactory;

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
    assertEquals(1.0, pi.point.getX(), 0.0);
    assertEquals(0.0, pi.point.getY(), 0.0);

    pi = result.get(1);
    assertEquals(10.0, pi.point.getX(), 0.0);
    assertEquals(0.5, pi.point.getY(), 0.0);

    pi = result.get(2);
    assertEquals(1.0, pi.point.getX(), 0.0);
    assertEquals(1.0, pi.point.getY(), 0.0);
  }

  @Test
  public void test04() {

    ShapePointsFactory factory = new ShapePointsFactory();
    factory.addPoint(47.66851509562011, -122.29019398384474);
    factory.addPoint(47.66486634286269, -122.29014033966445);
    factory.addPoint(47.66486634286269, -122.29560131721877);
    ShapePoints shapePoints = factory.create();

    UTMProjection projection = UTMLibrary.getProjectionForPoint(
        shapePoints.getLatForIndex(0), shapePoints.getLonForIndex(0));

    ShapePointsLibrary spl = new ShapePointsLibrary();
    List<XYPoint> projectedShapePoints = spl.getProjectedShapePoints(
        shapePoints, projection);

    XYPoint stopPoint = projection.forward(new CoordinatePoint(
        47.664922340500475, -122.29066873484038));

    double[] distanceAlongShape = {0.0, 405.7, 814.0};
    List<PointAndIndex> assignments = spl.computePotentialAssignments(
        projectedShapePoints, distanceAlongShape, stopPoint, 0, 3);
    assertEquals(2, assignments.size());
    PointAndIndex assignment = assignments.get(0);
    assertEquals(398.9, assignment.distanceAlongShape, 0.1);
    assertEquals(39.6, assignment.distanceFromTarget, 0.1);
    assignment = assignments.get(1);
    assertEquals(445.4, assignment.distanceAlongShape, 0.1);
    assertEquals(6.2, assignment.distanceFromTarget, 0.1);
  }

  private XYPoint p(double x, double y) {
    return new XYPoint(x, y);
  }

  private double[] shapePointDistances(List<XYPoint> points) {
    double[] distances = new double[points.size()];
    double accumulatedDistance = 0;
    for (int i = 0; i < points.size(); i++) {
      XYPoint point = points.get(i);
      if (i > 0)
        accumulatedDistance += point.getDistance(points.get(i - 1));
      distances[i] = accumulatedDistance;
    }
    return distances;
  }
}
