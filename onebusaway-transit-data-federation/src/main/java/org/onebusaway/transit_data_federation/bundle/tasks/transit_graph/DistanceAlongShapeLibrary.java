package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.onebusaway.collections.Min;
import org.onebusaway.geospatial.model.XYPoint;
import org.onebusaway.geospatial.services.UTMLibrary;
import org.onebusaway.geospatial.services.UTMProjection;
import org.onebusaway.transit_data_federation.impl.shapes.PointAndIndex;
import org.onebusaway.transit_data_federation.impl.shapes.ShapePointsLibrary;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DistanceAlongShapeLibrary {

  private static Logger _log = LoggerFactory.getLogger(DistanceAlongShapeLibrary.class);

  private ShapePointsLibrary _shapePointsLibrary = new ShapePointsLibrary();

  public void setLocalMinimumThreshold(double localMinimumThreshold) {
    _shapePointsLibrary.setLocalMinimumThreshold(localMinimumThreshold);
  }

  public PointAndIndex[] getDistancesAlongShape(ShapePoints shapePoints,
      List<StopTimeEntryImpl> stopTimes) {

    PointAndIndex[] stopTimePoints = new PointAndIndex[stopTimes.size()];

    UTMProjection projection = UTMLibrary.getProjectionForPoint(
        shapePoints.getLats()[0], shapePoints.getLons()[0]);

    List<XYPoint> projectedShapePoints = _shapePointsLibrary.getProjectedShapePoints(
        shapePoints, projection);

    double[] shapePointsDistTraveled = shapePoints.getDistTraveled();

    List<List<PointAndIndex>> possibleAssignments = computePotentialAssignments(
        projection, projectedShapePoints, shapePointsDistTraveled, stopTimes);

    double maxDistanceTraveled = shapePointsDistTraveled[shapePointsDistTraveled.length - 1];

    List<PointAndIndex> bestAssignment = computeBestAssignment(shapePoints,
        stopTimes, possibleAssignments, projection, projectedShapePoints);

    for (int i = 0; i < stopTimePoints.length; i++) {
      PointAndIndex pindex = bestAssignment.get(i);
      if (pindex.distanceAlongShape > maxDistanceTraveled) {
        int index = projectedShapePoints.size() - 1;
        XYPoint point = projectedShapePoints.get(index);
        StopEntryImpl stop = stopTimes.get(i).getStop();
        XYPoint stopPoint = projection.forward(stop.getStopLocation());
        double d = stopPoint.getDistance(point);
        pindex = new PointAndIndex(point, index, d, maxDistanceTraveled);
      }
      stopTimePoints[i] = pindex;
    }
    return stopTimePoints;
  }

  private List<List<PointAndIndex>> computePotentialAssignments(
      UTMProjection projection, List<XYPoint> projectedShapePoints,
      double[] shapePointDistance, List<StopTimeEntryImpl> stopTimes) {

    List<List<PointAndIndex>> possibleAssignments = new ArrayList<List<PointAndIndex>>();

    for (StopTimeEntryImpl stopTime : stopTimes) {

      StopEntryImpl stop = stopTime.getStop();
      XYPoint stopPoint = projection.forward(stop.getStopLocation());

      List<PointAndIndex> assignments = _shapePointsLibrary.computePotentialAssignments(
          projectedShapePoints, shapePointDistance, stopPoint, 0,
          projectedShapePoints.size());

      Collections.sort(assignments);

      possibleAssignments.add(assignments);
    }
    return possibleAssignments;
  }

  private List<PointAndIndex> computeBestAssignment(ShapePoints shapePoints,
      List<StopTimeEntryImpl> stopTimes,
      List<List<PointAndIndex>> possibleAssignments, UTMProjection projection,
      List<XYPoint> projectedShapePoints) {

    checkFirstAndLastStop(stopTimes, possibleAssignments, shapePoints,
        projection, projectedShapePoints);

    List<PointAndIndex> currentAssignment = new ArrayList<PointAndIndex>(
        possibleAssignments.size());

    List<Assignment> allValidAssignments = new ArrayList<Assignment>();

    recursivelyConstructAssignments(possibleAssignments, currentAssignment, 0,
        allValidAssignments);

    if (allValidAssignments.isEmpty()) {
      constructError(shapePoints, stopTimes, possibleAssignments, projection);
    }

    Min<Assignment> bestAssignments = new Min<Assignment>();

    for (Assignment validAssignment : allValidAssignments)
      bestAssignments.add(validAssignment.score, validAssignment);

    Assignment bestAssignment = bestAssignments.getMinElement();

    return bestAssignment.assigment;
  }

  /**
   * Special check for an issue with start points where the first stop isn't all
   * that near the start of the shape (the first stop being more of a layover
   * point). If the shape is working against us, the closest point for the first
   * stop can be a point further along the shape, which causes problems.
   */
  private void checkFirstAndLastStop(List<StopTimeEntryImpl> stopTimes,
      List<List<PointAndIndex>> possibleAssignments, ShapePoints shapePoints,
      UTMProjection projection, List<XYPoint> projectedShapePoints) {

    if (possibleAssignments.size() >= 2) {

      PointAndIndex first = possibleAssignments.get(0).get(0);
      PointAndIndex second = possibleAssignments.get(1).get(0);
      if (first.distanceAlongShape > second.distanceAlongShape) {

        StopTimeEntryImpl firstStopTime = stopTimes.get(0);

        _log.warn("snapping first stop time id=" + firstStopTime.getId()
            + " to start of shape");

        XYPoint point = projectedShapePoints.get(0);

        StopEntryImpl stop = firstStopTime.getStop();
        XYPoint stopPoint = projection.forward(stop.getStopLocation());

        double d = stopPoint.getDistance(point);

        possibleAssignments.get(0).add(new PointAndIndex(point, 0, d, 0.0));
      }

      int n = possibleAssignments.size();
      PointAndIndex prev = possibleAssignments.get(n - 2).get(0);
      PointAndIndex last = possibleAssignments.get(n - 1).get(0);
      if (prev.distanceAlongShape > last.distanceAlongShape) {
      }

    }

    if (possibleAssignments.size() > 0) {

      /**
       * We snap the last stop to the end of the shape and add it to the set of
       * possible assignments. In the worst case, it will be a higher-scoring
       * assignment and ignored, but it can help in cases where the stop was
       * weirdly assigned.
       */
      PointAndIndex lastSnapped = getLastStopSnappedToEndOfShape(stopTimes,
          shapePoints, projection, projectedShapePoints);

      possibleAssignments.get(possibleAssignments.size() - 1).add(lastSnapped);
    }
  }

  private PointAndIndex getLastStopSnappedToEndOfShape(
      List<StopTimeEntryImpl> stopTimes, ShapePoints shapePoints,
      UTMProjection projection, List<XYPoint> projectedShapePoints) {

    int i = stopTimes.size() - 1;
    StopTimeEntryImpl lastStopTime = stopTimes.get(i);

    int lastShapePointIndex = projectedShapePoints.size() - 1;
    XYPoint lastShapePoint = projectedShapePoints.get(lastShapePointIndex);
    XYPoint stopLocation = projection.forward(lastStopTime.getStop().getStopLocation());

    double existingDistanceAlongShape = shapePoints.getDistTraveledForIndex(lastShapePointIndex);
    double extraDistanceAlongShape = lastShapePoint.getDistance(stopLocation);
    double distanceAlongShape = existingDistanceAlongShape
        + extraDistanceAlongShape;

    double d = lastShapePoint.getDistance(stopLocation);

    return new PointAndIndex(lastShapePoint, lastShapePointIndex, d,
        distanceAlongShape);
  }

  private void recursivelyConstructAssignments(
      List<List<PointAndIndex>> possibleAssignments,
      List<PointAndIndex> currentAssignment, int i, List<Assignment> best) {

    /**
     * If we've made it through ALL assignments, we have a valid assignment!
     */
    if (i == possibleAssignments.size()) {

      double score = 0;
      for (PointAndIndex p : currentAssignment)
        score += p.distanceFromTarget;
      currentAssignment = new ArrayList<PointAndIndex>(currentAssignment);
      Assignment result = new Assignment(currentAssignment, score);
      best.add(result);
      return;
    }

    List<PointAndIndex> possibleAssignmentsForIndex = possibleAssignments.get(i);

    List<PointAndIndex> validAssignments = new ArrayList<PointAndIndex>();

    double lastDistanceAlongShape = -1;

    if (i > 0) {
      PointAndIndex prev = currentAssignment.get(i - 1);
      lastDistanceAlongShape = prev.distanceAlongShape;
    }

    for (PointAndIndex possibleAssignmentForIndex : possibleAssignmentsForIndex) {
      if (possibleAssignmentForIndex.distanceAlongShape >= lastDistanceAlongShape)
    	 validAssignments.add(possibleAssignmentForIndex);
    }

    /**
     * There is no satisfying assignment for this search tree, so we return
     */
    if (validAssignments.isEmpty()) {
    	/*
    	 * Make one last ditch effort: if we can't find a valid assignment using the last distance along shape, search 
    	 * up to 10 m backwards to see if we can find a match. Sometimes GTFS shape data has small "overlaps" where two
    	 * route lines butt up to each other, etc. that this 10m backwards search can help fix. 
    	 * 
    	 * The NY MTA bus data has such things, e.g. on the X22 at 6th Ave and 34th Street in Manhattan.
    	 */
        for(int z = 0; z < possibleAssignmentsForIndex.size(); z++) {
        	PointAndIndex possibleAssignmentForIndex = possibleAssignmentsForIndex.get(z);
        	if (possibleAssignmentForIndex.distanceAlongShape >= lastDistanceAlongShape - 10)
        		validAssignments.add(possibleAssignmentForIndex);
        }

        if (validAssignments.isEmpty()) {       	
        	return;
        }
    }

    /**
     * For each valid assignment, pop it onto the current assignment and
     * recursively evaluate
     */
    for (PointAndIndex validAssignment : validAssignments) {

      currentAssignment.add(validAssignment);

      recursivelyConstructAssignments(possibleAssignments, currentAssignment,
          i + 1, best);

      currentAssignment.remove(currentAssignment.size() - 1);
    }
  }

  private void constructError(ShapePoints shapePoints,
      List<StopTimeEntryImpl> stopTimes,
      List<List<PointAndIndex>> possibleAssignments, UTMProjection projection) {
    StopTimeEntryImpl first = stopTimes.get(0);
    StopTimeEntryImpl last = stopTimes.get(stopTimes.size() - 1);

    _log.error("We were attempting to compute the distance along a particular trip for each stop time of that trip by snapping them to the shape for that trip.  However, we could not find an assignment for each stop time where the distance traveled along the shape for each stop time was strictly increasing (aka a stop time seemed to travel backwards)");

    _log.error("error constructing stop-time distances along shape for trip="
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
    for (int i = 0; i < shapePoints.getSize(); i++) {
      b.append(shapePoints.getLatForIndex(i));
      b.append(' ');
      b.append(shapePoints.getLonForIndex(i));
      b.append(' ');
      b.append(shapePoints.getDistTraveledForIndex(i));
      b.append('\n');
    }

    _log.error("shape points:\n" + b.toString());

    throw new IllegalStateException();
  }

  private static class Assignment implements Comparable<Assignment> {
    private final List<PointAndIndex> assigment;
    private final double score;

    public Assignment(List<PointAndIndex> assignment, double score) {
      this.assigment = assignment;
      this.score = score;
    }

    @Override
    public int compareTo(Assignment o) {
      return Double.compare(score, o.score);
    }
  }
}
