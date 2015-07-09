package org.onebusaway.enterprise.webapp.actions.api.model;

import org.onebusaway.transit_data.model.RouteBean;

import java.util.List;

/**
 * Route available at a stop, the stop being the top-level result.
 * @author jmaki
 *
 */
public class RouteAtStop {

  private RouteBean route;
  
  private List<RouteDirection> directions;
  
  public RouteAtStop(RouteBean route, List<RouteDirection> directions) {
    this.route = route;
    this.directions = directions;
  }
  
  public String getId() {
    return route.getId();
  }
  
  public String getShortName() {
    return route.getShortName();
  }

  public String getLongName() {
    return route.getLongName();
  }
  
  public String getDescription() {
    return route.getDescription();
  }

  public String getColor() {
    if(route.getColor() != null) {
      return route.getColor();
    } else {
      return "000000";
    }
  }  

  public List<RouteDirection> getDirections() {
    return directions;
  }  

}
