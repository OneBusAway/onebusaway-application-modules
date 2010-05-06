/*
 * Copyright 2008 Greg Briggs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.transit_data.model;

import java.util.List;

public class StopsWithArrivalsAndDeparturesBean extends ApplicationBean {

  private static final long serialVersionUID = 1L;

  private List<StopBean> stops;

  private List<ArrivalAndDepartureBean> arrivalsAndDepartures;

  private List<StopBean> nearbyStops;
  
  private String timeZone;

  public StopsWithArrivalsAndDeparturesBean() {

  }

  public StopsWithArrivalsAndDeparturesBean(List<StopBean> stops,
      List<ArrivalAndDepartureBean> arrivalsAndDepartures,
      List<StopBean> nearbyStops) {
    this.stops = stops;
    this.arrivalsAndDepartures = arrivalsAndDepartures;
  }

  public List<StopBean> getStops() {
    return stops;
  }

  public void setStops(List<StopBean> stops) {
    this.stops = stops;
  }

  public List<ArrivalAndDepartureBean> getArrivalsAndDepartures() {
    return arrivalsAndDepartures;
  }

  public void setArrivalsAndDepartures(
      List<ArrivalAndDepartureBean> arrivalsAndDepartures) {
    this.arrivalsAndDepartures = arrivalsAndDepartures;
  }

  public List<StopBean> getNearbyStops() {
    return nearbyStops;
  }

  public void setNearbyStops(List<StopBean> nearbyStops) {
    this.nearbyStops = nearbyStops;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }
}
