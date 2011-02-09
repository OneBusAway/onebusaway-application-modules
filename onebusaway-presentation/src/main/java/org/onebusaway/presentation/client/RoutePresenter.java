package org.onebusaway.presentation.client;

import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.trips.TripBean;

public class RoutePresenter {
  
  public static String getNameForRoute(TripBean trip){
    String name = trip.getRouteShortName();
    if( name == null)
      name = getNameForRoute(trip.getRoute());
    return name;
  }
  
  public static String getNameForRoute(RouteBean route) {
    String name = route.getShortName();
    if (name == null)
      name = route.getLongName();
    if (name == null)
      name = route.getId();
    return name;
  }
  
  public static boolean isRouteNameLong(String name) {
    return name.length() > 5;
  }

  public static String getDescriptionForRoute(RouteBean route) {
    String value = route.getDescription();
    if (route.getShortName() != null && route.getLongName() != null)
      value = route.getLongName();
    return value;
  }
}
