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
package org.onebusaway.transit_data.model;

import java.util.ArrayList;
import java.util.List;

public class StopSequenceBlockBean extends ApplicationBean {

  private static final long serialVersionUID = 1L;

  private RouteBean route;

  private String id;

  private String description;

  private double startLat;

  private double startLon;

  private double endLat;

  private double endLon;

  private List<PathBean> paths = new ArrayList<PathBean>();

  private List<StopBean> stops;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public RouteBean getRoute() {
    return route;
  }

  public void setRoute(RouteBean route) {
    this.route = route;
  }

  public double getStartLat() {
    return startLat;
  }

  public void setStartLat(double startLat) {
    this.startLat = startLat;
  }

  public double getStartLon() {
    return startLon;
  }

  public void setStartLon(double startLon) {
    this.startLon = startLon;
  }

  public double getEndLat() {
    return endLat;
  }

  public void setEndLat(double endLat) {
    this.endLat = endLat;
  }

  public double getEndLon() {
    return endLon;
  }

  public void setEndLon(double endLon) {
    this.endLon = endLon;
  }

  public List<PathBean> getPaths() {
    return paths;
  }

  public void setPaths(List<PathBean> paths) {
    this.paths = paths;
  }

  public List<StopBean> getStops() {
    return stops;
  }

  public void setStops(List<StopBean> stops) {
    this.stops = stops;
  }
}
