package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.transit_data_federation.impl.tripplanner.aggregation.NBestTripStrategy;
import org.onebusaway.transit_data_federation.impl.tripplanner.comparison.BlockTripComparisonStrategy;
import org.onebusaway.transit_data_federation.impl.tripplanner.comparison.TripComparisonStrategy;
import org.onebusaway.transit_data_federation.model.tripplanner.EndState;
import org.onebusaway.transit_data_federation.model.tripplanner.StartState;
import org.onebusaway.transit_data_federation.model.tripplanner.TripContext;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlan;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstants;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstraints;
import org.onebusaway.transit_data_federation.model.tripplanner.TripState;
import org.onebusaway.transit_data_federation.model.tripplanner.TripStateStats;
import org.onebusaway.transit_data_federation.model.tripplanner.VehicleContinuationState;
import org.onebusaway.transit_data_federation.model.tripplanner.VehicleDepartureState;
import org.onebusaway.transit_data_federation.model.tripplanner.WaitingAtStopState;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkToStopState;
import org.onebusaway.transit_data_federation.services.tripplanner.ETripComparison;
import org.onebusaway.transit_data_federation.services.tripplanner.TripAggregationStrategy;
import org.onebusaway.transit_data_federation.services.walkplanner.NoPathException;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

public abstract class AbstractTripSearchStrategy {

  private final Logger _log = LoggerFactory.getLogger(AbstractTripSearchStrategy.class);
  
  /*****************************************************************************
   * Private Members
   ****************************************************************************/

  private Map<TripState, List<TripStateStats>> _tripsByState = new FactoryMap<TripState, List<TripStateStats>>(
      new ArrayList<TripStateStats>());

  private PriorityQueue<TripStateStats> _queue = new PriorityQueue<TripStateStats>(100,new TripStateStatsComparator());

  private TripPlanStatsMethods _statsMethods;

  protected CombinedStateHandler _transitions;

  protected TripAggregationStrategy _trips;

  private TripComparisonStrategy _comparisonStrategy;

  /**
   * If {@link TripPlannerConstraints#hasMaxComputationTime()} is specified, we
   * track the computation start time and terminate the search if we exceed our
   * allowance of computation time
   */
  private long _computationStartTime;

  private boolean _hasExplored = false;

  private int _stateIndex = 0;

  /*****************************************************************************
   * Protected Members
   ****************************************************************************/

  protected TripContext _context;

  protected TripPlannerConstraints _constraints;

  protected TripPlannerConstants _constants;

  protected WalkPlannerService _walkPlanner;

  private int _prune;

  private int _both;

  private int _notComparable;

  private int _transitionCount;

  private DoubleArrayList _counts = new DoubleArrayList();

