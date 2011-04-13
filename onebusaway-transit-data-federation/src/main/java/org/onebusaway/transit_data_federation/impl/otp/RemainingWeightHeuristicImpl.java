package org.onebusaway.transit_data_federation.impl.otp;

import java.util.List;

import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractBlockVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.HasStopTransitVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.TransitVertex;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
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

  private MinTravelTimeUsingTransitHeuristic _heuristic = null;

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

    State state = traverseResult.state;
    OBAStateData data = (OBAStateData) state.getData();

    if (data.getMaxBlockSequence() >= 0 && v instanceof AbstractBlockVertex) {

      AbstractBlockVertex abv = (AbstractBlockVertex) v;
      ArrivalAndDepartureInstance instance = abv.getInstance();

      BlockInstance blockInstance = instance.getBlockInstance();
      BlockConfigurationEntry blockConfig = blockInstance.getBlock();
      List<BlockStopTimeEntry> stopTimes = blockConfig.getStopTimes();
      int maxBlockSequence = Math.min(data.getMaxBlockSequence(),
          stopTimes.size());

      BlockStopTimeEntry blockStopTime = instance.getBlockStopTime();
      int sequence = blockStopTime.getBlockSequence();
      StopTimeEntry origStopTime = blockStopTime.getStopTime();

      double minTime = Double.POSITIVE_INFINITY;

      while (sequence < maxBlockSequence) {

        blockStopTime = stopTimes.get(sequence);
        StopTimeEntry stopTime = blockStopTime.getStopTime();
        StopEntry stop = stopTime.getStop();

        double d = SphericalGeometryLibrary.distance(stop.getStopLat(),
            stop.getStopLon(), target.getY(), target.getX());

        double transitTime = Math.max(0, stopTime.getArrivalTime()
            - origStopTime.getDepartureTime());
        double walkingTime = d / _options.speed;
        minTime = Math.min(minTime, transitTime + walkingTime);

        sequence++;
      }

      if (!Double.isInfinite(minTime))
        return minTime;
    }

    /*
    if (v instanceof HasStopTransitVertex) {
      HasStopTransitVertex hasStop = (HasStopTransitVertex) v;
      StopEntry stop = hasStop.getStop();
      if (_heuristic == null) {
        GraphContext context = hasStop.getContext();
        _heuristic = new MinTravelTimeUsingTransitHeuristic(target, _options,
            context, _maxTransitSpeed);
      }
      
      int travelTime = _heuristic.getMinTravelTimeFromStopToTarget(stop);
      
      if (travelTime >= 0)
        return travelTime;
    }
    */
    
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
