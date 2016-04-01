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
package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.collections.Min;
import org.onebusaway.container.ConfigurationParameter;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.XYPoint;
import org.onebusaway.geospatial.services.UTMLibrary;
import org.onebusaway.geospatial.services.UTMProjection;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.shapes.PointAndIndex;
import org.onebusaway.transit_data_federation.impl.shapes.ShapePointsLibrary;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DistanceAlongShapeLibrary {

  private static Logger _log = LoggerFactory.getLogger(DistanceAlongShapeLibrary.class);

  private static NumberFormat _errorFormatter = new DecimalFormat("0.00");

  private ShapePointsLibrary _shapePointsLibrary = new ShapePointsLibrary();

  private double _maxDistanceFromStopToShapePoint = 1000;

  private int _maximumNumberOfPotentialAssignments = Integer.MAX_VALUE;
  
  private boolean _lenientStopShapeAssignment = false;

  private Set<AgencyAndId> _shapeIdsWeHavePrinted = new HashSet<AgencyAndId>();

  /**
   * When computing stop-to-shape matches, we will consider multiple potential
   * matches for a stop if the shape passes by the multiple times, as is the
   * case in a looped route, for example. A potential match from the stop to
   * shape will be considered each time the shape is within the
   * localMinimumThreshold distance of the stop, such that the shape is more
   * than the localMinimumThreshold distance threshold from the stop in between
   * the potential matches.
   * 
   * @param localMinimumThreshold distance in meters
   */
  @ConfigurationParameter
  public void setLocalMinimumThreshold(double localMinimumThreshold) {
    _shapePointsLibrary.setLocalMinimumThreshold(localMinimumThreshold);
  }

  /**
   * If the closest distance from a stop to a shape is more than
   * maxDistanceFromStopToShapePoint in the
   * {@link #getDistancesAlongShape(ShapePoints, List)}, then a
   * {@link StopIsTooFarFromShapeException} will be thrown.
   * 
   * @param maxDistanceFromStopToShapePoint distance in meters
   */
  @ConfigurationParameter
  public void setMaxDistanceFromStopToShapePoint(
      double maxDistanceFromStopToShapePoint) {
    _maxDistanceFromStopToShapePoint = maxDistanceFromStopToShapePoint;
  }

  /**
   * When computing stop-to-shape matches, the potential number of assignments
   * for possible matches can grow so large that checking for the best
   * assignment could take way too long. This is often the case when dealing
   * with malformed shapes.
   * 
   * This parameter controls the maximum number of potential assignments we are
   * willing to consider. If the number of potential assignments is higher than
   * this number, we throw an exception.
   * 
   * @param maximumNumberOfPotentialAssignments
   */
  @ConfigurationParameter
  public void setMaximumNumberOfPotentialAssignment(
      int maximumNumberOfPotentialAssignments) {
    _maximumNumberOfPotentialAssignments = maximumNumberOfPotentialAssignments;
  }

  /**
   * Some shapes (ferries paths for instance) don't match to stops very well.
   * In these cases pick an assignment and hope for the best.
   * @param lenient
   */
  @ConfigurationParameter
  public void setLenientStopShapeAssignment(boolean lenient) {
    _lenientStopShapeAssignment = lenient;
  }
  public PointAndIndex[] getDistancesAlongShape(ShapePoints shapePoints,
      List<StopTimeEntryImpl> stopTimes)
      throws DistanceAlongShapeException {

    PointAndIndex[] stopTimePoints = new PointAndIndex[stopTimes.size()];

    UTMProjection projection = UTMLibrary.getProjectionForPoint(
        shapePoints.getLats()[0], shapePoints.getLons()[0]);

    List<XYPoint> projectedShapePoints = _shapePointsLibrary.getProjectedShapePoints(
        shapePoints, projection);

    double[] shapePointsDistTraveled = shapePoints.getDistTraveled();

    List<List<PointAndIndex>> possibleAssignments = computePotentialAssignments(
        projection, projectedShapePoints, shapePointsDistTraveled, stopTimes);

    pruneUnnecessaryAssignments(possibleAssignments);
    assignmentSanityCheck(shapePoints, stopTimes, possibleAssignments);

    double maxDistanceTraveled = shapePointsDistTraveled[shapePointsDistTraveled.length - 1];

    List<PointAndIndex> bestAssignment = computeBestAssignment(shapePoints,
        stopTimes, possibleAssignments, projection, projectedShapePoints);

    double last = Double.NEGATIVE_INFINITY;
    
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
      
      if (last > pindex.distanceAlongShape) {
        constructError(shapePoints, stopTimes, possibleAssignments, projection);
      }
      last = pindex.distanceAlongShape;
      stopTimePoints[i] = pindex;
    }
    return stopTimePoints;
  }

  /**
   * The {@link ShapePointsLibrary} returns a number of potential assignments
   * for a stop's position along a shape. While some of those potential
   * assignments can help us correctly deal with cases where a shape loops back
   * on itself or other weird configurations, many of the potential assignments
   * are just local minimas that won't ultimately have much affect on the final
   * assignment if they don't overlap with the previous or next stop. To help
   * reduce the set of possible assignments that we'll have to explore, we just
   * pick the best assignment and toss out the rest when the set of assignments
   * for a stop don't overlap with the assignments of the previous or next stop.
   * 
   * @param possibleAssignments
   */
  private void pruneUnnecessaryAssignments(
      List<List<PointAndIndex>> possibleAssignments) {

    double[] mins = new double[possibleAssignments.size()];
    double[] maxs = new double[possibleAssignments.size()];

    for (int i = 0; i < possibleAssignments.size(); ++i) {
      double minScore = Double.POSITIVE_INFINITY;
      double maxScore = Double.NEGATIVE_INFINITY;
      for (PointAndIndex pi : possibleAssignments.get(i)) {
        minScore = Math.min(minScore, pi.distanceAlongShape);
        maxScore = Math.max(maxScore, pi.distanceAlongShape);
      }
      mins[i] = minScore;
      maxs[i] = maxScore;
    }

    for (int i = 0; i < possibleAssignments.size(); i++) {
      List<PointAndIndex> points = possibleAssignments.get(i);
      // If there is only one possible assignment, there is nothing to prune
      if (points.size() == 1)
        continue;
      double currentMin = mins[i];
      double currentMax = maxs[i];
      if (i > 0) {
        double prevMax = maxs[i - 1];
        if (currentMin < prevMax)
          continue;
      }
      if (i + 1 < possibleAssignments.size()) {
        double nextMin = mins[i + 1];
        if (currentMax > nextMin)
          continue;
      }

      Collections.sort(points, PointAndIndex.DISTANCE_FROM_TARGET_COMPARATOR);
      while (points.size() > 1)
        points.remove(points.size() - 1);
    }
  }

  private void assignmentSanityCheck(ShapePoints shapePoints,
      List<StopTimeEntryImpl> stopTimes,
      List<List<PointAndIndex>> possibleAssignments)
      throws DistanceAlongShapeException {

    int stIndex = 0;
    for (List<PointAndIndex> assignments : possibleAssignments) {
      if (assignments.isEmpty()) {
        StopTimeEntry stopTime = stopTimes.get(stIndex);
        throw new InvalidStopToShapeMappingException(stopTime.getTrip());
      }
      Min<PointAndIndex> m = new Min<PointAndIndex>();
      for (PointAndIndex pindex : assignments)
        m.add(pindex.distanceFromTarget, pindex);
      if (m.getMinValue() > _maxDistanceFromStopToShapePoint) {
        StopTimeEntry stopTime = stopTimes.get(stIndex);
        PointAndIndex pindex = m.getMinElement();
        CoordinatePoint point = shapePoints.getPointForIndex(pindex.index);
        throw new StopIsTooFarFromShapeException(stopTime, pindex, point);
      }
      stIndex++;
    }
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

      possibleAssignments.add(assignments);
    }
    return possibleAssignments;
  }

  private List<PointAndIndex> computeBestAssignment(ShapePoints shapePoints,
      List<StopTimeEntryImpl> stopTimes,
      List<List<PointAndIndex>> possibleAssignments, UTMProjection projection,
      List<XYPoint> projectedShapePoints)
      throws InvalidStopToShapeMappingException {

    checkFirstAndLastStop(stopTimes, possibleAssignments, shapePoints,
        projection, projectedShapePoints);

    int startIndex = 0;
    int assingmentCount = 1;

    /**
     * We iterate over each stop, examining its possible assignments. If we find
     * a region of stops where the first and last stop have a single assignment
     * but the stops in-between have multiple assignments, we compute the best
     * assignments for that section. We also handle the edge-cases where the
     * multiple potential assignments occur at the start or end of the route.
     */
    for (int index = 0; index < possibleAssignments.size(); index++) {
      List<PointAndIndex> possibleAssignment = possibleAssignments.get(index);
      int count = possibleAssignment.size();
      if (count == 0) {
        constructErrorForPotentialAssignmentCount(shapePoints, stopTimes, count);
      }

      boolean hasRegion = index > startIndex;
      boolean hasSingleAssignmentFollowingMultipleAssignments = count == 1
          && assingmentCount > 1;
      boolean hasMultipleAssignmentsAndLastPoint = count > 1
          && index == possibleAssignments.size() - 1;

      if (hasRegion
          && (hasSingleAssignmentFollowingMultipleAssignments || hasMultipleAssignmentsAndLastPoint)) {

        List<PointAndIndex> currentAssignment = new ArrayList<PointAndIndex>(
            index - startIndex + 1);
        Min<Assignment> bestAssignments = new Min<Assignment>();
        recursivelyConstructAssignments(possibleAssignments, currentAssignment,
            startIndex, startIndex, index + 1, bestAssignments);
        if (bestAssignments.isEmpty()) {
          constructError(shapePoints, stopTimes, possibleAssignments,
              projection);
        } else {
          List<PointAndIndex> bestAssignment = bestAssignments.getMinElement().assigment;
          for (int bestIndex = 0; bestIndex < bestAssignment.size(); bestIndex++) {
            possibleAssignments.set(startIndex + bestIndex,
                Arrays.asList(bestAssignment.get(bestIndex)));
          }
        }
      }
      if (count == 1) {
        startIndex = index;
        assingmentCount = 1;
      } else {
        assingmentCount *= count;
        if (assingmentCount > _maximumNumberOfPotentialAssignments) {
          constructErrorForPotentialAssignmentCount(shapePoints, stopTimes,
              assingmentCount);
        }
      }
    }

    List<PointAndIndex> bestAssignment = new ArrayList<PointAndIndex>();
    for (List<PointAndIndex> possibleAssignment : possibleAssignments) {
      if (possibleAssignment.size() != 1) {
        String msg = "expected just one assignment at this point, found "
            + possibleAssignment.size() + "; " + "shapePoint="
            + shapePoints.getShapeId() + ", " + "\npossibleAssignments=\n";
        for (PointAndIndex pa : possibleAssignment) {
          msg += "PointAndIndex(index=" + pa.index + ", point=" + pa.point
              + ", distanceAlongShape=" + pa.distanceAlongShape
              + ", distanceFromTarget=" + pa.distanceFromTarget + "), ";
        }
        msg += "\nstopTime=\n";
        for (StopTimeEntryImpl st : stopTimes) {
          msg += "StopTimeEntry(Stop(" + st.getStop().getId() + ":"
              + st.getStop().getStopLat() + ", " + st.getStop().getStopLon()
              + ")" + ", trip=" + st.getTrip().getId() + "), ";
        }
        _log.error(msg);
        if (!_lenientStopShapeAssignment)
          throw new IllegalStateException(msg);    	  
      }
      bestAssignment.add(possibleAssignment.get(0));
    }

    return bestAssignment;
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
      List<PointAndIndex> currentAssignment, int index, int indexFrom,
      int indexTo, Min<Assignment> best) {

    /**
     * If we've made it through ALL assignments, we have a valid assignment!
     */
    if (index == indexTo) {

      double score = 0;
      for (PointAndIndex p : currentAssignment)
        score += p.distanceFromTarget;
      currentAssignment = new ArrayList<PointAndIndex>(currentAssignment);
      Assignment result = new Assignment(currentAssignment, score);
      best.add(score, result);
      return;
    }

    List<PointAndIndex> possibleAssignmentsForIndex = possibleAssignments.get(index);

    List<PointAndIndex> validAssignments = new ArrayList<PointAndIndex>();

    double lastDistanceAlongShape = -1;

    if (index > indexFrom) {
      PointAndIndex prev = currentAssignment.get(index - 1 - indexFrom);
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
      return;
    }

    /**
     * For each valid assignment, pop it onto the current assignment and
     * recursively evaluate
     */
    for (PointAndIndex validAssignment : validAssignments) {

      currentAssignment.add(validAssignment);

      recursivelyConstructAssignments(possibleAssignments, currentAssignment,
          index + 1, indexFrom, indexTo, best);

      currentAssignment.remove(currentAssignment.size() - 1);
    }
  }

  private void constructErrorForPotentialAssignmentCount(
      ShapePoints shapePoints, List<StopTimeEntryImpl> stopTimes, long count)
      throws InvalidStopToShapeMappingException {

    if (count == 0) {
      _log.error("We were attempting to compute the distance along a particular trip for each stop time of that "
          + "trip by snapping them to the shape for that trip.  However, we could not find an assignment for each "
          + "stop time of the trip, which usually indicates that there is something wrong with the underlying "
          + "shape data.  For more information on errors of this kind, see:\n"
          + "  https://github.com/OneBusAway/onebusaway-application-modules/wiki/Stop-to-Shape-Matching");
    } else {
      _log.error("We were attempting to compute the distance along a particular trip for each stop time of that "
          + "trip by snapping them to the shape for that trip.  However, we found WAY TOO MANY potential "
          + "assignments, which usually indicates that there is something wrong with the underlying shape data.  "
          + "For more information on errors of this kind, see:\n"
          + "  https://github.com/OneBusAway/onebusaway-application-modules/wiki/Stop-to-Shape-Matching");
    }

    StopTimeEntryImpl first = stopTimes.get(0);
    TripEntryImpl trip = first.getTrip();
    StopTimeEntryImpl last = stopTimes.get(stopTimes.size() - 1);

    _log.error("error constructing stop-time distances along shape for trip="
        + trip.getId() + " shape=" + trip.getShapeId() + " firstStopTime="
        + first.getId() + " lastStopTime=" + last.getId());

    if (_shapeIdsWeHavePrinted.add(trip.getShapeId())) {
      StringBuilder b = new StringBuilder();
      for (int i = 0; i < shapePoints.getSize(); i++) {
        b.append(shapePoints.getLatForIndex(i));
        b.append(' ');
        b.append(shapePoints.getLonForIndex(i));
        b.append(' ');
        b.append(shapePoints.getDistTraveledForIndex(i));
        b.append('\n');
      }
      _log.error("shape points:\n" + b.toString());
    }
    throw new InvalidStopToShapeMappingException(first.getTrip());
  }

  private void constructError(ShapePoints shapePoints,
      List<StopTimeEntryImpl> stopTimes,
      List<List<PointAndIndex>> possibleAssignments, UTMProjection projection)
      throws InvalidStopToShapeMappingException {

    StopTimeEntryImpl first = stopTimes.get(0);
    StopTimeEntryImpl last = stopTimes.get(stopTimes.size() - 1);

    _log.error("We were attempting to compute the distance along a particular trip for each stop time of that trip by "
        + "snapping them to the shape for that trip.  However, we could not find an assignment for each stop time "
        + "where the distance traveled along the shape for each stop time was strictly increasing (aka a stop time "
        + "seemed to travel backwards).  For more information on errors of this kind, see:\n"
        + "  https://github.com/OneBusAway/onebusaway-application-modules/wiki/Stop-to-Shape-Matching");

    TripEntryImpl trip = first.getTrip();
    _log.error("error constructing stop-time distances along shape for trip="
        + trip.getId() + " shape=" + trip.getShapeId() + " firstStopTime="
        + first.getId() + " lastStopTime=" + last.getId());

    StringBuilder b = new StringBuilder();
    int index = 0;

    b.append("# potential assignments:\n");
    b.append("# stopLat stopLon stopId\n");
    b.append("#   locationOnShapeLat locationOnShapeLon distanceAlongShape distanceFromShape shapePointIndex\n");
    b.append("#   ...\n");

    double prevMaxDistanceAlongShape = Double.NEGATIVE_INFINITY;

    for (List<PointAndIndex> possible : possibleAssignments) {
      StopTimeEntryImpl stopTime = stopTimes.get(index);
      StopEntryImpl stop = stopTime.getStop();
      b.append(stop.getStopLat());
      b.append(' ');
      b.append(stop.getStopLon());
      b.append(' ');
      b.append(index);
      b.append(' ');
      b.append(stop.getId());
      b.append('\n');

      double maxDistanceAlongShape = Double.NEGATIVE_INFINITY;
      double minDistanceAlongShape = Double.POSITIVE_INFINITY;

      for (PointAndIndex pindex : possible) {
        b.append("  ");
        b.append(projection.reverse(pindex.point));
        b.append(' ');
        b.append(_errorFormatter.format(pindex.distanceAlongShape));
        b.append(' ');
        b.append(_errorFormatter.format(pindex.distanceFromTarget));
        b.append(' ');
        b.append(pindex.index);
        b.append("\n");
        maxDistanceAlongShape = Math.max(maxDistanceAlongShape,
            pindex.distanceAlongShape);
        minDistanceAlongShape = Math.min(minDistanceAlongShape,
            pindex.distanceAlongShape);
      }

      if (minDistanceAlongShape < prevMaxDistanceAlongShape) {
        b.append("    ^ potential problem here ^\n");
      }

      prevMaxDistanceAlongShape = maxDistanceAlongShape;

      index++;
    }
    _log.error(b.toString());

    if (_shapeIdsWeHavePrinted.add(trip.getShapeId())) {
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
    }

    throw new InvalidStopToShapeMappingException(first.getTrip());
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

  public static class DistanceAlongShapeException extends Exception {

    private static final long serialVersionUID = 1L;

    public DistanceAlongShapeException(String message) {
      super(message);
    }
  }

  public static class StopIsTooFarFromShapeException extends
      DistanceAlongShapeException {

    private static final long serialVersionUID = 1L;
    private final StopTimeEntry _stopTime;
    private final PointAndIndex _pointAndIndex;
    private final CoordinatePoint _point;

    public StopIsTooFarFromShapeException(StopTimeEntry stopTime,
        PointAndIndex pointAndIndex, CoordinatePoint point) {
      super("stopTime=" + stopTime + " pointAndIndex=" + pointAndIndex
          + " point=" + point);
      _stopTime = stopTime;
      _pointAndIndex = pointAndIndex;
      _point = point;
    }

    public StopTimeEntry getStopTime() {
      return _stopTime;
    }

    public PointAndIndex getPointAndIndex() {
      return _pointAndIndex;
    }

    public CoordinatePoint getPoint() {
      return _point;
    }
  }

  public static class InvalidStopToShapeMappingException extends
      DistanceAlongShapeException {

    private static final long serialVersionUID = 1L;

    private final TripEntry _trip;

    public InvalidStopToShapeMappingException(TripEntry trip) {
      super("trip=" + trip.getId());
      _trip = trip;
    }

    public TripEntry getTrip() {
      return _trip;
    }
  }
}