  public AbstractTripSearchStrategy(TripContext context) {

    _context = context;
    _constraints = context.getConstraints();
    _constants = context.getConstants();
    _walkPlanner = context.getWalkPlannerService();

    if (!_constraints.hasMinDepartureTime())
      throw new IllegalArgumentException("must specify minStartTime constraint");

    _statsMethods = new TripPlanStatsMethods(context);
    _transitions = new CombinedStateHandler(context);

    _trips = createTripAggregationStrategy();

    _comparisonStrategy = createTripComparisonStrategy();
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  protected void addOrigin(TripState start) {

    TripStateStats stats = new TripStateStats(_constants.getWalkingVelocity(), start, start.getCurrentTime(),
        _stateIndex++);
    _tripsByState.get(start).add(stats);
    stats.setEsimatedScore(0);
    _queue.add(stats);
  }

  protected TripComparisonStrategy createTripComparisonStrategy() {
    return new BlockTripComparisonStrategy(_constraints);
  }

  protected NBestTripStrategy createTripAggregationStrategy() {
    return new NBestTripStrategy(_constants, _constraints);
  }

  protected abstract void handleState(TripStateStats state);

  protected abstract double scoreForComparison(TripStateStats toStats);

  protected abstract double scoreForSearch(TripStateStats toStats) throws NoPathException;

  protected boolean hasSearchReachedTermination(TripStateStats state) {

    // Stop if we have reached our maximum allowed search computation time
    if (_constraints.hasMaxComputationTime()) {
      long t = System.currentTimeMillis();
      if (t - _computationStartTime > _constraints.getMaxComputationTime()) {
        return true;
      }
    }

    if (_constraints.hasMaxTrips()) {
      if (_trips.getSize() >= _constraints.getMaxTrips())
        return true;
    }

    return false;
  }

  /**
   * Return true if the specifed TripStats are valid.
   * 
   * @param stats
   * @return
   */
  protected boolean isTripStatsValid(TripStateStats tripStats) {

    if (_constraints.hasMaxTransferCount() && tripStats.getVehicleCount() - 1 > _constraints.getMaxTransferCount())
      return false;

    if (_constraints.hasMaxSingleWalkDistance()
        && tripStats.getMaxSingleWalkDistance() > _constraints.getMaxSingleWalkDistance())
      return false;

    long duration = tripStats.getTripDuration();

    if (_constraints.hasMinDepartureTime() && tripStats.getTripStartTime() < _constraints.getMinDepartureTime())
      return false;

    if (_constraints.hasMaxDepartureTime() && tripStats.getTripStartTime() > _constraints.getMaxDepartureTime())
      return false;

    if (_constraints.hasMaxTripDuration() && duration > _constraints.getMaxTripDuration())
      return false;

    return true;
  }

  protected TripPlan getAsTrip(TripStateStats stats) {
    return getAsTrip(stats, true);
  }

  protected TripPlan getAsTrip(TripStateStats stats, boolean pruneRepeated) {
    TripPlan plan = new TripPlan(stats, _context.getWalkPlans());
    while (stats != null) {
      plan.pushState(stats.getState());
      if (pruneRepeated)
        pruneRepeatedStates(plan);
      shiftStart(plan);
      stats = stats.getParent();
    }
    return plan;
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  protected void explore() {

    if (_hasExplored)
      return;

    int stateCount = 0;
    int excluded = 0;

    // If we have a computation time constraint, note the start time of
    // computation so we can determine how long we've been computing
    if (_constraints.hasMaxComputationTime())
      _computationStartTime = System.currentTimeMillis();

    while (true) {

      if (_queue.isEmpty()) {
        break;
      }

      stateCount++;

      TripStateStats state = _queue.poll();

      if (state.isExcluded()) {
        excluded++;
        continue;
      }

      if (hasSearchReachedTermination(state)) {
        break;
      }

      handleState(state);

      Set<TripState> transitions = new HashSet<TripState>();
      _transitions.getForwardTransitions(state.getState(), transitions);

      for (TripState next : transitions) {
        handleTransition(state, next);
      }
    }

    _hasExplored = true;

    System.out.println("transitions=" + _transitionCount + " prune=" + _prune + " both=" + _both + " not="
        + _notComparable + " queue=" + Descriptive.mean(_counts));
    System.out.println("stateCount=" + stateCount + " excluded=" + excluded);
  }

  private void handleTransition(TripStateStats fromStats, TripState to) {

    _transitionCount++;

    if (fromStats.getState().equals(to)) {
      _log.warn("invalid state transition: " + fromStats.getState() + " " + to);
      return;
    }

    TripStateStats toStats = _statsMethods.extendTripStateStats(fromStats, to, _context.getWalkPlans(), _stateIndex++);
    toStats.setParent(fromStats);

    checkAndRemoveExcessInitialWaitTime(fromStats, toStats, to);

    // Score it
    double score = scoreForComparison(toStats);
    toStats.setScore(score);

    if (!isTripStatsValid(toStats)) {
      toStats.setExcluded(true);
      return;
    }

    TripState key = to;
    if (key instanceof EndState)
      key = new EndState(0, ((EndState) key).getLocation());

    List<TripStateStats> statesWithStats = _tripsByState.get(key);

    statesWithStats.add(toStats);
    fromStats.addChild(toStats);

    Set<TripStateStats> toExclude = new HashSet<TripStateStats>();

    checkForExclusion(toStats, statesWithStats, toExclude);
    excludeStates(toExclude);
    addToQueue(toStats);
  }

  private void addToQueue(TripStateStats toStats) {
    if (!toStats.isExcluded()) {
      try {
        estimate(toStats);
        _queue.add(toStats);
        _counts.add(_queue.size());
      } catch (NoPathException ex) {

      }
    }
  }

  private void estimate(TripStateStats toStats) throws NoPathException {
    double estimatedScore = scoreForSearch(toStats);
    toStats.setEsimatedScore(estimatedScore);
  }

  private void excludeStates(Set<TripStateStats> toExclude) {
    for (TripStateStats stateWithStats : toExclude) {
      if (stateWithStats.isExcluded())
        continue;
      // excludeParents(stateWithStats);
      excludeChildren(stateWithStats);
    }
  }

  private void checkForExclusion(TripStateStats toStats, List<TripStateStats> statesWithStats,
      Set<TripStateStats> toExclude) {

    for (TripStateStats stateWithStats : statesWithStats) {

      if (stateWithStats.equals(toStats))
        continue;

      ETripComparison comparison = _comparisonStrategy.compare(stateWithStats, toStats);
      switch (comparison) {
        case KEEP_A:
          toExclude.add(toStats);
          _prune++;
          break;
        case KEEP_B:
          toExclude.add(stateWithStats);
          _prune++;
          break;
        case KEEP_BOTH:
          _both++;
          break;
        case NOT_COMPARABLE:
          _notComparable++;
          break;
      }
    }
  }

  private void checkAndRemoveExcessInitialWaitTime(TripStateStats fromStats, TripStateStats toStats, TripState toState) {
    // Is this our initial departure?
    // Are we still waiting at the initial stop?
    TripState fromState = fromStats.getState();
    if (fromState instanceof WaitingAtStopState && fromStats.getVehicleCount() == 0) {
      long slack = toState.getCurrentTime() - fromState.getCurrentTime();
      toStats.incrementInitialWaitingTime(-slack);
      toStats.incrementTripStartTime(slack);
      toStats.incrementInitialSlackTime(slack);
    }
  }

  private void excludeChildren(TripStateStats stats) {
    stats.setExcluded(true);
    for (TripStateStats child : stats.getChildren())
      excludeChildren(child);
  }

  /**
   * 
   * @param plan
   */
  private void pruneRepeatedStates(TripPlan plan) {

    List<TripState> states = plan.getStates();

    if (states.size() < 2)
      return;
    if (states.get(0) instanceof WaitingAtStopState && states.get(1) instanceof WaitingAtStopState)
      states.remove(1);
    else if (states.get(1) instanceof VehicleContinuationState)
      states.remove(1);
  }

  private void shiftStart(TripPlan plan) {

    List<TripState> states = plan.getStates();

    if (states.size() < 4)
      return;

    if (!(states.get(0) instanceof StartState && states.get(1) instanceof WalkToStopState
        && states.get(2) instanceof WaitingAtStopState && states.get(3) instanceof VehicleDepartureState))
      return;

    StartState start = (StartState) states.get(0);
    WalkToStopState walk = (WalkToStopState) states.get(1);
    WaitingAtStopState waiting = (WaitingAtStopState) states.get(2);
    VehicleDepartureState departure = (VehicleDepartureState) states.get(3);

    long slack = departure.getCurrentTime() - waiting.getCurrentTime();

    // If there is no slack, we can return right now
    if (slack <= 0)
      return;

    // Shift all the states to adjust the slack
    states.set(0, start.shift(slack));
    states.set(1, walk.shift(slack));
    states.set(2, waiting.shift(slack));

    plan.incrementInitialSlackTime(slack);
    plan.incrementInitialWaitingTime(-slack);
  }

  private static class TripStateStatsComparator implements Comparator<TripStateStats> {

    public int compare(TripStateStats o1, TripStateStats o2) {
      double s1 = o1.getEstimatedScore();
      double s2 = o2.getEstimatedScore();
      return s1 == s2 ? 0 : (s1 < s2 ? -1 : 1);
    }
  }
}
