package org.onebusaway.tripplanner.impl;

import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.gtfs.services.CalendarService;
import org.onebusaway.tripplanner.model.TripContext;
import org.onebusaway.tripplanner.model.TripPlan;
import org.onebusaway.tripplanner.model.TripPlannerConstants;
import org.onebusaway.tripplanner.model.TripPlannerConstraints;
import org.onebusaway.tripplanner.offline.TripPlannerGraphImpl;
import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.TripPlannerService;
import org.onebusaway.tripplanner.services.WalkPlannerService;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import com.vividsolutions.jts.geom.Point;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Map;

public class TripPlannerServiceImpl implements TripPlannerService {

  @Autowired
  private ProjectionService _projection;

  @Autowired
  private CalendarService _calendarService;

  @Autowired
  private WalkPlannerService _walkPlanner;

  private TripPlannerConstants _constants = new TripPlannerConstants();

  private TripPlannerGraphImpl _graph;

  public void setTripPlannerGraph(TripPlannerGraphImpl graph) {
    _graph = graph;
    _graph.initialize();
  }

  public void setTripPlannerConstants(TripPlannerConstants constants) {
    _constants = constants;
  }

  public Map<StopProxy, Long> getMinTravelTimeToStopsFrom(CoordinatePoint from, TripPlannerConstraints constraints) {

    Point pointFrom = _projection.getCoordinatePointAsPoint(from);

    TripContext context = createContext(constraints);

    PointToStopsStrategy strategy = new PointToStopsStrategy(context, pointFrom);
    return strategy.getMinTravelTimeToStop();
  }

  public Collection<TripPlan> getTripsBetween(CoordinatePoint from, CoordinatePoint to,
      TripPlannerConstraints constraints) {

    Point pointFrom = _projection.getCoordinatePointAsPoint(from);
    Point pointTo = _projection.getCoordinatePointAsPoint(to);

    TripContext context = createContext(constraints);
    PointToPointStrategy strategy = new PointToPointStrategy(context, pointFrom, pointTo);
    // PointToPointGeneralStrategy strategy = new
    // PointToPointGeneralStrategy(context, pointFrom, pointTo);

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
