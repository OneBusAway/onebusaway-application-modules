package org.onebusaway.transit_data_federation.impl.tripplanner;

import org.onebusaway.transit_data_federation.impl.tripplanner.comparison.MinPathToEndStateImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.comparison.PerceivedTravelTimeScoringStrategy;
import org.onebusaway.transit_data_federation.impl.tripplanner.comparison.TripStateScoringStrategy;
import org.onebusaway.transit_data_federation.impl.tripplanner.comparison.TripStatsScoringStrategy;
import org.onebusaway.transit_data_federation.model.tripplanner.EndState;
import org.onebusaway.transit_data_federation.model.tripplanner.StartState;
import org.onebusaway.transit_data_federation.model.tripplanner.TripContext;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlan;
import org.onebusaway.transit_data_federation.model.tripplanner.TripStateStats;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlan;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;
import org.onebusaway.transit_data_federation.services.walkplanner.NoPathException;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;
import edu.washington.cs.rse.geospatial.latlon.CoordinateRectangle;

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

  public PointToPointStrategy(TripContext context, CoordinatePoint pointFrom, CoordinatePoint pointTo) {
    super(context);

    _startTime = _constraints.getMinDepartureTime();
    StartState start = new StartState(_startTime, pointFrom);

    addOrigin(start);

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

  private void computeEndpointWalkPlans(CoordinatePoint pointFrom, CoordinatePoint pointTo) {

    double maxDistance = _constants.getMaxTransferDistance();

    if (_constraints.hasMaxSingleWalkDistance())
      maxDistance = _constraints.getMaxSingleWalkDistance();

    WalkPlan walkFromStart = null;

    if (DistanceLibrary.distance(pointFrom, pointTo) <= maxDistance) {
      try {
        WalkPlan walkPlan = _walkPlanner.getWalkPlan(pointFrom, pointTo);
        if (walkPlan.getDistance() <= maxDistance) {
          walkFromStart = walkPlan;
        }
      } catch (NoPathException e) {
      }
    }

    TripPlannerGraph graph = _context.getGraph();
    CoordinateRectangle bounds = DistanceLibrary.bounds(pointTo, maxDistance);
    List<StopEntry> stopEntries = graph.getStopsByLocation(bounds);

    Map<StopEntry, WalkPlan> walksFromStops = new HashMap<StopEntry, WalkPlan>();
    Map<StopEntry, Double> initialWalkDistances = new HashMap<StopEntry, Double>();

    for (StopEntry stop : stopEntries) {

      try {
        WalkPlan plan = _walkPlanner.getWalkPlan(stop.getStopLocation(), pointTo);
        walksFromStops.put(stop, plan);
        double walkDistance = plan.getDistance();
        initialWalkDistances.put(stop, walkDistance);
      } catch (NoPathException ex) {

      }
    }

    if (walkFromStart == null && walksFromStops.isEmpty())
      throw new IllegalStateException("no paths to end");

    _transitions.setEndPointWalkPlans(pointTo, walkFromStart, walksFromStops);
    _scoringStrategy = new PerceivedTravelTimeScoringStrategy(_constants);
    _estimationStrategy = new MinPathToEndStateImpl(_scoringStrategy, initialWalkDistances, _constants.getWalkingVelocity(),
        _constants.getMinTransferTime());
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
