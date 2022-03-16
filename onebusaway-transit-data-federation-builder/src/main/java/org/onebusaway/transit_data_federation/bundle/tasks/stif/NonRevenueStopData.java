/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.bundle.tasks.stif;

import java.io.Serializable;

import org.locationtech.jts.geom.Coordinate;

public class NonRevenueStopData implements Serializable {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private NonRevenueStopOrder nonRevenueStopOrder;
  
  private Coordinate location;
  
  private int scheduleTime;

  public NonRevenueStopOrder getNonRevenueStopOrder() {
    return nonRevenueStopOrder;
  }

  public void setNonRevenueStopOrder(NonRevenueStopOrder nonRevenueStopOrder) {
    this.nonRevenueStopOrder = nonRevenueStopOrder;
  }

  public Coordinate getLocation() {
    return location;
  }

  public void setLocation(Coordinate location) {
    this.location = location;
  }

  public int getScheduleTime() {
    return scheduleTime;
  }

  public void setScheduleTime(int scheduleTime) {
    this.scheduleTime = scheduleTime;
  }

}
