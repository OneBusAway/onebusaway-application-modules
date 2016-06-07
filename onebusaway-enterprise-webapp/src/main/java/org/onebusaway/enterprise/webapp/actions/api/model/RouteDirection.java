package org.onebusaway.enterprise.webapp.actions.api.model;

import org.onebusaway.transit_data.model.StopGroupBean;

import java.util.List;

/**
 * Route destination
 * @author jmaki
 *
 */
public class RouteDirection {

  private String directionId;
  
  private String destination;
  
  private List<String> polylines;

  private List<StopOnRoute> stops;
  
  private Boolean hasUpcomingScheduledService;

  public RouteDirection(StopGroupBean stopGroup, List<String> polylines, 
      List<StopOnRoute> stops, Boolean hasUpcomingScheduledService) {
    this.directionId = stopGroup.getId();
    this.destination = stopGroup.getName().getName();
    this.polylines = polylines;
    this.stops = stops;
    this.hasUpcomingScheduledService = hasUpcomingScheduledService;
  }

  public String getDirectionId() {
    return directionId;
  }
  
  public String getDestination() {
    return destination;
  }

  public List<String> getPolylines() {
    return polylines;
  }

  public List<StopOnRoute> getStops() {
    return stops;
  }
  
  public Boolean getHasUpcomingScheduledService() {
    return hasUpcomingScheduledService;
  }

}
