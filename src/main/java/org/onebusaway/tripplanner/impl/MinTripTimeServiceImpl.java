package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.transit.common.offline.graph.AStarProblem;
import edu.washington.cs.rse.transit.common.offline.graph.AStarResults;
import edu.washington.cs.rse.transit.common.offline.graph.AStarSearch;
import edu.washington.cs.rse.transit.common.offline.graph.AStarSearch.NoPathToGoalException;

import org.onebusaway.common.impl.UtilityLibrary;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.gtdf.model.Trip;
import org.onebusaway.tripplanner.model.StopEntry;
import org.onebusaway.tripplanner.model.TripEntry;
import org.onebusaway.tripplanner.model.TripPlannerConstants;
import org.onebusaway.tripplanner.model.TripPlannerGraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MinTripTimeServiceImpl {

  private TripPlannerConstants _constants;

  private TripPlannerGraph _graph;

  public void setTripPlannerConstants(TripPlannerConstants constants) {
    _constants = constants;
  }

  public void setTripPlannerGraph(TripPlannerGraph graph) {
    _graph = graph;
  }

  public long getMinTripTime(Stop from, Stop to) {

    try {
      AStarResults<Stop> results = AStarSearch.searchWithResults(new Problem(),
          from, to);
      return results.getGScore().get(to).longValue();
    } catch (NoPathToGoalException e) {
      return Long.MAX_VALUE;
    }
  }

  public AStarResults<Stop> getMinTrip(Stop from, Stop to)
      throws NoPathToGoalException {
      return AStarSearch.searchWithResults(new Problem(), from, to);
  }

  private class Problem implements AStarProblem<Stop> {

    public double getDistance(Stop from, Stop to) {

      int tt = _graph.getMinTransitTime(from.getId(), to.getId());

      if (tt < 0) {
        double d = UtilityLibrary.distance(from.getLocation(), to.getLocation());
        return d / _constants.getWalkingVelocity()
            + _constants.getMinTransferTime();
      }

      return tt * 1000;
    }

    public double getEstimatedDistance(Stop from, Stop to) {
      double d = UtilityLibrary.distance(from.getLocation(), to.getLocation());
      return d / _constants.getMaxTransitVelocity();
    }

    public Collection<Stop> getNeighbors(Stop node) {

      Set<Stop> stops = new HashSet<Stop>();

      // Transfers
      StopEntry entry = _graph.getStopEntryByStopId(node.getId());
      for (String id : entry.getTransfers()) {
        StopEntry se = _graph.getStopEntryByStopId(id);
        stops.add(se.getStop());
      }

      Map<String, List<StopTime>> stsByServiceId = entry.getStopTimes();

      for (List<StopTime> sts : stsByServiceId.values()) {
        for (StopTime st : sts) {
          Trip trip = st.getTrip();
          TripEntry tripEntry = _graph.getTripEntryByTripId(trip.getId());
          List<StopTime> tripStopTimes = tripEntry.getStopTimes();
          int index = tripStopTimes.indexOf(st);
          if (index == -1)
            throw new IllegalStateException();
          if (index + 1 < tripStopTimes.size()) {
            StopTime next = tripStopTimes.get(index + 1);
            stops.add(next.getStop());
          }
        }
      }

      return stops;
    }
  }
}
