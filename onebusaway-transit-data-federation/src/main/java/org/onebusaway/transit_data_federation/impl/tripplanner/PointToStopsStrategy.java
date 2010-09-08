package org.onebusaway.transit_data_federation.impl.tripplanner;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.impl.tripplanner.comparison.DurationTripComparisonStrategy;
import org.onebusaway.transit_data_federation.impl.tripplanner.comparison.TripComparisonStrategy;
import org.onebusaway.transit_data_federation.model.tripplanner.AtStopState;
import org.onebusaway.transit_data_federation.model.tripplanner.StartState;
import org.onebusaway.transit_data_federation.model.tripplanner.TripContext;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstraints;
import org.onebusaway.transit_data_federation.model.tripplanner.TripState;
import org.onebusaway.transit_data_federation.model.tripplanner.TripStateStats;
import org.onebusaway.transit_data_federation.services.tripplanner.MinTravelTimeToStopsListener;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.walkplanner.NoPathException;

public class PointToStopsStrategy extends AbstractTripSearchStrategy {

  private MinTravelTimeToStopsListener _listener;

  public PointToStopsStrategy(TripContext context, CoordinatePoint startPoint, MinTravelTimeToStopsListener listener) {
    super(context);

    _listener = listener;

    TripPlannerConstraints constraints = context.getConstraints();
    StartState start = new StartState(constraints.getMinDepartureTime(), startPoint);
    addOrigin(start);
  }

  public void getMinTravelTimeToStop() {
    explore();
    _listener.setComplete();
  }

  /*****************************************************************************
   * Protected Methods
   ****************************************************************************/

  @Override
  protected void handleState(TripStateStats state) {

    TripState s = state.getState();

    if (s instanceof AtStopState) {

      AtStopState atStop = (AtStopState) s;
      StopEntry stop = atStop.getStop();
      long duration = state.getTripDuration();
      _listener.putTrip(stop, duration);
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