package org.onebusaway.transit_data_federation.impl.tripplanner.comparison;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.onebusaway.collections.Min;
import org.onebusaway.transit_data_federation.model.tripplanner.AtStopState;
import org.onebusaway.transit_data_federation.model.tripplanner.BlockTransferState;
import org.onebusaway.transit_data_federation.model.tripplanner.EmptyStopEntriesWithValues;
import org.onebusaway.transit_data_federation.model.tripplanner.EndState;
import org.onebusaway.transit_data_federation.model.tripplanner.StopEntriesWithValues;
import org.onebusaway.transit_data_federation.model.tripplanner.TripState;
import org.onebusaway.transit_data_federation.model.tripplanner.TripStats;
import org.onebusaway.transit_data_federation.model.tripplanner.VehicleArrivalState;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkFromStopState;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkToStopState;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.walkplanner.NoPathException;

public class MinPathToEndStateImpl implements TripStateScoringStrategy {

  private Map<StopEntry, TripStats> _walkFromStopToEnd = new HashMap<StopEntry, TripStats>();

  private Map<StopEntry, TripStats> _distanceFromStart = new HashMap<StopEntry, TripStats>();

  private TripStatsScoringStrategy _scoring;

  private PriorityQueue<StopId> _queue = new PriorityQueue<StopId>();

  private Set<StopEntry> _closed = new HashSet<StopEntry>();

  private long _minTransferTime;

  private double _walkingVelocity;

  public MinPathToEndStateImpl(TripStatsScoringStrategy scoring,
      Map<StopEntry, Double> statsFromStopsToEndPoint, double walkingVelocity,
      long minTransferTime) {

    _scoring = scoring;
    _minTransferTime = minTransferTime;
    _walkingVelocity = walkingVelocity;

    for (Map.Entry<StopEntry, Double> entry : statsFromStopsToEndPoint.entrySet()) {
      StopEntry stopEntry = entry.getKey();
      TripStats stats = new TripStats(walkingVelocity);
      stats.incrementTotalWalkingDistance(entry.getValue());
      double score = _scoring.getTripScore(stats);
      stats.setScore(score);
      _walkFromStopToEnd.put(stopEntry, stats);
      exploreIncomingRoutesToStop(stopEntry, stats, true);
    }
  }

  public double getMinScoreForTripState(TripState state) throws NoPathException {
    TripStats stats = getMinStatsForTripState(state);
    return _scoring.getTripScore(stats);
  }

  public TripStats getMinStatsForTripState(TripState state)
      throws NoPathException {

    if (state instanceof WalkToStopState) {
      WalkToStopState wtss = (WalkToStopState) state;
      return getWalkToStopState(wtss.getStop());
    } else if (state instanceof WalkFromStopState) {
      WalkFromStopState wfss = (WalkFromStopState) state;
      return getWalkFromStopState(wfss.getStop());
    } else if (state instanceof VehicleArrivalState) {
      VehicleArrivalState atStopState = (VehicleArrivalState) state;
      return getVehcileArrivalState(atStopState.getStop());
    } else if (state instanceof AtStopState) {
      AtStopState atStopState = (AtStopState) state;
      return getMinScoreForStop(atStopState.getStop());
    } else if (state instanceof BlockTransferState) {
      BlockTransferState bt = (BlockTransferState) state;
      return getBlockTransferState(bt, bt.getNextTrip());
    } else if (state instanceof EndState) {
      TripStats stats = new TripStats(_walkingVelocity);
      stats.setScore(_scoring.getTripScore(stats));
      return stats;
    } else {
      throw new IllegalStateException();
    }
  }

  private TripStats getVehcileArrivalState(StopEntry stopId)
      throws NoPathException {

    Min<TripStats> m = new Min<TripStats>();

    try {
      TripStats stats = getWalkFromStopState(stopId);
      m.add(stats.getScore(), stats);
    } catch (NoPathException ex) {

    }

    try {
      TripStats departFromStopStats = getWaitingAtStopState(stopId);
      TripStats stats = new TripStats(departFromStopStats);
      stats.incrementTransferWaitingTime(_minTransferTime);
      stats.setScore(_scoring.getTripScore(stats));
      m.add(stats.getScore(), stats);
    } catch (NoPathException e) {

    }

    if (m.isEmpty())
      throw new NoPathException();

    return m.getMinElement();
  }

  private TripStats getWaitingAtStopState(StopEntry stopId)
      throws NoPathException {
    return getMinScoreForStop(stopId);
  }

