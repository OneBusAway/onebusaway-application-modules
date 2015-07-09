package org.onebusaway.enterprise.webapp.actions.api.model;

import org.onebusaway.transit_data.model.StopBean;

/**
 * A stop on a route, the route being the top-level search result.
 * @author jmaki
 *
 */
public class StopOnRoute {

  private StopBean stop;
  
  public StopOnRoute(StopBean stop) {
    this.stop = stop;
  }
  
  public String getId() {
    return stop.getId();
  }
  
  public String getName() {
    return stop.getName();
  }
  
  public Double getLatitude() {
    return stop.getLat();
  }
  
  public Double getLongitude() {
    return stop.getLon();
  }

  public String getStopDirection() {
    if(stop.getDirection() == null || (stop.getDirection() != null && stop.getDirection().equals("?"))) {
      return "unknown";
    } else {
      return stop.getDirection();
    }
  }  

}
