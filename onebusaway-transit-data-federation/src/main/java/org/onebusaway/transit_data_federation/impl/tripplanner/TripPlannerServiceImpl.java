package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.util.Collection;
import java.util.Map;

import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data_federation.model.tripplanner.TripContext;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlan;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstants;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstraints;
import org.onebusaway.transit_data_federation.services.tripplanner.MinTravelTimeToStopsListener;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerService;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

@Component
class TripPlannerServiceImpl implements TripPlannerService {

  private CalendarService _calendarService;

  private WalkPlannerService _walkPlanner;

  private TripPlannerConstants _constants;

  private TripPlannerGraph _graph;

  @Autowired
  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  @Autowired
  public void setWalkPlannerService(WalkPlannerService walkPlannerService) {
    _walkPlanner = walkPlannerService;
  }

  @Autowired
  public void setTripPlannerConstants(TripPlannerConstants constants) {
    _constants = constants;
  }

  @Autowired
  public void setTripPlannerGraph(TripPlannerGraph graph) {
    _graph = graph;
  }

  public Map<StopEntry, Long> getMinTravelTimeToStopsFrom(CoordinatePoint from,
      TripPlannerConstraints constraints) {
    MinTravelTimeToStopsHandler handler = new MinTravelTimeToStopsHandler();
    getMinTravelTimeToStopsFrom(from, constraints, handler);
    return handler.getResults();
  }

  public void getMinTravelTimeToStopsFrom(CoordinatePoint from,
      TripPlannerConstraints constraints, MinTravelTimeToStopsListener listener) {

    TripContext context = createContext(constraints);
    PointToStopsStrategy strategy = new PointToStopsStrategy(context, from,
        listener);
    strategy.getMinTravelTimeToStop();
  }

  public Collection<TripPlan> getTripsBetween(CoordinatePoint from,
      CoordinatePoint to, TripPlannerConstraints constraints) {

    TripContext context = createContext(constraints);
    PointToPointStrategy strategy = new PointToPointStrategy(context, from, to);
    return strategy.getTrips();
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private TripContext createContext(TripPlannerConstraints constraints) {

    TripContext context = new TripContext();

    context.setConstants(_constants);
    context.setGraph(_graph);
    context.setWalkPlannerService(_walkPlanner);
    context.setCalendarService(_calendarService);
    context.setConstraints(constraints);

    return context;
  }
}
