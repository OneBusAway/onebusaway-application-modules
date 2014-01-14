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
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.transit_data.model.schedule.FrequencyInstanceBean;

public class StopRouteDirectionScheduleBean implements Serializable {

  private static final long serialVersionUID = 2L;

  private String tripHeadsign;

  private List<StopTimeInstanceBean> stopTimes = new ArrayList<StopTimeInstanceBean>();

  private List<FrequencyInstanceBean> frequencies = new ArrayList<FrequencyInstanceBean>();
  
  private List<StopTimeGroupBean> groups;

  public String getTripHeadsign() {
    return tripHeadsign;
  }

  public void setTripHeadsign(String tripHeadsign) {
    this.tripHeadsign = tripHeadsign;
  }

  public List<StopTimeInstanceBean> getStopTimes() {
    return stopTimes;
  }

  public void setStopTimes(List<StopTimeInstanceBean> stopTimes) {
    this.stopTimes = stopTimes;
  }

  public List<FrequencyInstanceBean> getFrequencies() {
    return frequencies;
  }

  public void setFrequencies(List<FrequencyInstanceBean> frequencies) {
    this.frequencies = frequencies;
  }

  public List<StopTimeGroupBean> getGroups() {
    return groups;
  }

  public void setGroups(List<StopTimeGroupBean> groups) {
    this.groups = groups;
  }
}
