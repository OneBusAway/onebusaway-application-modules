package org.onebusaway.tripplanner.impl.comparison;

import org.onebusaway.tripplanner.model.AtStopState;
import org.onebusaway.tripplanner.model.BlockTransferState;
import org.onebusaway.tripplanner.model.EndState;
import org.onebusaway.tripplanner.model.TripPlannerConstants;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.TripStateStats;
import org.onebusaway.tripplanner.model.VehicleArrivalState;
import org.onebusaway.tripplanner.model.WalkFromStopState;
import org.onebusaway.tripplanner.model.WalkToStopState;
import org.onebusaway.tripplanner.services.MinTransitTimeEstimationStrategy;
import org.onebusaway.tripplanner.services.NoPathException;
import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.StopTimeProxy;
import org.onebusaway.tripplanner.services.TripEntry;
import org.onebusaway.tripplanner.services.TripPlannerGraph;

import java.util.List;

public class EstimatedTripStateStatsScoringStrategy implements TripStateStatsScoringStrategy {

  private MinTransitTimeEstimationStrategy _minTransitTimeEstimationStrategy;
  private TripPlannerConstants _constants;
  private TripPlannerGraph _graph;

  public EstimatedTripStateStatsScoringStrategy(TripPlannerConstants constants, TripPlannerGraph graph,
      MinTransitTimeEstimationStrategy minTransitTimeEstimationStrategy) {

    _constants = constants;
    _graph = graph;
    _minTransitTimeEstimationStrategy = minTransitTimeEstimationStrategy;
  }

  public double getTripStateStatsScore(TripStateStats stats) {

    TripState state = stats.getState();

    if (state instanceof AtStopState) {
      AtStopState atStop = (AtStopState) state;
      try {
        int time = _minTransitTimeEstimationStrategy.getMinTransitTime(atStop.getStopId());
        if (time < 0)
          throw new IllegalStateException();
        if (state instanceof WalkFromStopState || state instanceof WalkToStopState
            || state instanceof VehicleArrivalState)
          time += _constants.getMinTransferTime() / 1000;
        return time;
      } catch (NoPathException ex) {
        return Integer.MAX_VALUE;
      }
    } else if (state instanceof BlockTransferState) {
      BlockTransferState bt = (BlockTransferState) state;
      String tripId = bt.getNextTripId();
      return getEstimatedMinimumTravelTimeToDestination(bt, tripId);
    } else if (state instanceof EndState) {
      return 0;
    } else {
      throw new IllegalStateException();
    }
  }

  private int getEstimatedMinimumTravelTimeToDestination(BlockTransferState bt, String tripId) {

    if (tripId == null)
      return Integer.MAX_VALUE;

    TripEntry entry = _graph.getTripEntryByTripId(tripId);
    List<StopTimeProxy> stopTimes = entry.getStopTimes();

    if (stopTimes.isEmpty()) {
      String nextTripId = entry.getNextTripId();
      return getEstimatedMinimumTravelTimeToDestination(bt, nextTripId);
    } else {
      StopTimeProxy first = stopTimes.get(0);
      StopProxy stop = first.getStop();
      try {
        return _minTransitTimeEstimationStrategy.getMinTransitTime(stop.getStopId());
      } catch (NoPathException e) {
        return Integer.MAX_VALUE;
      }
    }
  }

}
