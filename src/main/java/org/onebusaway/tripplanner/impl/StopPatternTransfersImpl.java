/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.collections.stats.Min;
import edu.washington.cs.rse.collections.tuple.Pair;

import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.tripplanner.NoPathException;
import org.onebusaway.tripplanner.StopTransferWalkPlannerService;
import org.onebusaway.tripplanner.model.TripPlannerConstants;
import org.onebusaway.tripplanner.model.TripPlannerGraph;
import org.onebusaway.tripplanner.model.Walk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StopPatternTransfersImpl {

  private TripPlannerConstants _constants;

  private TripPlannerGraph _graph;

  private List<Transfer> _tCache;

  private List<Stop> _fromStops;

  private List<Stop> _toStops;

  private StopTransferWalkPlannerService _stopTransferWalkPlannerService;

  /*****************************************************************************
   * 
   ****************************************************************************/

  public StopPatternTransfersImpl(TripPlannerConstants constants,
      TripPlannerGraph graph, List<Stop> fromStops, List<Stop> toStops,
      StopTransferWalkPlannerService stopTransferWalkPlannerService) {
    _constants = constants;
    _graph = graph;
    _fromStops = fromStops;
    _toStops = toStops;
    _stopTransferWalkPlannerService = stopTransferWalkPlannerService;
  }

  public Map<Pair<String>, Double> computeTransfers() {

    Map<Pair<String>, Double> transfers = new HashMap<Pair<String>, Double>();

    int n = getKeyCount();
    _tCache = new ArrayList<Transfer>(n);
    for (int i = 0; i < n; i++)
      _tCache.add(null);

    for (int fromStopIndex = 0; fromStopIndex < _fromStops.size(); fromStopIndex++) {

      Stop fromStop = _fromStops.get(fromStopIndex);

      for (int toStopIndex = 0; toStopIndex < _toStops.size(); toStopIndex++) {

        Stop toStop = _toStops.get(toStopIndex);

        Transfer transfer = getFastest(fromStopIndex, false, toStopIndex,
            false, 0);
        Pair<String> pair = transfer.getTransfer();

        // We only care about bus-to-bus transfers
        String transferFrom = pair.getFirst();
        String transferTo = pair.getSecond();

        if (transferFrom.equals(fromStop.getId())
            || transferTo.equals(toStop.getId()))
          continue;

        if (transfer.getDistance() <= _constants.getMaxTransferDistance())
          transfers.put(pair,transfer.getDistance());
      }
    }

    return transfers;
  }

  private int getKeyCount() {
    return _fromStops.size() * 2 * _toStops.size() * 2;
  }

  private int getKey(int fromStop, boolean fromBus, int toStop, boolean toBus) {
    int a = fromStop * 2 + (fromBus ? 1 : 0);
    int b = toStop * 2 + (toBus ? 1 : 0);
    return a * _toStops.size() * 2 + b;
  }

  private Transfer getFastest(int fromStop, boolean fromBus, int toStop,
      boolean toBus, int depth) {

    int key = getKey(fromStop, fromBus, toStop, toBus);

    Transfer transfer = _tCache.get(key);
    if (transfer == null) {
      transfer = getFastestNonCached(fromStop, fromBus, toStop, toBus, depth);
      _tCache.set(key, transfer);
    }
    return transfer;
  }

  private Transfer getFastestNonCached(int fromStopIndex, boolean fromBus,
      int toStopIndex, boolean toBus, int depth) {

    double fromTransferPenalty = fromBus ? 0.0
        : _constants.getMinTransferTime();
    double toTransferPenalty = toBus ? _constants.getMinTransferTime() : 0.0;

    Stop fromStop = _fromStops.get(fromStopIndex);
    Stop toStop = _toStops.get(toStopIndex);

    if (fromStop.equals(toStop))
      return new Transfer(Pair.createPair(fromStop.getId(), toStop.getId()),
          0.0 + toTransferPenalty, 0.0);

    double walkingDistance = getWalkingDistance(fromStop, toStop);
    double walkingTime = walkingDistance / _constants.getWalkingVelocity();

    Min<Transfer> m = new Min<Transfer>();

    Transfer walk = new Transfer(Pair.createPair(fromStop.getId(),
        toStop.getId()), walkingTime + toTransferPenalty, walkingDistance);
    m.add(walk.getTime(), walk);

    int fromNextIndex = fromStopIndex + 1;
    int toPreviousIndex = toStopIndex - 1;

    boolean hasNext = fromNextIndex < _fromStops.size();
    boolean hasPrevious = toPreviousIndex >= 0;

    if (hasNext) {
      Stop fromNextStop = _fromStops.get(fromNextIndex);
      double busTimeFrom = fromTransferPenalty
          + getMinTravelTime(fromStop, fromNextStop);
      Transfer transfer = getFastest(fromNextIndex, true, toStopIndex, toBus,
          depth + 1);
      transfer = transfer.extend(busTimeFrom);
      m.add(transfer.getTime(), transfer);
    }

    if (hasPrevious) {
      Stop toPreviousStop = _toStops.get(toPreviousIndex);
      Transfer transfer = getFastest(fromStopIndex, fromBus, toPreviousIndex,
          true, depth + 1);
      double busTimeTo = getMinTravelTime(toPreviousStop, toStop);
      transfer = transfer.extend(busTimeTo);
      m.add(transfer.getTime(), transfer);
    }

    if (hasNext && hasPrevious) {
      Stop fromNextStop = _fromStops.get(fromNextIndex);
      Stop toPreviousStop = _toStops.get(toPreviousIndex);
      double busTimeFrom = fromTransferPenalty
          + getMinTravelTime(fromStop, fromNextStop);
      Transfer transfer = getFastest(fromNextIndex, true, toPreviousIndex,
          true, depth + 1);
      double busTimeTo = getMinTravelTime(toPreviousStop, toStop);
      transfer = transfer.extend(busTimeFrom + busTimeTo);
      m.add(transfer.getTime(), transfer);
    }

    return m.getMinElement();
  }

  private double getWalkingDistance(Stop fromStop, Stop toStop) {
    try {
      Walk walk = _stopTransferWalkPlannerService.getWalkPlan(fromStop, toStop);
      if (walk == null)
        return Double.POSITIVE_INFINITY;
      return walk.getDistance();
    } catch (NoPathException ex) {
      return Double.POSITIVE_INFINITY;
    }
  }

  private double getMinTravelTime(Stop from, Stop to) {
    return _graph.getMinTransitTime(from.getId(), to.getId()) * 1000;
  }

  private static class Transfer implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Pair<String> _transfer;

    private final double _time;

    private final double _distance;

    public Transfer(Pair<String> transfer, double time, double distance) {
      _transfer = transfer;
      _time = time;
      _distance = distance;
    }

    public Transfer extend(double time) {
      return new Transfer(_transfer, _time + time, _distance);
    }

    public Pair<String> getTransfer() {
      return _transfer;
    }

    public double getTime() {
      return _time;
    }

    public double getDistance() {
      return _distance;
    }

    @Override
    public String toString() {
      return "Transfer(" + _transfer + " " + _time + ")";
    }
  }
}