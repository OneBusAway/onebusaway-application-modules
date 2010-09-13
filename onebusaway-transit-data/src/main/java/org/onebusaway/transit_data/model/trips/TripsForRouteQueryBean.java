package org.onebusaway.transit_data.model.trips;

import org.onebusaway.transit_data.model.QueryBean;

@QueryBean
public final class TripsForRouteQueryBean extends AbstractTripsQueryBean {

  private static final long serialVersionUID = 1L;

  private String routeId;

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }
}
