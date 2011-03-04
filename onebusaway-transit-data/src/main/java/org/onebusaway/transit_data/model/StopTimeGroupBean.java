package org.onebusaway.transit_data.model;

import java.io.Serializable;

public class StopTimeGroupBean implements Serializable {

  private static final long serialVersionUID = 2L;

  private String id;

  private String tripHeadsign;

  private RouteBean continuesAs;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTripHeadsign() {
    return tripHeadsign;
  }

  public void setTripHeadsign(String tripHeadsign) {
    this.tripHeadsign = tripHeadsign;
  }

  public RouteBean getContinuesAs() {
    return continuesAs;
  }

  public void setContinuesAs(RouteBean continuesAs) {
    this.continuesAs = continuesAs;
  }
}
