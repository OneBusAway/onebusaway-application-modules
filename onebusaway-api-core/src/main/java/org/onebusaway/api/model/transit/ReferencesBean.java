/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.api.model.transit;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.api.model.transit.service_alerts.SituationV2Bean;

public class ReferencesBean {

  private List<AgencyV2Bean> agencies;

  private List<RouteV2Bean> routes;

  private List<StopV2Bean> stops;

  private List<TripV2Bean> trips;

  private List<SituationV2Bean> situations;

  private List<ScheduleStopTimeInstanceV2Bean> stopTimes;

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

  public List<SituationV2Bean> getSituations() {
    return situations;
  }

  public void setSituations(List<SituationV2Bean> situations) {
    this.situations = situations;
  }

  public void addSituation(SituationV2Bean situation) {
    if (situations == null)
      situations = new ArrayList<SituationV2Bean>();
    situations.add(situation);
  }

  public List<ScheduleStopTimeInstanceV2Bean> getStopTimes() {
    return stopTimes;
  }

  public void setStopTimes(List<ScheduleStopTimeInstanceV2Bean> stopTimes) {
    this.stopTimes = stopTimes;
  }

  public void addStopTime(ScheduleStopTimeInstanceV2Bean stopTime) {
    if (stopTimes == null)
      stopTimes = new ArrayList<ScheduleStopTimeInstanceV2Bean>();
    stopTimes.add(stopTime);
  }
}
