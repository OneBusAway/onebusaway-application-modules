package org.onebusaway.transit_data_federation.impl.otp;

import java.util.List;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data_federation.impl.otp.graph.TransitVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.tp.TPPathVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.tp.TPState;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.algorithm.RemainingWeightHeuristic;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.spt.SPTVertex;

public class TPRemainingWeightHeuristicImpl implements RemainingWeightHeuristic {

  private TraverseOptions _options;

  private boolean _useTransit;

  /**
   * assume that the max average transit speed over a hop is 10 m/s, which is so
   * far true for New York and Portland
   */
  private double _maxTransitSpeed = 10.0;

  @Override
  public double computeInitialWeight(Vertex from, Vertex to,
      TraverseOptions traverseOptions) {

    _options = traverseOptions;
    _useTransit = traverseOptions.getModes().getTransit();

    double maxSpeed = getMaxSpeed();

    return distance(from, to) / maxSpeed;
  }

  @Override
  public double computeForwardWeight(SPTVertex from, Edge edge,
      TraverseResult traverseResult, Vertex target) {

    EdgeNarrative narrative = traverseResult.getEdgeNarrative();
    Vertex v = narrative.getToVertex();

    return computeWeight(traverseResult, target, v);
  }

  @Override
  public double computeReverseWeight(SPTVertex from, Edge edge,
      TraverseResult traverseResult, Vertex target) {

    EdgeNarrative narrative = traverseResult.getEdgeNarrative();
    Vertex v = narrative.getFromVertex();

    return computeWeight(traverseResult, target, v);
  }

  /****
   * Private Methods
   ****/

  private double computeWeight(TraverseResult traverseResult, Vertex target,
      Vertex v) {

    if (v instanceof TPPathVertex) {
      TPPathVertex tpV = (TPPathVertex) v;
      TPState pathState = tpV.getPathState();
      List<Pair<StopEntry>> path = pathState.getPath();
      int index = pathState.getPathIndex();

      double transitDistance = 0.0;
      double walkDistance = 0.0;

      for (int i = index; i < path.size(); i++) {
        Pair<StopEntry> pair = path.get(i);
        StopEntry fromStop = pair.getFirst();
        StopEntry toStop = pair.getSecond();
        transitDistance += SphericalGeometryLibrary.distance(
            fromStop.getStopLat(), fromStop.getStopLon(), toStop.getStopLat(),
            toStop.getStopLon());

        CoordinatePoint dest = null;
        if (i + 1 < path.size()) {
          dest = path.get(i + 1).getFirst().getStopLocation();
        } else {
          dest = new CoordinatePoint(target.getY(), target.getX());
        }

        walkDistance += SphericalGeometryLibrary.distance(
            toStop.getStopLocation(), dest);
      }

      return transitDistance / _maxTransitSpeed + walkDistance / _options.speed;
    }

    double distanceEstimate = distance(v, target);

    double maxSpeed = getMaxSpeedForCurrentState(traverseResult, v);

    return distanceEstimate / maxSpeed;
  }

  private double getMaxSpeed() {

    if (!_useTransit)
      return _options.speed;

    return _maxTransitSpeed;
  }

  private double getMaxSpeedForCurrentState(TraverseResult traverseResult,
      Vertex v) {

    State state = traverseResult.state;

    /**
     * If we can't use transit at all, just use our walking speed
     */
    if (!_useTransit)
      return _options.speed;

    /**
     * If we've ever boarded a transit vehicle, but are now off transit, we can
     * guarantee that we'll never get back on transit. Thus, we can assume
     * walking speed as our max velocity.
     */
    StateData data = state.getData();
    if (data.isEverBoarded() && !(v instanceof TransitVertex))
      return _options.speed;

    return _maxTransitSpeed;
  }

  private double distance(Vertex a, Vertex b) {
    return SphericalGeometryLibrary.distance(a.getY(), a.getX(), b.getY(),
        b.getX());
  }
}
