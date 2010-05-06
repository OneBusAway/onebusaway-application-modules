package org.onebusaway.tripplanner.impl.comparison;

import org.onebusaway.tripplanner.model.AtStopState;
import org.onebusaway.tripplanner.model.BlockTransferState;
import org.onebusaway.tripplanner.model.EndState;
import org.onebusaway.tripplanner.model.StopIdsWithValues;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.TripStats;
import org.onebusaway.tripplanner.model.VehicleArrivalState;
import org.onebusaway.tripplanner.model.WalkFromStopState;
import org.onebusaway.tripplanner.model.WalkToStopState;
import org.onebusaway.tripplanner.services.NoPathException;
import org.onebusaway.tripplanner.services.StopEntry;
import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.StopTimeProxy;
import org.onebusaway.tripplanner.services.TripEntry;
import org.onebusaway.tripplanner.services.TripPlannerGraph;

import edu.washington.cs.rse.collections.stats.Min;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class MinPathToEndStateImpl implements TripStateScoringStrategy {

  private Map<String, Stats> _walkFromStopToEnd = new HashMap<String, Stats>();

  private Map<String, Stats> _distanceFromStart = new HashMap<String, Stats>();

  private TripPlannerGraph _graph;

  private TripStatsScoringStrategy _scoring;

  private PriorityQueue<StopId> _queue = new PriorityQueue<StopId>();

  private Set<String> _closed = new HashSet<String>();

  private long _minTransferTime;

  private double _walkingVelocity;

  public MinPathToEndStateImpl(TripPlannerGraph graph, TripStatsScoringStrategy scoring,
      Map<String, Double> statsFromStopsToEndPoint, double walkingVelocity, long minTransferTime) {

    _graph = graph;
    _scoring = scoring;
    _minTransferTime = minTransferTime;
    _walkingVelocity = walkingVelocity;

    for (Map.Entry<String, Double> entry : statsFromStopsToEndPoint.entrySet()) {
      String stopId = entry.getKey();
      Stats stats = new Stats(walkingVelocity);
      stats.incrementTotalWalkingDistance(entry.getValue());
      double score = _scoring.getTripScore(stats);
      stats.setScore(score);
      _walkFromStopToEnd.put(stopId, stats);
      exploreIncomingRoutesToStop(stopId, stats, true);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @seeorg.onebusaway.tripplanner.impl.comparison.TripStateScoringStrategy#
   * getMinScoreForTripState(org.onebusaway.tripplanner.model.TripState)
   */
  public double getMinScoreForTripState(TripState state) throws NoPathException {
    TripStats stats = getMinStatsForTripState(state);
    return _scoring.getTripScore(stats);
  }

  public TripStats getMinStatsForTripState(TripState state) throws NoPathException {

    if (state instanceof WalkToStopState) {
      WalkToStopState wtss = (WalkToStopState) state;
      return getWalkToStopState(wtss.getStopId());
    } else if (state instanceof WalkFromStopState) {
      WalkFromStopState wfss = (WalkFromStopState) state;
      return getWalkFromStopState(wfss.getStopId());
    } else if (state instanceof VehicleArrivalState) {
      VehicleArrivalState atStopState = (VehicleArrivalState) state;
      return getVehcileArrivalState(atStopState.getStopId());
    } else if (state instanceof AtStopState) {
      AtStopState atStopState = (AtStopState) state;
      return getMinScoreForStop(atStopState.getStopId());
    } else if (state instanceof BlockTransferState) {
      BlockTransferState bt = (BlockTransferState) state;
      String tripId = bt.getNextTripId();
      return getBlockTransferState(bt, tripId);
    } else if (state instanceof EndState) {
      Stats stats = new Stats(_walkingVelocity);
      stats.setScore(_scoring.getTripScore(stats));
      return stats;
    } else {
      throw new IllegalStateException();
    }
  }

  private Stats getVehcileArrivalState(String stopId) throws NoPathException {

    Min<Stats> m = new Min<Stats>();

    try {
      Stats stats = getWalkFromStopState(stopId);
      m.add(stats.getScore(), stats);
    } catch (NoPathException ex) {

    }

    try {
      Stats departFromStopStats = getWaitingAtStopState(stopId);
      Stats stats = new Stats(departFromStopStats);
      stats.incrementTransferWaitingTime(_minTransferTime);
      stats.setScore(_scoring.getTripScore(stats));
      m.add(stats.getScore(), stats);
    } catch (NoPathException e) {

    }

    if (m.isEmpty())
      throw new NoPathException();

    return m.getMinElement();
  }

  private Stats getWaitingAtStopState(String stopId) throws NoPathException {
    return getMinScoreForStop(stopId);
  }

  private Stats getWalkFromStopState(String stopId) throws NoPathException {

    Stats stats = _walkFromStopToEnd.get(stopId);

    if (stats != null)
      return stats;

    StopEntry entry = _graph.getStopEntryByStopId(stopId);

    StopIdsWithValues transfers = entry.getTransfers();

    Min<Stats> m = new Min<Stats>();

    for (int i = 0; i < transfers.size(); i++) {

      try {
        String transferStopId = transfers.getStopId(i);
        int walkingDistance = transfers.getValue(i);

        Stats toStopStats = getWalkToStopState(transferStopId);
        Stats fromStopStats = new Stats(toStopStats);
        fromStopStats.incrementTotalWalkingDistance(walkingDistance);
        fromStopStats.setScore(_scoring.getTripScore(fromStopStats));
        m.add(fromStopStats.getScore(), fromStopStats);
      } catch (NoPathException ex) {

      }
    }

    if (m.isEmpty())
      throw new NoPathException();

    return m.getMinElement();
  }

  private Stats getWalkToStopState(String stopId) throws NoPathException {
    Stats travelFromStopStats = getMinScoreForStop(stopId);
    Stats toStopStats = new Stats(travelFromStopStats);
    toStopStats.incrementTransferWaitingTime(_minTransferTime);
    toStopStats.setScore(_scoring.getTripScore(toStopStats));
    return toStopStats;
  }

  private Stats getBlockTransferState(BlockTransferState bt, String tripId) throws NoPathException {

    if (tripId == null)
      throw new NoPathException();

    TripEntry entry = _graph.getTripEntryByTripId(tripId);
    List<StopTimeProxy> stopTimes = entry.getStopTimes();

    if (stopTimes.isEmpty()) {
      String nextTripId = entry.getNextTripId();
      return getBlockTransferState(bt, nextTripId);
    } else {
      StopTimeProxy first = stopTimes.get(0);
      StopProxy stop = first.getStop();
      return getMinScoreForStop(stop.getStopId());
    }
  }

  private Stats getMinScoreForStop(String stopId) throws NoPathException {

    while (true) {

      if (_closed.contains(stopId))
        return _distanceFromStart.get(stopId);

      if (_queue.isEmpty())
        throw new NoPathException();

      StopId id = _queue.poll();
      String currentStop = id.getStopId();

      if (_closed.contains(currentStop))
        continue;

      _closed.add(currentStop);

      Stats currentStats = _distanceFromStart.get(currentStop);

      StopEntry entry = _graph.getStopEntryByStopId(currentStop);

      exploreIncomingRoutesToStop(currentStop, currentStats, false);

      // We can only transfer between stops if we're currently on a vehicle
      // Stop to Stop to Stop transfers are not allowed

      StopIdsWithValues transfers = entry.getTransfers();

      for (int i = 0; i < transfers.size(); i++) {

        String walkFromStop = transfers.getStopId(i);
        int walkingDistance = transfers.getValue(i);

        Stats potential = new Stats(currentStats);
        potential.incrementTotalWalkingDistance(walkingDistance);
        potential.incrementTransferWaitingTime(_minTransferTime);
        potential.setScore(_scoring.getTripScore(potential));

        exploreIncomingRoutesToStop(walkFromStop, potential, true);
      }
    }
  }

  private void exploreIncomingRoutesToStop(String targetStopId, Stats statsFromTargetStop, boolean startOfTrip) {

    StopEntry entry = _graph.getStopEntryByStopId(targetStopId);
    StopIdsWithValues incomingStops = entry.getPreviousStopsWithMinTimes();

    for (int i = 0; i < incomingStops.size(); i++) {

      String toStop = incomingStops.getStopId(i);
      int transitTime = incomingStops.getValue(i);
      Stats potential = new Stats(statsFromTargetStop);
      potential.incrementVehicleTime(transitTime * 1000);
      if (startOfTrip)
        potential.incrementVehicleCount();
      potential.setScore(_scoring.getTripScore(potential));

      updatePotentialDistance(toStop, potential);
    }
  }

  private void updatePotentialDistance(String stopId, Stats potential) {
    Stats existing = _distanceFromStart.get(stopId);
    if (existing == null || existing.getScore() > potential.getScore()) {
      _distanceFromStart.put(stopId, potential);
      _queue.add(new StopId(stopId, potential.getScore()));
    }
  }

  private static class Stats extends TripStats implements Comparable<Stats> {

    private double score = 0;

    public Stats(double walkingVelocity) {
      super(walkingVelocity);
    }

    public Stats(Stats stats) {
      super(stats);
    }

    public double getScore() {
      return score;
    }

    public void setScore(double score) {
      this.score = score;
    }

    public int compareTo(Stats o) {
      return this.score == o.score ? 0 : (this.score < o.score ? -1 : 1);
    }

    @Override
    public String toString() {
      return "score=" + score + " duration=" + (getTripDuration() / (60.0 * 1000));
    }
  }

  private static class StopId implements Comparable<StopId> {

    private String _stopId;
    private double _score;

    public StopId(String stopId, double score) {
      _stopId = stopId;
      _score = score;
    }

    public String getStopId() {
      return _stopId;
    }

    public int compareTo(StopId o) {
      return _score == o._score ? 0 : (_score < o._score ? -1 : 1);
    }

  }
}
