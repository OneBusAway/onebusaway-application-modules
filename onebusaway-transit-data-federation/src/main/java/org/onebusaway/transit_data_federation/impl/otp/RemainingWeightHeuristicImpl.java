package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.impl.otp.graph.TransitVertex;
import org.opentripplanner.routing.algorithm.RemainingWeightHeuristic;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.spt.SPTVertex;

public class RemainingWeightHeuristicImpl implements RemainingWeightHeuristic {

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

    return from.distance(to) / maxSpeed;
  }

  @Override
  public double computeForwardWeight(SPTVertex from, Edge edge,
      TraverseResult traverseResult, Vertex target) {

    EdgeNarrative narrative = traverseResult.getEdgeNarrative();
    Vertex v = narrative.getToVertex();

    double distanceEstimate = v.distance(target);

    double maxSpeed = getMaxSpeedForCurrentState(traverseResult, v);

    return distanceEstimate / maxSpeed;
  }

  @Override
  public double computeReverseWeight(SPTVertex from, Edge edge,
      TraverseResult traverseResult, Vertex target) {

    EdgeNarrative narrative = traverseResult.getEdgeNarrative();
    Vertex v = narrative.getFromVertex();

    double distanceEstimate = v.distance(target);

    double maxSpeed = getMaxSpeedForCurrentState(traverseResult, v);

    return distanceEstimate / maxSpeed;
  }

  /****
   * Private Methods
   ****/

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
}
