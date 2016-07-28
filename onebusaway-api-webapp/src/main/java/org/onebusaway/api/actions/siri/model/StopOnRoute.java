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

import org.onebusaway.transit_data.model.StopBean;

public class StopOnRoute {

  private StopBean stop;
  
  private Boolean hasUpcomingScheduledStop;
  
  public StopOnRoute(StopBean stop) {
    this.stop = stop;
  }
  
  public StopOnRoute(StopBean stop, Boolean hasUpcomingScheduledStop) {
    this.stop = stop;
    this.hasUpcomingScheduledStop = hasUpcomingScheduledStop;
  }
  
  public String getId() {
    return stop.getId();
  }
  
  public String getName() {
    return stop.getName();
  }
  
  public Double getLatitude() {
    return stop.getLat();
  }
  
  public Double getLongitude() {
    return stop.getLon();
  }

  public String getStopDirection() {
    if(stop.getDirection() == null || (stop.getDirection() != null && stop.getDirection().equals("?"))) {
      return "unknown";
    } else {
      return stop.getDirection();
    }
  }

  public Boolean getHasUpcomingScheduledStop() {
    return hasUpcomingScheduledStop;
  }
  
  public void setHasUpcomingScheduledStop(Boolean hasUpcomingScheduledStop) {
    this.hasUpcomingScheduledStop = hasUpcomingScheduledStop;
  }
}
