package org.onebusaway.transit_data.model.tripplanner;

import org.onebusaway.transit_data.model.StopBean;

public class DepartureSegmentBean extends TripSegmentBean {

  private static final long serialVersionUID = 1L;

  private StopBean stop;

  private String routeId;

  private String tripId;

  private String routeShortName;

  private String routeLongName;

  private String tripHeadsign;

  public StopBean getStop() {
    return stop;
  }

  public void setStop(StopBean stop) {
    this.stop = stop;
  }

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public String getRouteShortName() {
    return routeShortName;
  }

  public void setRouteShortName(String routeShortName) {
    this.routeShortName = routeShortName;
  }

  public String getRouteLongName() {
    return routeLongName;
  }

  public void setRouteLongName(String routeLongName) {
    this.routeLongName = routeLongName;
  }

  public String getTripHeadsign() {
    return tripHeadsign;
  }

  public void setTripHeadsign(String tripHeadsign) {
    this.tripHeadsign = tripHeadsign;
  }
}
