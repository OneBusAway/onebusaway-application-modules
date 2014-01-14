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

import java.io.Serializable;
import java.util.List;

import org.onebusaway.transit_data.model.EAccessibility;

public class StopV2Bean implements Serializable, HasId {

  private static final long serialVersionUID = 2L;

  private String id;

  private double lat;

  private double lon;

  private String direction;

  private String name;

  private String code;

  private int locationType;

  private EAccessibility wheelchairBoarding;
  
  private List<String> routeIds;

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

  public String getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public EAccessibility getWheelchairBoarding() {
    return wheelchairBoarding;
  }

  public void setWheelchairBoarding(EAccessibility wheelchairBoarding) {
    this.wheelchairBoarding = wheelchairBoarding;
  }

  public List<String> getRouteIds() {
    return routeIds;
  }

  public void setRouteIds(List<String> routeIds) {
    this.routeIds = routeIds;
  }
}
