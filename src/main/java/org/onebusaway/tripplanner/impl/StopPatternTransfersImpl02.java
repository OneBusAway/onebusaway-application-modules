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
import org.onebusaway.tripplanner.model.TripPlannerConstants;
import org.onebusaway.tripplanner.model.TripPlannerGraph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StopPatternTransfersImpl02 {

  private TripPlannerConstants _constants;

  private TripPlannerGraph _graph;

  private List<Stop> _fromStops;

  private List<Stop> _toStops;

  private List<Transfer> _tCache;

  /*****************************************************************************
   * 
   ****************************************************************************/

  public StopPatternTransfersImpl02(TripPlannerConstants constants,
      TripPlannerGraph graph, List<Stop> fromStops, List<Stop> toStops) {
    _constants = constants;
    _graph = graph;
    _fromStops = fromStops;
    _toStops = toStops;
  }

  public Set<Pair<String>> computeTransfers() {

    Set<Pair<String>> transfers = new HashSet<Pair<String>>();

    int n = getKeyCount();
    _tCache = new ArrayList<Transfer>(n);
    for (int i = 0; i < n; i++)
      _tCache.add(null);

    for (int fromStopIndex = 0; fromStopIndex < _fromStops.size(); fromStopIndex++) {

      Stop fromStop = _fromStops.get(fromStopIndex);

      for (int toStopIndex = 0; toStopIndex < _toStops.size(); toStopIndex++) {

        Stop toStop = _toStops.get(toStopIndex);

        Transfer transfer = getFastest(fromStopIndex, toStopIndex, 0);
        Pair<String> pair = transfer.getTransfer();

        // We only care about bus-to-bus transfers
        String transferFrom = pair.getFirst();
        String transferTo = pair.getSecond();

        if (transferFrom.equals(fromStop.getId())
            || transferTo.equals(toStop.getId()))
          continue;

        if (transfer.getDistance() <= _constants.getMaxTransferDistance())
          transfers.add(pair);
      }
    }

    return transfers;
  }

  private int getKeyCount() {
    return _fromStops.size() * _toStops.size();
  }

  private int getKey(int fromStopIndex, int toStopIndex) {
    return fromStopIndex * _toStops.size() + toStopIndex;
  }

  private Transfer getFastest(int fromStopIndex, int toStopIndex, int depth) {

    int key = getKey(fromStopIndex, toStopIndex);

    Transfer transfer = _tCache.get(key);
    if (transfer == null) {
      transfer = getFastestNonCached(fromStopIndex, toStopIndex, depth);
      _tCache.set(key, transfer);
    }
    return transfer;
  }

  private Transfer getFastestNonCached(int fromStopIndex, int toStopIndex,
      int depth) {

    double fromTransferPenalty = 0.0;
    double toTransferPenalty = _constants.getMinTransferTime();

    Stop fromStop = _fromStops.get(fromStopIndex);
    Stop toStop = _toStops.get(toStopIndex);

    if (fromStop.equals(toStop))
      return new Transfer(Pair.createPair(fromStop.getId(), toStop.getId()),
          0.0 + toTransferPenalty, 0.0);

    double walkingDistance = getDistance(fromStop, toStop);
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
      Transfer transfer = getFastest(fromNextIndex, toStopIndex, depth + 1);
      transfer = transfer.extend(busTimeFrom);
      m.add(transfer.getTime(), transfer);
    }

    if (hasPrevious) {
      Stop toPreviousStop = _toStops.get(toPreviousIndex);
      Transfer transfer = getFastest(fromStopIndex, toPreviousIndex, depth + 1);
      double busTimeTo = getMinTravelTime(toPreviousStop, toStop);
      transfer = transfer.extend(busTimeTo);
      m.add(transfer.getTime(), transfer);
    }

    if (hasNext && hasPrevious) {
      Stop fromNextStop = _fromStops.get(fromNextIndex);
      Stop toPreviousStop = _toStops.get(toPreviousIndex);
      double busTimeFrom = fromTransferPenalty
          + getMinTravelTime(fromStop, fromNextStop);
      Transfer transfer = getFastest(fromNextIndex, toPreviousIndex, depth + 1);
      double busTimeTo = getMinTravelTime(toPreviousStop, toStop);
      transfer = transfer.extend(busTimeFrom + busTimeTo);
      m.add(transfer.getTime(), transfer);
    }

    return m.getMinElement();
  }

  private double getMinTravelTime(Stop from, Stop to) {
    return _graph.getMinTransitTime(from.getId(), to.getId()) * 1000;
  }

  private static final double getDistance(Stop a, Stop b) {
    double ax = a.getLocation().getX();
    double ay = a.getLocation().getY();
    double bx = b.getLocation().getX();
    double by = b.getLocation().getY();
    double dx = ax - bx;
    double dy = ay - by;
    return Math.sqrt(dx * dx + dy * dy);
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