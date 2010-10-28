package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.util.Collection;
import java.util.Map;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.model.tripplanner.TripContext;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlan;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstants;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstraints;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.MinTravelTimeToStopsListener;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerService;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class TripPlannerServiceImpl implements TripPlannerService {

  private WalkPlannerService _walkPlanner;

  private TripPlannerConstants _constants;

  private TransitGraphDao _transitGraphDao;

  private StopTimeService _stopTimeService;

  @Autowired
  public void setWalkPlannerService(WalkPlannerService walkPlannerService) {
    _walkPlanner = walkPlannerService;
  }

  @Autowired
  public void setTripPlannerConstants(TripPlannerConstants constants) {
    _constants = constants;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setStopTimeService(StopTimeService stopTimeService) {
    _stopTimeService = stopTimeService;
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
    context.setTransitGraphDao(_transitGraphDao);
    context.setWalkPlannerService(_walkPlanner);
    context.setStopTimeService(_stopTimeService);
    context.setConstraints(constraints);

    return context;
  }
}
