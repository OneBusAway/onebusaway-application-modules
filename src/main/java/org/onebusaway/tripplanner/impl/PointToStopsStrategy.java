package org.onebusaway.tripplanner.impl;

import org.onebusaway.tripplanner.impl.comparison.DurationTripComparisonStrategy;
import org.onebusaway.tripplanner.impl.comparison.TripComparisonStrategy;
import org.onebusaway.tripplanner.model.AtStopState;
import org.onebusaway.tripplanner.model.StartState;
import org.onebusaway.tripplanner.model.TripContext;
import org.onebusaway.tripplanner.model.TripPlannerConstraints;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.TripStateStats;
import org.onebusaway.tripplanner.services.NoPathException;
import org.onebusaway.tripplanner.services.StopProxy;

import com.vividsolutions.jts.geom.Point;

import java.util.HashMap;
import java.util.Map;

public class PointToStopsStrategy extends AbstractTripSearchStrategy {

  private Map<StopProxy, Long> _minTravelTimeToStop = new HashMap<StopProxy, Long>();

  public PointToStopsStrategy(TripContext context, Point startPoint) {
    super(context);

    TripPlannerConstraints constraints = context.getConstraints();
    StartState start = new StartState(constraints.getMinDepartureTime(), startPoint);
    addStart(start);
  }

  public Map<StopProxy, Long> getMinTravelTimeToStop() {
    explore();
    return _minTravelTimeToStop;
  }

  /*****************************************************************************
   * Protected Methods
   ****************************************************************************/

  @Override
  protected void handleState(TripStateStats state) {

    TripState s = state.getState();

    if (s instanceof AtStopState) {

      AtStopState atStop = (AtStopState) s;
      StopProxy stop = atStop.getStop();
      Long existingTime = _minTravelTimeToStop.get(stop);
      long duration = state.getTripDuration();

      if (existingTime == null || existingTime > duration) {
        _minTravelTimeToStop.put(stop, duration);
      }
    }
  }

  /**
   * We wish to keep only the shortest trip to any given stop. Thus, we compare
   * and prune trips based solely on trip duration.
   */
  @Override
  protected TripComparisonStrategy createTripComparisonStrategy() {
    return new DurationTripComparisonStrategy();
  }

  /**
   * ...
   */
  @Override
  protected double scoreForComparison(TripStateStats toStats) {
    return toStats.getTripDuration();
  }

  @Override
  protected double scoreForSearch(TripStateStats toStats) throws NoPathException {
    return toStats.getTripDuration();
  }

}