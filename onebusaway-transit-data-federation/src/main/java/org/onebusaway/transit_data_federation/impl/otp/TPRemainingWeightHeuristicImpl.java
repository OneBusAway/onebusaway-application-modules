package org.onebusaway.transit_data_federation.impl.otp;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.HubNode;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferNode;
import org.onebusaway.transit_data_federation.impl.otp.graph.TransitVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.tp.HasPathStateVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.tp.TPState;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.algorithm.strategies.RemainingWeightHeuristic;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;

public class TPRemainingWeightHeuristicImpl implements RemainingWeightHeuristic {

  private TraverseOptions _options;

  private boolean _useTransit;

  /**
   * assume that the max average transit speed over a hop is 10 m/s, which is so
   * far true for New York and Portland
   */
  private double _maxTransitSpeed = 10.0;

  @Override
  public void reset() {
    _options = null;
    _useTransit = false;
  }

  @Override
  public double computeInitialWeight(State s, Vertex target) {

    _options = s.getOptions();
    _useTransit = _options.getModes().getTransit();

    double maxSpeed = getMaxSpeed();

    return distance(s.getVertex(), target) / maxSpeed;
  }

  @Override
  public double computeForwardWeight(State s, Vertex target) {

    EdgeNarrative narrative = s.getBackEdgeNarrative();
    Vertex v = narrative.getToVertex();

    return computeWeight(s, target, v);
  }

  @Override
  public double computeReverseWeight(State s, Vertex target) {

    EdgeNarrative narrative = s.getBackEdgeNarrative();
    Vertex v = narrative.getFromVertex();

    return computeWeight(s, target, v);
  }

  /****
   * Private Methods
   ****/

  private double computeWeight(State state, Vertex target, Vertex v) {

    if (v instanceof HasPathStateVertex) {
      HasPathStateVertex tpV = (HasPathStateVertex) v;

      TPState pathState = tpV.getPathState();
      TransferNode node = pathState.getNode();

      boolean isFromSourceStop = tpV.isDeparture() ^ pathState.isReverse();

      CoordinatePoint dest = new CoordinatePoint(target.getY(), target.getX());
      Set<TransferNode> visitedNodes = new HashSet<TransferNode>();
      return getWeightForTransferNode(null, node, isFromSourceStop, dest,
          visitedNodes);
    }

    double distanceEstimate = distance(v, target);

    double maxSpeed = getMaxSpeedForCurrentState(state, v);

    return distanceEstimate / maxSpeed;
  }

  private double getWeightForTransferNode(TransferNode parentNode,
      TransferNode node, boolean isFromSourceStop, CoordinatePoint target,
      Set<TransferNode> visitedNodes) {

    visitedNodes.add(node);

    double w = 0;

    if (isFromSourceStop) {

      if (parentNode != null) {
        StopEntry fromStop = parentNode.getToStop();
        StopEntry toStop = node.getFromStop();
        double transferWeight = computeTransferWeight(
            fromStop.getStopLocation(), toStop.getStopLocation());
        w += transferWeight;
      }

      StopEntry fromStop = node.getFromStop();
      StopEntry toStop = node.getToStop();

      int transitWeight = computeTransitWeight(fromStop.getStopLocation(),
          toStop.getStopLocation());
      w += transitWeight;
    }

    /**
     * What's our best option?
     */
    double minOption = node.getMinRemainingWeight();

    if (minOption < 0) {

      minOption = Double.POSITIVE_INFINITY;

      /**
       * We could exit if we're allowed, walking to our destination
       */
      if (node.isExitAllowed()) {

        StopEntry toStop = node.getToStop();

        double transferWeight = computeTransferWeight(toStop.getStopLocation(),
            target);
        minOption = Math.min(transferWeight, minOption);
      }

      /**
       * Or we could transfer to another transfer pattern
       */
      Collection<TransferNode> transfers = node.getTransfers();
      for (TransferNode subTree : transfers) {
        if (!visitedNodes.contains(subTree)) {
          double subWeight = getWeightForTransferNode(node, subTree, true,
              target, visitedNodes);
          minOption = Math.min(subWeight, minOption);
        }
      }
      Collection<HubNode> hubs = node.getHubs();
      for (HubNode hubNode : hubs) {
        StopEntry hubStop = hubNode.getHubStop();
        CoordinatePoint hubLocation = hubStop.getStopLocation();
        double transferWeight = computeTransferWeight(
            node.getToStop().getStopLocation(), hubLocation);
        double transitWeight = computeTransitWeight(hubLocation, target);
        double subWeight = transferWeight + transitWeight;
        minOption = Math.min(subWeight, minOption);
      }

      node.setMinRemainingWeight(minOption);
    }

    w += minOption;

    return w;
  }

  private double getMaxSpeed() {

    if (!_useTransit)
      return _options.speed;

    return _maxTransitSpeed;
  }

  private double getMaxSpeedForCurrentState(State state, Vertex v) {

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
    if (state.isEverBoarded() && !(v instanceof TransitVertex))
      return _options.speed;

    return _maxTransitSpeed;
  }

  private int computeTransitWeight(CoordinatePoint from, CoordinatePoint to) {
    double transitDistance = distance(from.getLat(), from.getLon(),
        to.getLat(), to.getLon());
    return (int) (transitDistance / _maxTransitSpeed);
  }

  private double computeTransferWeight(CoordinatePoint source,
      CoordinatePoint dest) {
    double walkDistance = distance(source.getLat(), source.getLon(),
        dest.getLat(), dest.getLon());
    int walkTime = (int) (walkDistance / _options.speed);
    return ItineraryWeightingLibrary.computeTransferWeight(walkTime, _options);
  }

  private double distance(Vertex a, Vertex b) {
    return distance(a.getY(), a.getX(), b.getY(), b.getX());
  }

  private static final double distance(double lat1, double lon1, double lat2,
      double lon2) {
    return SphericalGeometryLibrary.distanceFaster(lat1, lon2, lat2, lon2);
  }

}
