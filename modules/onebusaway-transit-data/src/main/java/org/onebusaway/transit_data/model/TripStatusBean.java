package org.onebusaway.transit_data.model;

import org.onebusaway.geospatial.model.CoordinatePoint;

import java.io.Serializable;

public final class TripStatusBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private TripBean trip;

  private RouteBean route;
  
  private CoordinatePoint position;

  public TripBean getTrip() {
    return trip;
  }

  public void setTrip(TripBean trip) {
    this.trip = trip;
  }

  public RouteBean getRoute() {
    return route;
  }

  public void setRoute(RouteBean route) {
    this.route = route;
  }

  public CoordinatePoint getPosition() {
    return position;
  }

  public void setPosition(CoordinatePoint position) {
    this.position = position;
  }
}
