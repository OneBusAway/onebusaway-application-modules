package org.onebusaway.api.model.transit;

import java.util.ArrayList;
import java.util.List;

public class ReferencesBean {

  private List<AgencyV2Bean> agencies;

  private List<RouteV2Bean> routes;

  private List<StopV2Bean> stops;

  private List<TripV2Bean> trips;

  public List<AgencyV2Bean> getAgencies() {
    return agencies;
  }

  public void setAgencies(List<AgencyV2Bean> agencies) {
    this.agencies = agencies;
  }

  public void addAgency(AgencyV2Bean bean) {
    if (agencies == null)
      agencies = new ArrayList<AgencyV2Bean>();
    agencies.add(bean);
  }

  public List<RouteV2Bean> getRoutes() {
    return routes;
  }

  public void setRoutes(List<RouteV2Bean> routes) {
    this.routes = routes;
  }

  public void addRoute(RouteV2Bean route) {
    if (routes == null)
      routes = new ArrayList<RouteV2Bean>();
    routes.add(route);
  }

  public List<StopV2Bean> getStops() {
    return stops;
  }

  public void setStops(List<StopV2Bean> stops) {
    this.stops = stops;
  }

  public void addStop(StopV2Bean stop) {
    if (stops == null)
      stops = new ArrayList<StopV2Bean>();
    stops.add(stop);
  }

  public List<TripV2Bean> getTrips() {
    return trips;
  }

  public void setTrips(List<TripV2Bean> trips) {
    this.trips = trips;
  }

  public void addTrip(TripV2Bean trip) {
    if (trips == null)
      trips = new ArrayList<TripV2Bean>();
    trips.add(trip);
  }
}
