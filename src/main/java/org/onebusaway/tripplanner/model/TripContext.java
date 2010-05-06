package org.onebusaway.tripplanner.model;

import org.onebusaway.gtfs.services.CalendarService;
import org.onebusaway.tripplanner.impl.WalkPlansImpl;
import org.onebusaway.tripplanner.services.StopTimeIndexContext;
import org.onebusaway.tripplanner.services.TripPlannerGraph;
import org.onebusaway.tripplanner.services.WalkPlannerService;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TripContext implements StopTimeIndexContext {

  private TripPlannerGraph _graph;

  private TripPlannerConstants _constants = new TripPlannerConstants();

  private WalkPlannerService _walkPlannerService;

  private CalendarService _calendarService;

  private TripPlannerConstraints _constraints;

  private WalkPlansImpl _walkPlans = new WalkPlansImpl();

  public void setGraph(TripPlannerGraph graph) {
    _graph = graph;
  }

  public TripPlannerGraph getGraph() {
    return _graph;
  }

  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  public TripPlannerConstants getConstants() {
    return _constants;
  }

  public void setConstants(TripPlannerConstants constants) {
    _constants = constants;
  }

  public void setWalkPlannerService(WalkPlannerService walkPlannerService) {
    _walkPlannerService = walkPlannerService;
  }

  public WalkPlannerService getWalkPlannerService() {
    return _walkPlannerService;
  }

  public void setConstraints(TripPlannerConstraints constraints) {
    _constraints = constraints;
  }

  public TripPlannerConstraints getConstraints() {
    return _constraints;
  }

  public WalkPlansImpl getWalkPlans() {
    return _walkPlans;
  }

  /*****************************************************************************
   * {@link StopTimeIndexContext} Interface
   ****************************************************************************/

  public Map<String, List<Date>> getNextServiceDates(Set<String> serviceIds, long targetTime) {
    return _calendarService.getNextDepartureServiceDates(serviceIds, targetTime);
  }

  public Map<String, List<Date>> getPreviousServiceDates(Set<String> serviceIds, long targetTime) {
    return _calendarService.getPreviousArrivalServiceDates(serviceIds, targetTime);
  }

}
