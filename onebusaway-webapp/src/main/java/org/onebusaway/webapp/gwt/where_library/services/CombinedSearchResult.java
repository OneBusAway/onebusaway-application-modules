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
