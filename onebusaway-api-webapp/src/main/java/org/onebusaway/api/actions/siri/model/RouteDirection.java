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

import org.onebusaway.transit_data.model.StopGroupBean;

public class RouteDirection {

  private String directionId;
  
  private String destination;
  
  private List<String> polylines;

  private List<StopOnRoute> stops;
  
  private Boolean hasUpcomingScheduledService;

  public RouteDirection(StopGroupBean stopGroup, List<String> polylines, 
      List<StopOnRoute> stops, Boolean hasUpcomingScheduledService) {
    this.directionId = stopGroup.getId();
    this.destination = stopGroup.getName().getName();
    this.polylines = polylines;
    this.stops = stops;
    this.hasUpcomingScheduledService = hasUpcomingScheduledService;
  }

  public String getDirectionId() {
    return directionId;
  }
  
  public String getDestination() {
    return destination;
  }

  public List<String> getPolylines() {
    return polylines;
  }

  public List<StopOnRoute> getStops() {
    return stops;
  }
  
  public Boolean getHasUpcomingScheduledService() {
    return hasUpcomingScheduledService;
  }
}
