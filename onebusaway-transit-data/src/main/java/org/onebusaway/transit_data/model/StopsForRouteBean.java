package org.onebusaway.transit_data.model;

import org.onebusaway.geospatial.model.EncodedPolylineBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StopsForRouteBean implements Serializable {

  private static final long serialVersionUID = 3L;

  private RouteBean route;

  private List<StopBean> stops;

  private List<StopGroupingBean> stopGroupings;

  private List<EncodedPolylineBean> polylines;

  public RouteBean getRoute() {
    return route;
  }

  public void setRoute(RouteBean route) {
    this.route = route;
  }

  public List<StopBean> getStops() {
    return stops;
  }

  public void setStops(List<StopBean> stops) {
    this.stops = stops;
  }

  public List<StopGroupingBean> getStopGroupings() {
    return stopGroupings;
  }

  public void setStopGroupings(List<StopGroupingBean> groupings) {
    this.stopGroupings = groupings;
  }

  public List<EncodedPolylineBean> getPolylines() {
    return polylines;
  }

  public void setPolylines(List<EncodedPolylineBean> polylines) {
    this.polylines = polylines;
  }

  public void addGrouping(StopGroupingBean grouping) {
    if (stopGroupings == null)
      stopGroupings = new ArrayList<StopGroupingBean>();
    stopGroupings.add(grouping);
  }
}
