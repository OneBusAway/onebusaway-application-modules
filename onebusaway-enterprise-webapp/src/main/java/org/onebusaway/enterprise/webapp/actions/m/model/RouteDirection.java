/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.enterprise.webapp.actions.m.model;

import org.onebusaway.transit_data.model.StopGroupBean;

import java.util.List;

/**
 * Route destination
 * @author jmaki
 *
 */
public class RouteDirection {

  private String directionId;
  
  private String destination;
  
  private List<StopOnRoute> stops;
  
  private List<String> distanceAways;
  
  private Boolean hasUpcomingScheduledService;

  public RouteDirection(String destinationName, StopGroupBean stopGroup, List<StopOnRoute> stops, Boolean hasUpcomingScheduledService, List<String> distanceAways) {
    this.directionId = stopGroup.getId();
    this.destination = destinationName;    
    this.stops = stops;
    this.hasUpcomingScheduledService = hasUpcomingScheduledService;
    this.distanceAways = distanceAways;
  }

  public String getDirectionId() {
    return directionId;
  }
  
  public String getDestination() {
    return destination;
  }

  public List<StopOnRoute> getStops() {
    return stops;
  }
  
  public Boolean getHasUpcomingScheduledService() {
    return hasUpcomingScheduledService;
  }
  
  public List<String> getDistanceAways() {
    return distanceAways;
  }

}
