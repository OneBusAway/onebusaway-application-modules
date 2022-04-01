/**
 * Copyright (C) 2020 Cambridge Systematics, Inc.
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

import org.onebusaway.gtfs.model.AgencyAndId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Bean to support schedule APIs
 * Specifically:
 *  * "stopTripGroupings": [
 *  *         {
 *  *           "directionId": 0,
 *  *           "tripHeadsign": "University of Washington Station",
 *  *           "stopIds": ["STOPID1", "STOPID2"],
 *  *           "tripIds": ["TRIPID1", "TRIPID2"]
 *  *         },
 *  *         {
 *  *           "directionId": 1,
 *  *           "tripHeadsign": "Angle Lake Station",
 *  *           "stopIds": ["STOPID2", "STOPID3"],
 *  *           "tripIds": ["TRIPID3", "TRIPID4"]
 *  *         }
 *  *       ]
 */
public class StopsAndTripsForDirectionBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String directionId;
  private Set<String> tripHeadsigns = new HashSet<>();
  private List<AgencyAndId> stopIds;
  private List<AgencyAndId> tripIds;
  // this bean needs to support getReferences of API tier
  // the datasource of any V2 bean needed there needs to be provided here
  private List<StopTimeInstanceBeanExtendedWithStopId> stopTimes = new ArrayList<>();

  // TODO: add support for frequency implementations

  public String getDirectionId() {
    return directionId;
  }

  public void setDirectionId(String directionId) {
    this.directionId = directionId;
  }

  public Set<String> getTripHeadsigns() {
    return tripHeadsigns;
  }

  public void setTripHeadsigns(Set<String> tripHeadsigns) {
    this.tripHeadsigns = tripHeadsigns;
  }

  public void addTripHeadsign(String tripHeadsign) { this.tripHeadsigns.add(tripHeadsign); }

  public List<AgencyAndId> getStopIds() {
    return stopIds;
  }

  public void setStopIds(List<AgencyAndId> stopIds) {
    this.stopIds = stopIds;
  }

  public List<AgencyAndId> getTripIds() {
    return tripIds;
  }

  public void setTripIds(List<AgencyAndId> tripIds) {
    this.tripIds = tripIds;
  }

  public List<StopTimeInstanceBeanExtendedWithStopId> getStopTimes() {
    return stopTimes;
  }

  public void setStopTimes(List<StopTimeInstanceBeanExtendedWithStopId> stopTimes) {
    this.stopTimes = stopTimes;
  }

}
