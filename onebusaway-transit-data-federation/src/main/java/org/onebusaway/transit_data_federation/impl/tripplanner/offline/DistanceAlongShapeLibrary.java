package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.onebusaway.collections.Min;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.XYPoint;
import org.onebusaway.geospatial.services.GeometryLibrary;
import org.onebusaway.geospatial.services.UTMLibrary;
import org.onebusaway.geospatial.services.UTMProjection;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistanceAlongShapeLibrary {

  private static Logger _log = LoggerFactory.getLogger(DistanceAlongShapeLibrary.class);

  private double _localMinimumThreshold = 20.0;

  public void setLocalMinimumThreshold(double localMinimumThreshold) {
    _localMinimumThreshold = localMinimumThreshold;
  }

  public double[] getDistancesAlongShape(ShapePoints shapePoints,
      List<StopTimeEntryImpl> stopTimes) {

    double[] distances = new double[stopTimes.size()];

    UTMProjection projection = UTMLibrary.getProjectionForPoint(
        shapePoints.getLats()[0], shapePoints.getLons()[0]);

    List<XYPoint> projectedShapePoints = getProjectedShapePoints(shapePoints,
        projection);

    List<List<PointAndIndex>> possibleAssignments = computePotentialAssignments(
        projection, projectedShapePoints, shapePoints.getDistTraveled(),
        stopTimes);

    List<PointAndIndex> bestAssignment = computeBestAssignment(shapePoints,
        stopTimes, possibleAssignments, projection, projectedShapePoints);

    for (int i = 0; i < distances.length; i++) {
      PointAndIndex pindex = bestAssignment.get(i);
      distances[i] = pindex.distanceAlongShape;
    }
    return distances;
  }

  private List<XYPoint> getProjectedShapePoints(ShapePoints shapePoints,
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

  private List<List<PointAndIndex>> computePotentialAssignments(
      UTMProjection projection, List<XYPoint> projectedShapePoints,
      double[] shapePointDistance, List<StopTimeEntryImpl> stopTimes) {

    List<List<PointAndIndex>> possibleAssignments = new ArrayList<List<PointAndIndex>>();

    for (StopTimeEntryImpl stopTime : stopTimes) {

      StopEntryImpl stop = stopTime.getStop();
      XYPoint stopPoint = projection.forward(stop.getStopLocation());

      Min<PointAndIndex> min = new Min<PointAndIndex>();
      Min<PointAndIndex> localMin = new Min<PointAndIndex>();
      List<PointAndIndex> localMins = new ArrayList<PointAndIndex>();

      for (int i = 0; i < projectedShapePoints.size() - 1; i++) {
        XYPoint from = projectedShapePoints.get(i);
        XYPoint to = projectedShapePoints.get(i + 1);

        XYPoint location = GeometryLibrary.projectPointToSegment(stopPoint,
            from, to);
        double d = location.getDistance(stopPoint);
        double distanceAlongShape = shapePointDistance[i]
            + location.getDistance(from);
        PointAndIndex pindex = new PointAndIndex(location, i,
            distanceAlongShape);
        min.add(d, pindex);

        if (d <= _localMinimumThreshold)
          localMin.add(d, pindex);

        if (d > _localMinimumThreshold && !localMin.isEmpty()) {
          localMins.add(localMin.getMinElement());
          localMin = new Min<PointAndIndex>();
        }
      }

      if (!localMin.isEmpty())
        localMins.add(localMin.getMinElement());

      if (localMins.isEmpty())
        localMins.add(min.getMinElement());

      Collections.sort(localMins);

      possibleAssignments.add(localMins);
    }
    return possibleAssignments;
  }

  private List<PointAndIndex> computeBestAssignment(ShapePoints shapePoints,
      List<StopTimeEntryImpl> stopTimes,
      List<List<PointAndIndex>> possibleAssignments, UTMProjection projection,
      List<XYPoint> projectedShapePoints) {

    List<PointAndIndex> bestAssignment = new ArrayList<DistanceAlongShapeLibrary.PointAndIndex>();

    double lastDistanceAlongShape = -1;

    /**
     * Special check for an issue with start points where the first stop isn't
     * all that near the start of the shape (the first stop being more of a
     * layover point). If the shape is working against us, the closest point for
     * the first stop can be a point further along the shape, which causes
     * problems.
     */
    if (possibleAssignments.size() >= 2) {

      PointAndIndex first = possibleAssignments.get(0).get(0);
      PointAndIndex second = possibleAssignments.get(1).get(0);
      if (first.distanceAlongShape > second.distanceAlongShape) {
        StopTimeEntryImpl firstStopTime = stopTimes.get(0);
        _log.warn("snapping first stop of trip="
            + firstStopTime.getTrip().getId() + " to start of shape");
        possibleAssignments.get(0).add(
            new PointAndIndex(projectedShapePoints.get(0), 0, 0.0));
      }

      int n = possibleAssignments.size();
      PointAndIndex prev = possibleAssignments.get(n - 2).get(0);
      PointAndIndex last = possibleAssignments.get(n - 1).get(0);
      if (prev.distanceAlongShape > last.distanceAlongShape) {
      }

    }

    for (int i = 0; i < possibleAssignments.size(); i++) {

      List<PointAndIndex> assignments = possibleAssignments.get(i);

      Min<PointAndIndex> min = new Min<PointAndIndex>();

      for (PointAndIndex assignment : assignments) {
        if (assignment.distanceAlongShape >= lastDistanceAlongShape)
          min.add(assignment.distanceAlongShape, assignment);
      }

      if (min.isEmpty()) {

        // Is it the last point? We might make an exception
        if (i == possibleAssignments.size() - 1) {

          StopTimeEntryImpl lastStopTime = stopTimes.get(i);
          _log.warn("snapping last stop of trip="
              + lastStopTime.getTrip().getId() + " to end of shape");
          int lastShapePointIndex = projectedShapePoints.size() - 1;
          XYPoint lastShapePoint = projectedShapePoints.get(lastShapePointIndex);
          XYPoint stopLocation = projection.forward(lastStopTime.getStop().getStopLocation());
          double existingDistance = shapePoints.getDistTraveledForIndex(lastShapePointIndex);
          double extraDistance = lastShapePoint.getDistance(stopLocation);
          double distance = existingDistance + extraDistance;
          min.add(0, new PointAndIndex(stopLocation, lastShapePointIndex,
              distance));

        } else {
          constructError(stopTimes, possibleAssignments, projection,
              bestAssignment);
        }
      }

      PointAndIndex best = min.getMinElement();
      bestAssignment.add(best);
      lastDistanceAlongShape = best.distanceAlongShape;
    }

    return bestAssignment;
  }

  private void constructError(List<StopTimeEntryImpl> stopTimes,
      List<List<PointAndIndex>> possibleAssignments, UTMProjection projection,
      List<PointAndIndex> bestAssignment) {
    StopTimeEntryImpl first = stopTimes.get(0);
    StopTimeEntryImpl last = stopTimes.get(stopTimes.size() - 1);

    _log.error("error constructing distances along shape for trip="
        + first.getTrip().getId() + " firstStopTime=" + first.getId()
        + " lastStopTime=" + last.getId());
    StringBuilder b = new StringBuilder();
    int index = 0;
    for (List<PointAndIndex> possible : possibleAssignments) {
      b.append(index);
      for (PointAndIndex pindex : possible) {
        b.append(' ');
        b.append(pindex.distanceAlongShape);
        b.append(' ');
        b.append(projection.reverse(pindex.point));
        b.append(' ');
        b.append(pindex.index);
      }
      b.append("\n");
      index++;
    }
    _log.error("potential assignments:\n" + b.toString());

    b = new StringBuilder();
    index = 0;

    for (PointAndIndex pindex : bestAssignment) {
      b.append(index);
      b.append(' ');
      b.append(pindex.distanceAlongShape);
      b.append(' ');
      b.append(projection.reverse(pindex.point));
      b.append(' ');
      b.append(pindex.index);
      b.append('\n');
      index++;
    }

    _log.error("best assignment:\n" + b.toString());

    throw new IllegalStateException();
  }

  private static class PointAndIndex implements Comparable<PointAndIndex> {

    public XYPoint point;
    public int index;
    public double distanceAlongShape;

    public PointAndIndex(XYPoint point, int index, double distanceAlongShape) {
      this.point = point;
      this.index = index;
      this.distanceAlongShape = distanceAlongShape;
    }

    @Override
    public int compareTo(PointAndIndex o) {
      if (distanceAlongShape == o.distanceAlongShape)
        return 0;
      return distanceAlongShape < o.distanceAlongShape ? -1 : 1;
    }
  }
}
