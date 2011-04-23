package org.onebusaway.transit_data_federation.impl.otp;

import java.util.Collection;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferTree;
import org.onebusaway.transit_data_federation.impl.otp.graph.TransitVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.tp.HasPathStateVertex;
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

    if (traverseOptions.isArriveBy())
      throw new IllegalStateException();

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

    if (v instanceof HasPathStateVertex) {
      HasPathStateVertex tpV = (HasPathStateVertex) v;

      TPState pathState = tpV.getPathState();
      boolean isDeparture = tpV.isDeparture();

      TransferTree tree = pathState.getTree();

      return getWeightForTree(null, tree, isDeparture, target);
    }

    double distanceEstimate = distance(v, target);

    double maxSpeed = getMaxSpeedForCurrentState(traverseResult, v);

    return distanceEstimate / maxSpeed;
  }

  private double getWeightForTree(TransferTree parentTree, TransferTree tree,
      boolean isDeparture, Vertex target) {

    double w = 0;

    if (parentTree != null && isDeparture) {

      StopEntry fromStop = parentTree.getToStop();
      StopEntry toStop = tree.getFromStop();
      double walkDistance = SphericalGeometryLibrary.distance(
          fromStop.getStopLocation(), toStop.getStopLocation());
      int walkTime = (int) (walkDistance / _options.speed);
      double transferWeight = ItineraryWeightingLibrary.computeTransferWeight(
          walkTime, _options);
      w += transferWeight;
    }

    if (isDeparture) {

      StopEntry fromStop = tree.getFromStop();
      StopEntry toStop = tree.getToStop();

      double transitDistance = SphericalGeometryLibrary.distance(
          fromStop.getStopLat(), fromStop.getStopLon(), toStop.getStopLat(),
          toStop.getStopLon());
      int transitTime = (int) (transitDistance / _maxTransitSpeed);
      w += transitTime;
    }

    Collection<TransferTree> transfers = tree.getTransfers();
    if (transfers.isEmpty()) {

      StopEntry toStop = tree.getToStop();
      CoordinatePoint dest = new CoordinatePoint(target.getY(), target.getX());

      double walkDistance = SphericalGeometryLibrary.distance(
          toStop.getStopLocation(), dest);
      int walkTime = (int) (walkDistance / _options.speed);
      double transferWeight = ItineraryWeightingLibrary.computeTransferWeight(
          walkTime, _options);
      w += transferWeight;
    } else {
      double min = Double.POSITIVE_INFINITY;
      for (TransferTree subTree : transfers) {
        double subWeight = getWeightForTree(tree, subTree, true, target);
        min = Math.min(subWeight, min);
      }

      w += min;
    }

    return w;
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
