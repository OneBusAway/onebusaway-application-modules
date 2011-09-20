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

import org.onebusaway.geospatial.model.Point;
import org.onebusaway.geospatial.model.PointVector;

public class GeometryLibrary {

  /***************************************************************************
   * General Methods
   **************************************************************************/

  /**
     * 
     */
  public static final boolean inRange(double r1, double r2, double v) {
    return Math.min(r1, r2) <= v && v <= Math.max(r1, r2);
  }

  /***************************************************************************
   * Angle Methods
   **************************************************************************/

  public static double normalizeAngle(double radians) {
    while (radians > Math.PI)
      radians -= Math.PI * 2;
    while (radians < -Math.PI)
      radians += Math.PI * 2;
    return radians;
  }

  public static double getAngleDifference(double radiansA, double radiansB) {
    double diff = Math.abs(normalizeAngle(radiansA) - normalizeAngle(radiansB));
    if (diff > Math.PI)
      diff = 2 * Math.PI - diff;
    return diff;
  }

  public static double getRelativeAngleDifference(double from, double to) {
    from = normalizeAngle(from);
    to = normalizeAngle(to);
    double delta = to - from;
    if (delta > Math.PI)
      delta -= Math.PI * 2;
    if (delta < -Math.PI)
      delta += Math.PI * 2;
    return delta;
  }

  /***************************************************************************
     * 
     **************************************************************************/

  public static <P extends Point> double distance(P a, P b) {
    return a.getDistance(b);
  }

  public static <P extends Point> double getAngle(P origin, P a, P b) {
    PointVector va = PointVector.create(origin, a);
    PointVector vb = PointVector.create(origin, b);
    return va.getAngle(vb);
  }

  public static <P extends Point> P projectPointToLine(P point, P lineStart,
      P lineEnd) {
    PointVector v = PointVector.create(lineStart, point);
    PointVector line = PointVector.create(lineStart, lineEnd);
    return line.getProjection(v).addToPoint(lineStart);
  }

  public static <P extends Point> P projectPointToSegment(P point,
      P segmentStart, P segmentEnd) {
    PointVector v = PointVector.create(segmentStart, point);
    PointVector line = PointVector.create(segmentStart, segmentEnd);
    P p = line.getProjection(v).addToPoint(segmentStart);
    if (isBetween(p, segmentStart, segmentEnd))
      return p;
    double a = p.getDistance(segmentStart);
    double b = p.getDistance(segmentEnd);
    return a <= b ? segmentStart : segmentEnd;
  }

  /**
   * Consider a line segment defined by points 'lineStart' and 'lineEnd'.
   * Consider two planes, each perpendicular to the line segment and each
   * intersecting one of the endpoints of the segment. Method returns true when
   * the target 'point' falls on or between these two planes.
   * 
   * @param point
   * @param lineStart
   * @param lineEnd
   * @return
   */
  public static <P extends Point> boolean isBetween(P point, P lineStart,
      P lineEnd) {
    if (point.equals(lineStart) || point.equals(lineEnd))
      return true;
    P proj = projectPointToLine(point, lineStart, lineEnd);
    double segmentLength = lineStart.getDistance(lineEnd);
    double lenA = lineStart.getDistance(proj);
    double lenB = lineEnd.getDistance(proj);
    return lenA <= segmentLength && lenB <= segmentLength;
  }
}
