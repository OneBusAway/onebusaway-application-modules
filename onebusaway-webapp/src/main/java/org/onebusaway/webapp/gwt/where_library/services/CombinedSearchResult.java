package org.onebusaway.webapp.gwt.where_library.services;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.webapp.gwt.common.control.Place;

public class CombinedSearchResult {

  private List<RouteBean> routes = new ArrayList<RouteBean>();

  private List<StopBean> stops = new ArrayList<StopBean>();

  private List<Place> addresses = new ArrayList<Place>();

  private List<Place> places = new ArrayList<Place>();

  public List<RouteBean> getRoutes() {
    return routes;
  }

  public void setRoutes(List<RouteBean> routes) {
    this.routes = routes;
  }

  public List<StopBean> getStops() {
    return stops;
  }

  public void setStops(List<StopBean> stops) {
    this.stops = stops;
  }

  public List<Place> getAddresses() {
    return addresses;
  }

  public void setAddresses(List<Place> addresses) {
    this.addresses = addresses;
  }

  public List<Place> getPlaces() {
    return places;
  }

  public void setPlaces(List<Place> places) {
    this.places = places;
  }

  public boolean isEmpty() {
    return routes.isEmpty() && stops.isEmpty() && addresses.isEmpty()
        && places.isEmpty();
  }
}
