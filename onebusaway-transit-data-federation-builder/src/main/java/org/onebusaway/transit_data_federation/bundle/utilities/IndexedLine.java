/**
 * Copyright (C) 2014 Kurt Raschke <kurt@kurtraschke.com>
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
package org.onebusaway.transit_data_federation.bundle.utilities;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.utility.InterpolationLibrary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Class for computing distances (in meters) between arbitrary points along a
 * line, where the points defining the line are associated with distances in
 * some arbitrary unit, and the points to be measured are identified in the same
 * unit.
 *
 */
public class IndexedLine {

  private final Logger _log = LoggerFactory.getLogger(IndexedLine.class);

  private final NavigableMap<Double, Point> _distanceToPointMap = new TreeMap<Double, Point>();
  private final NavigableMap<Integer, Point> _indexToPointMap = new TreeMap<Integer, Point>();

  public IndexedLine() {

  }

  public void addPoint(int index, double distance, CoordinatePoint point) {
    Point p = new Point(index, distance, point);
    _distanceToPointMap.put(p.getDistance(), p);
    _indexToPointMap.put(p.getIndex(), p);
  }

  public int interpolateIndex(double measure) {
    Pair<Point> neighborPoints = closestNeighbors(measure);

    if (neighborPoints.isReflexive()) {
      return neighborPoints.getFirst().getIndex();
    }

    double fraction = ((measure - neighborPoints.getFirst().getDistance()) / (neighborPoints.getSecond().getDistance() - neighborPoints.getFirst().getDistance()));

    return ((fraction >= 0.5) ? neighborPoints.getSecond().getIndex() : neighborPoints.getFirst().getIndex());

  }

  public double interpolateDistance(double measureOne, double measureTwo) {
    List<CoordinatePoint> points = new ArrayList<CoordinatePoint>();

    Pair<Point> firstPointNeighbors = closestNeighbors(measureOne);
    Pair<Point> lastPointNeighbors = closestNeighbors(measureTwo);

    CoordinatePoint firstPoint = interpolatePoint(firstPointNeighbors, measureOne);

    points.add(firstPoint);

    for (Point p : _distanceToPointMap.subMap(measureOne, false, measureTwo, false).values()) {
      points.add(p.getPoint());
    }

    CoordinatePoint lastPoint = interpolatePoint(lastPointNeighbors, measureTwo);
    points.add(lastPoint);

    return sumDistance(points);
  }

  private Pair<Point> closestNeighbors(double measure) {
    Map.Entry<Double, Point> e1 = _distanceToPointMap.floorEntry(measure);
    Map.Entry<Double, Point> e2 = _distanceToPointMap.ceilingEntry(measure);

    /*
     * If we're off the end of the shape at either end, then use the
     * first two/last two points as appropriate and interpolate from there.
     */
    if (e1 == null) {
      e1 = _distanceToPointMap.firstEntry();
      e2 = _distanceToPointMap.higherEntry(e1.getKey());
    } else if (e2 == null) {
      e2 = _distanceToPointMap.lastEntry();
      e1 = _distanceToPointMap.lowerEntry(e2.getKey());
    }

    return Tuples.pair(e1.getValue(), e2.getValue());
  }

  private CoordinatePoint interpolatePoint(Pair<Point> neighborPoints, double measure) {
    if (neighborPoints.isReflexive()) {
      return neighborPoints.getFirst().getPoint();
    }

    double fraction = (measure - neighborPoints.getFirst().getDistance()) / (neighborPoints.getSecond().getDistance() - neighborPoints.getFirst().getDistance());

    CoordinatePoint pointOne = neighborPoints.getFirst().getPoint();
    CoordinatePoint pointTwo = neighborPoints.getSecond().getPoint();

    return new CoordinatePoint(InterpolationLibrary.interpolatePair(pointOne.getLat(), pointTwo.getLat(), fraction),
            InterpolationLibrary.interpolatePair(pointOne.getLon(), pointTwo.getLon(), fraction));

  }

  private static double sumDistance(Iterable<CoordinatePoint> points) {
    double d = 0;
    CoordinatePoint last = null;
    for (CoordinatePoint p : points) {
      if (last != null) {
        d += SphericalGeometryLibrary.distance(last, p);
      }
      last = p;
    }

    return d;
  }

  private class Point {

    private int index;
    private double distance;
    private CoordinatePoint point;

    public Point(int index, double distance, CoordinatePoint point) {
      this.index = index;
      this.distance = distance;
      this.point = point;
    }

    public int getIndex() {
      return index;
    }

    public void setIndex(int index) {
      this.index = index;
    }

    public double getDistance() {
      return distance;
    }

    public void setDistance(double distance) {
      this.distance = distance;
    }

    public CoordinatePoint getPoint() {
      return point;
    }

    public void setPoint(CoordinatePoint point) {
      this.point = point;
    }
  }
}
