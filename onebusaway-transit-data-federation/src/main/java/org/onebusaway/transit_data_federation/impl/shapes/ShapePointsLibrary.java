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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.onebusaway.collections.Min;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.XYPoint;
import org.onebusaway.geospatial.services.GeometryLibrary;
import org.onebusaway.geospatial.services.UTMProjection;
import org.onebusaway.transit_data_federation.model.ShapePoints;

public class ShapePointsLibrary {

  private double _localMinimumThreshold = 50.0;

  public ShapePointsLibrary() {

  }

  public ShapePointsLibrary(double localMinimumThreshold) {
    _localMinimumThreshold = localMinimumThreshold;
  }

  /**
   * When searching for the closest point along a block's shape, there may be
   * multiple local minimums as a block potentially loops back on itself. This
   * threshold (in meters) determines how close a local minimum should be if it
   * is to be considered.
   */
  public void setLocalMinimumThreshold(double localMinimumThreshold) {
    _localMinimumThreshold = localMinimumThreshold;
  }

  public List<XYPoint> getProjectedShapePoints(ShapePoints shapePoints,
      UTMProjection projection) {
    List<XYPoint> projectedShapePoints = new ArrayList<XYPoint>();

    double[] lats = shapePoints.getLats();
    double[] lons = shapePoints.getLons();
    int n = lats.length;

    for (int i = 0; i < n; i++)
      projectedShapePoints.add(projection.forward(new CoordinatePoint(lats[i],
          lons[i])));
    return projectedShapePoints;
  }

  /**
   * Here is the idea:
   * 
   * Given a shape as an array of XY Points, a range over those points, and a
   * target point, find the closest location(s) along the shape for the target
   * point.
   * 
   * The trick is that there may be multiple good assignments, especially for a
   * shape that loops back on itself. In this case, we look for assignments
   * within our {@link #_localMinimumThreshold} distance. There will typically
   * be ranges of assignments that apply and we take the local min within each
   * range. We then return each of these local mins as a potential assignment.
   * 
   * There is an additional wrinkle when the section of the shape that loops
   * back occurs where all the shape points are within
   * {@link #_localMinimumThreshold} of the target point. In this case, we no
   * longer consider the local min for the section, but the individual local
   * mins within the section. That is to say, if we find a local min and then
   * the shape moves away and comes back, we'll consider the second local min as
   * a new potential assignment.
   * 
   * If no assignments are found within the {@link #_localMinimumThreshold}
   * distance, we just return the global min.
   * 
   * @param projectedShapePoints
   * @param shapePointDistance
   * @param targetPoint
   * @param fromIndex
   * @param toIndex
   * @return
   */
  public List<PointAndIndex> computePotentialAssignments(
      List<XYPoint> projectedShapePoints, double[] shapePointDistance,
      XYPoint targetPoint, int fromIndex, int toIndex) {

    /**
     * The absolute closest assignment
     */
    Min<PointAndIndex> min = new Min<PointAndIndex>();

    /**
     * 
     */
    Min<PointAndIndex> localMin = new Min<PointAndIndex>();

    List<PointAndIndex> localMins = new ArrayList<PointAndIndex>();

    /**
     * For our multi-local min within the _localMinimumThreshold detection, we
     * keep track of whether the endpoint of the previous segment was further
     * away from the target point than the snapped point, indicating the shape
     * is moving away from the target point. We also keep track of the distance
     * of the target point from the endpoint of the previous segment. If the
     * snapped point of the next section is closer to the target point than the
     * previous endpoint AND the shape was moving away from the target point in
     * the previous section, we establish a new local min.
     */
    boolean previousEndpointDistanceGreaterThanSnappedDistance = false;
    double previousEndpointDistance = Double.POSITIVE_INFINITY;

    for (int i = fromIndex; i < toIndex - 1; i++) {
      XYPoint from = projectedShapePoints.get(i);
      XYPoint to = projectedShapePoints.get(i + 1);

      XYPoint location = GeometryLibrary.projectPointToSegment(targetPoint,
          from, to);
      double d = location.getDistance(targetPoint);
      double distanceAlongShape = shapePointDistance[i]
          + location.getDistance(from);
      PointAndIndex pindex = new PointAndIndex(location, i, d,
          distanceAlongShape);
      min.add(d, pindex);

      if (d <= _localMinimumThreshold) {
        if (previousEndpointDistanceGreaterThanSnappedDistance
            && d < previousEndpointDistance && !localMin.isEmpty()) {
          localMins.add(localMin.getMinElement());
          localMin = new Min<PointAndIndex>();
        }
        localMin.add(d, pindex);
      } else if (!localMin.isEmpty()) {
        localMins.add(localMin.getMinElement());
        localMin = new Min<PointAndIndex>();
      }

      previousEndpointDistance = to.getDistance(targetPoint);
      previousEndpointDistanceGreaterThanSnappedDistance = previousEndpointDistance > d;
    }

    /**
     * If don't have ANY potential assignments, return an empty list
     */
    if (min.isEmpty())
      return Collections.emptyList();

    /**
     * Check to see if we have a localMin element that needs to be added to our
     * collection of local mins
     */
    if (!localMin.isEmpty())
      localMins.add(localMin.getMinElement());

    /**
     * If we don't have ANY local mins (aka assignments that were within our
     * _localMinimumThreshold), we just use the best assigment(s) from the
     * global min
     */
    if (localMins.isEmpty())
      localMins.addAll(min.getMinElements());

    return localMins;
  }
}
