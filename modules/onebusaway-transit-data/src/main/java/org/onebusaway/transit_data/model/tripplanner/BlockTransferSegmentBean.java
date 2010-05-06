package org.onebusaway.transit_data.model.tripplanner;

import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.TripBean;

public class BlockTransferSegmentBean extends TripSegmentBean {

  private static final long serialVersionUID = 1L;

  private RouteBean route;

  private TripBean trip;

  public RouteBean getRoute() {
    return route;
  }

  public void setRoute(RouteBean route) {
    this.route = route;
  }

  public TripBean getTrip() {
    return trip;
  }

  public void setTrip(TripBean trip) {
    this.trip = trip;
  }
}
