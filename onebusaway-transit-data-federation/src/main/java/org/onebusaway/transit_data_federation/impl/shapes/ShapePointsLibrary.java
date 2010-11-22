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

  private double _localMinimumThreshold = 20.0;

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

  public List<PointAndIndex> computePotentialAssignments(
      List<XYPoint> projectedShapePoints, double[] shapePointDistance,
      XYPoint targetPoint, int fromIndex, int toIndex) {

    Min<PointAndIndex> min = new Min<PointAndIndex>();
    Min<PointAndIndex> localMin = new Min<PointAndIndex>();
    List<PointAndIndex> localMins = new ArrayList<PointAndIndex>();

    for (int i = fromIndex; i < toIndex - 1; i++) {
      XYPoint from = projectedShapePoints.get(i);
      XYPoint to = projectedShapePoints.get(i + 1);

      XYPoint location = GeometryLibrary.projectPointToSegment(targetPoint,
          from, to);
      double d = location.getDistance(targetPoint);
      double distanceAlongShape = shapePointDistance[i]
          + location.getDistance(from);
      PointAndIndex pindex = new PointAndIndex(location, i, distanceAlongShape);
      min.add(d, pindex);

      if (d <= _localMinimumThreshold)
        localMin.add(d, pindex);

      if (d > _localMinimumThreshold && !localMin.isEmpty()) {
        localMins.add(localMin.getMinElement());
        localMin = new Min<PointAndIndex>();
      }
    }

    if (min.isEmpty())
      return Collections.emptyList();

    if (!localMin.isEmpty())
      localMins.add(localMin.getMinElement());

    if (localMins.isEmpty())
      localMins.add(min.getMinElement());

    Collections.sort(localMins);

    return localMins;
  }
}
