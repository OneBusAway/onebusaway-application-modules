package org.onebusaway.transit_data.model;

import java.io.Serializable;

public final class TripBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;

  private String tripShortName;

  private String tripHeadsign;

  private String routeShortName;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTripShortName() {
    return tripShortName;
  }

  public void setTripShortName(String tripShortName) {
    this.tripShortName = tripShortName;
  }

  public String getTripHeadsign() {
    return tripHeadsign;
  }

  public void setTripHeadsign(String tripHeadsign) {
    this.tripHeadsign = tripHeadsign;
  }

  public String getRouteShortName() {
    return routeShortName;
  }

  public void setRouteShortName(String routeShortName) {
    this.routeShortName = routeShortName;
  }
}
