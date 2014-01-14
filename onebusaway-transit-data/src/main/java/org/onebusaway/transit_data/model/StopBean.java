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

import java.io.Serializable;
import java.util.List;

public class StopBean implements Serializable {

  private static final long serialVersionUID = 2L;

  private String id;

  private double lat;

  private double lon;

  private String direction;

  private String name;

  private String code;

  private int locationType;

  private List<RouteBean> routes;
  
  private EAccessibility wheelchairBoarding;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLon() {
    return lon;
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  public boolean hasDirection() {
    return direction != null;
  }

  public String getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public int getLocationType() {
    return locationType;
  }

  public void setLocationType(int locationType) {
    this.locationType = locationType;
  }

  public List<RouteBean> getRoutes() {
    return routes;
  }

  public void setRoutes(List<RouteBean> routes) {
    this.routes = routes;
  }

  public EAccessibility getWheelchairBoarding() {
    return wheelchairBoarding;
  }

  public void setWheelchairBoarding(EAccessibility wheelchairBoarding) {
    this.wheelchairBoarding = wheelchairBoarding;
  }

  /***************************************************************************
   * {@link Object} Interface
   **************************************************************************/

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof StopBean))
      return false;
    StopBean stop = (StopBean) obj;
    return id.equals(stop.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
