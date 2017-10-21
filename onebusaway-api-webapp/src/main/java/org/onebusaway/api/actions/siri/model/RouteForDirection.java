/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.api.actions.siri.model;

import java.util.List;

public class RouteForDirection {

  private String directionId;

  private String destination;

  private String routeId;

  private Boolean hasUpcomingScheduledService;

  private List<StopOnRoute> stops;

  public RouteForDirection(String routeId, String directionId,
      Boolean hasUpcomingScheduledService) {
    this.routeId = routeId;
    this.directionId = directionId;
    this.hasUpcomingScheduledService = hasUpcomingScheduledService;
    this.setDestination(null);
  }

  public RouteForDirection(String routeId, String directionId){
    this.routeId = routeId;
    this.directionId = directionId;
  }

  public String getDirectionId() {
    return directionId;
  }

  public void setDirectionId(String directionId) {
    this.directionId = directionId;
  }

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeBean) {
    this.routeId = routeBean;
  }

  public Boolean getHasUpcomingScheduledService() {
    return hasUpcomingScheduledService;
  }

  public void setHasUpcomingScheduledService(
      Boolean hasUpcomingScheduledService) {
    this.hasUpcomingScheduledService = hasUpcomingScheduledService;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public List<StopOnRoute> getStops() {
    return stops;
  }

  public void setStops(List<StopOnRoute> stops) {
    this.stops = stops;
  }
}
