package org.onebusaway.transit_data_federation.model.tripplanner;

import org.onebusaway.transit_data_federation.impl.walkplanner.WalkPlansImpl;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerService;

public class TripContext {

  private TripPlannerGraph _graph;

  private TripPlannerConstants _constants = new TripPlannerConstants();

  private WalkPlannerService _walkPlannerService;

  private TripPlannerConstraints _constraints;

  private WalkPlansImpl _walkPlans = new WalkPlansImpl();

  private StopTimeService _stopTimeService;

  public void setGraph(TripPlannerGraph graph) {
    _graph = graph;
  }

  public TripPlannerGraph getGraph() {
    return _graph;
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

  public void setStopTimeService(StopTimeService stopTimeService) {
    _stopTimeService = stopTimeService;
  }

  public StopTimeService getStopTimeService() {
    return _stopTimeService;
  }
}
