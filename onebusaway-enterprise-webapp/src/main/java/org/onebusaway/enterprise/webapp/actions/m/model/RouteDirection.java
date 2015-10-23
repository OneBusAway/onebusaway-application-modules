package org.onebusaway.enterprise.webapp.actions.m.model;

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
  
  private List<StopOnRoute> stops;
  
  private List<String> distanceAways;
  
  private Boolean hasUpcomingScheduledService;

  public RouteDirection(String destinationName, StopGroupBean stopGroup, List<StopOnRoute> stops, Boolean hasUpcomingScheduledService, List<String> distanceAways) {
    this.directionId = stopGroup.getId();
    this.destination = destinationName;    
    this.stops = stops;
    this.hasUpcomingScheduledService = hasUpcomingScheduledService;
    this.distanceAways = distanceAways;
  }

  public String getDirectionId() {
    return directionId;
  }
  
  public String getDestination() {
    return destination;
  }

  public List<StopOnRoute> getStops() {
    return stops;
  }
  
  public Boolean getHasUpcomingScheduledService() {
    return hasUpcomingScheduledService;
  }
  
  public List<String> getDistanceAways() {
    return distanceAways;
  }

}
