package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;

public interface RouteBeanService {
  public RouteBean getRouteForId(AgencyAndId id);
  public StopsForRouteBean getStopsForRoute(AgencyAndId routeId);
}
