package org.onebusaway.tripplanner.impl;

import org.onebusaway.common.impl.UtilityLibrary;
import org.onebusaway.tripplanner.impl.comparison.MinPathToEndStateImpl;
import org.onebusaway.tripplanner.impl.comparison.PerceivedTravelTimeScoringStrategy;
import org.onebusaway.tripplanner.impl.comparison.TripStateScoringStrategy;
import org.onebusaway.tripplanner.impl.comparison.TripStatsScoringStrategy;
import org.onebusaway.tripplanner.model.EndState;
import org.onebusaway.tripplanner.model.StartState;
import org.onebusaway.tripplanner.model.TripContext;
import org.onebusaway.tripplanner.model.TripPlan;
import org.onebusaway.tripplanner.model.TripStateStats;
import org.onebusaway.tripplanner.model.WalkPlan;
import org.onebusaway.tripplanner.services.NoPathException;
import org.onebusaway.tripplanner.services.StopEntry;
import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.TripPlannerGraph;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PointToPointStrategy extends AbstractTripSearchStrategy {

  private TripStatsScoringStrategy _scoringStrategy;

  private TripStateScoringStrategy _estimationStrategy;

  private long _startTime;

  private int _states;

  private int _ends;

  public PointToPointStrategy(TripContext context, Point pointFrom, Point pointTo) {
    super(context);

    _startTime = _constraints.getMinDepartureTime();
    StartState start = new StartState(_startTime, pointFrom);

    addStart(start);

    computeEndpointWalkPlans(pointFrom, pointTo);
  }

  public Collection<TripPlan> getTrips() {
    explore();
    System.out.println("states=" + _states + " ends=" + _ends);
    return _trips.getTrips(new ArrayList<TripPlan>());
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private void computeEndpointWalkPlans(Point pointFrom, Point pointTo) {

    double maxDistance = _constants.getMaxTransferDistance();

    if (_constraints.hasMaxSingleWalkDistance())
      maxDistance = _constraints.getMaxSingleWalkDistance();

    WalkPlan walkFromStart = null;

    if (UtilityLibrary.distance(pointFrom, pointTo) <= maxDistance) {
      try {
        WalkPlan walkPlan = _walkPlanner.getWalkPlan(pointFrom, pointTo);
        if (walkPlan.getDistance() <= maxDistance) {
          walkFromStart = walkPlan;
        }
      } catch (NoPathException e) {
      }
    }

    TripPlannerGraph graph = _context.getGraph();
    Geometry boundary = pointTo.buffer(maxDistance).getBoundary();
    List<String> stopIds = graph.getStopsByLocation(boundary);

    Map<String, WalkPlan> walksFromStops = new HashMap<String, WalkPlan>();
    Map<String, Double> initialWalkDistances = new HashMap<String, Double>();

    for (String stopId : stopIds) {

      try {
        StopEntry entry = graph.getStopEntryByStopId(stopId);
        StopProxy stop = entry.getProxy();
        WalkPlan plan = _walkPlanner.getWalkPlan(stop.getStopLocation(), pointTo);
        walksFromStops.put(stop.getStopId(), plan);
        double walkDistance = plan.getDistance();
        initialWalkDistances.put(stop.getStopId(), walkDistance);
      } catch (NoPathException ex) {

      }
    }

    if (walkFromStart == null && walksFromStops.isEmpty())
      throw new IllegalStateException("no paths to end");

    _transitions.setEndPointWalkPlans(pointTo, walkFromStart, walksFromStops);
    _scoringStrategy = new PerceivedTravelTimeScoringStrategy(_constants);
    _estimationStrategy = new MinPathToEndStateImpl(_graph, _scoringStrategy, initialWalkDistances,
        _constants.getWalkingVelocity(), _constants.getMinTransferTime());
  }

  protected void handleState(TripStateStats state) {
    _states++;
    if (state.getState() instanceof EndState) {
      _ends++;
      TripPlan trip = getAsTrip(state);
      _trips.addTrip(trip);
    }
  }

  @Override
  protected double scoreForComparison(TripStateStats toStats) {

    double fromStart = (toStats.getTripStartTime() - _startTime) * 0.5;
    if (fromStart < 0)
      throw new IllegalStateException();
    double currentScore = _scoringStrategy.getTripScore(toStats);
    return fromStart + currentScore;
  }

  @Override
  protected double scoreForSearch(TripStateStats toStats) throws NoPathException {

    double currentScore = scoreForComparison(toStats);
    double remainingEstimate = _estimationStrategy.getMinScoreForTripState(toStats.getState());
    double estimatedScore = currentScore + remainingEstimate;
    return estimatedScore;
  }

  protected boolean hasSearchReachedTermination(TripStateStats state) {

    if (super.hasSearchReachedTermination(state))
      return true;

    return false;
  }

  /**
   * Return true if the specifed TripStats are valid.
   * 
   * @param stats
   * @return
   */
  protected boolean isTripStatsValid(TripStateStats tripStats) {

    if (!super.isTripStatsValid(tripStats))
      return false;

    return true;
  }
}