  private TripStats getWalkFromStopState(StopEntry stopEntry)
      throws NoPathException {

    TripStats stats = _walkFromStopToEnd.get(stopEntry);

    if (stats != null)
      return stats;

    // StopEntriesWithValues transfers = stopEntry.getTransfers();
    StopEntriesWithValues transfers = new EmptyStopEntriesWithValues();

    Min<TripStats> m = new Min<TripStats>();

    for (int i = 0; i < transfers.size(); i++) {

      try {
        StopEntry transferStopId = transfers.getStopEntry(i);
        int walkingDistance = transfers.getValue(i);

        TripStats toStopStats = getWalkToStopState(transferStopId);
        TripStats fromStopStats = new TripStats(toStopStats);
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

  private TripStats getWalkToStopState(StopEntry stopId) throws NoPathException {
    TripStats travelFromStopStats = getMinScoreForStop(stopId);
    TripStats toStopStats = new TripStats(travelFromStopStats);
    toStopStats.incrementTransferWaitingTime(_minTransferTime);
    toStopStats.setScore(_scoring.getTripScore(toStopStats));
    return toStopStats;
  }

  private TripStats getBlockTransferState(BlockTransferState bt,
      BlockTripEntry entry) throws NoPathException {

    if (entry == null)
      throw new NoPathException();

    List<BlockStopTimeEntry> stopTimes = entry.getStopTimes();

    BlockStopTimeEntry first = stopTimes.get(0);
    StopEntry stopEntry = first.getStopTime().getStop();
    return getMinScoreForStop(stopEntry);
  }

  private TripStats getMinScoreForStop(StopEntry stopEntry)
      throws NoPathException {

    while (true) {

      if (_closed.contains(stopEntry))
        return _distanceFromStart.get(stopEntry);

      if (_queue.isEmpty())
        throw new NoPathException();

      StopId id = _queue.poll();
      StopEntry currentStop = id.getStop();

      if (_closed.contains(currentStop))
        continue;

      _closed.add(currentStop);

      TripStats currentStats = _distanceFromStart.get(currentStop);

      exploreIncomingRoutesToStop(currentStop, currentStats, false);

      // We can only transfer between stops if we're currently on a vehicle
      // Stop to Stop to Stop transfers are not allowed

      // StopEntriesWithValues transfers = currentStop.getTransfers();
      StopEntriesWithValues transfers = new EmptyStopEntriesWithValues();

      for (int i = 0; i < transfers.size(); i++) {

        StopEntry walkFromStop = transfers.getStopEntry(i);
        int walkingDistance = transfers.getValue(i);

        TripStats potential = new TripStats(currentStats);
        potential.incrementTotalWalkingDistance(walkingDistance);
        potential.incrementTransferWaitingTime(_minTransferTime);
        potential.setScore(_scoring.getTripScore(potential));

        exploreIncomingRoutesToStop(walkFromStop, potential, true);
      }
    }
  }

  private void exploreIncomingRoutesToStop(StopEntry entry,
      TripStats statsFromTargetStop, boolean startOfTrip) {

    StopEntriesWithValues incomingStops = entry.getPreviousStopsWithMinTimes();

    for (int i = 0; i < incomingStops.size(); i++) {

      StopEntry toStop = incomingStops.getStopEntry(i);
      int transitTime = incomingStops.getValue(i);
      TripStats potential = new TripStats(statsFromTargetStop);
      potential.incrementVehicleTime(transitTime * 1000);
      if (startOfTrip)
        potential.incrementVehicleCount();
      potential.setScore(_scoring.getTripScore(potential));

      updatePotentialDistance(toStop, potential);
    }
  }

  private void updatePotentialDistance(StopEntry stopEntry, TripStats potential) {
    TripStats existing = _distanceFromStart.get(stopEntry);
    if (existing == null || existing.getScore() > potential.getScore()) {
      _distanceFromStart.put(stopEntry, potential);
      _queue.add(new StopId(stopEntry, potential.getScore()));
    }
  }

  private static class StopId implements Comparable<StopId> {

    private StopEntry _stop;
    private double _score;

    public StopId(StopEntry stop, double score) {
      _stop = stop;
      _score = score;
    }

    public StopEntry getStop() {
      return _stop;
    }

    public int compareTo(StopId o) {
      return _score == o._score ? 0 : (_score < o._score ? -1 : 1);
    }

  }
}
