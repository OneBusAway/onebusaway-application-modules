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
package org.onebusaway.transit_data.model.trips;

import java.io.Serializable;

import org.onebusaway.transit_data.model.QueryBean;

@QueryBean
public abstract class AbstractTripsQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private long time = System.currentTimeMillis();
  
  private int minutesBefore = 90;
  
  private int minutesAfter = 90;

  private int maxCount = 0;

  private TripDetailsInclusionBean inclusion = new TripDetailsInclusionBean();

  public void setMinutesBefore(int time) {
    this.minutesBefore = time;
  }

  public void setMinutesAfter(int time) {
    this.minutesAfter = time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public long getTime() {
    return time;
  }

  public int getMinutesBefore() {
    return minutesBefore;
  }

  public int getMinutesAfter() {
    return minutesAfter;
  }
  
  public int getMaxCount() {
    return maxCount;
  }

  public void setMaxCount(int maxCount) {
    this.maxCount = maxCount;
  }

  public TripDetailsInclusionBean getInclusion() {
    return inclusion;
  }

  public void setInclusion(TripDetailsInclusionBean inclusion) {
    this.inclusion = inclusion;
  }

}
