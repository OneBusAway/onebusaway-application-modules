/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.impl.otp;

import java.util.List;

import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractBlockVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.TransitVertex;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.opentripplanner.routing.algorithm.strategies.RemainingWeightHeuristic;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;

public class RemainingWeightHeuristicImpl implements RemainingWeightHeuristic {

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

    OBAState obaState = (OBAState) state;

    if (obaState.getMaxBlockSequence() >= 0 && v instanceof AbstractBlockVertex) {

      AbstractBlockVertex abv = (AbstractBlockVertex) v;
      ArrivalAndDepartureInstance instance = abv.getInstance();

      BlockInstance blockInstance = instance.getBlockInstance();
      BlockConfigurationEntry blockConfig = blockInstance.getBlock();
      List<BlockStopTimeEntry> stopTimes = blockConfig.getStopTimes();
      int maxBlockSequence = Math.min(obaState.getMaxBlockSequence(),
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
     * if (v instanceof HasStopTransitVertex) { HasStopTransitVertex hasStop =
     * (HasStopTransitVertex) v; StopEntry stop = hasStop.getStop(); if
     * (_heuristic == null) { GraphContext context = hasStop.getContext();
     * _heuristic = new MinTravelTimeUsingTransitHeuristic(target, _options,
     * context, _maxTransitSpeed); }
     * 
     * int travelTime = _heuristic.getMinTravelTimeFromStopToTarget(stop);
     * 
     * if (travelTime >= 0) return travelTime; }
     */

    double distanceEstimate = distance(v, target);

    double maxSpeed = getMaxSpeedForCurrentState(state, v);

    return distanceEstimate / maxSpeed;
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

  private double distance(Vertex a, Vertex b) {
    return SphericalGeometryLibrary.distance(a.getY(), a.getX(), b.getY(),
        b.getX());
  }

}
