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

public class StopWithArrivalsAndDeparturesV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String stopId;

  private List<ArrivalAndDepartureV2Bean> arrivalsAndDepartures;

  private List<String> nearbyStopIds;

  private List<String> situationIds;

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public List<ArrivalAndDepartureV2Bean> getArrivalsAndDepartures() {
    return arrivalsAndDepartures;
  }

  public void setArrivalsAndDepartures(
      List<ArrivalAndDepartureV2Bean> arrivalsAndDepartures) {
    this.arrivalsAndDepartures = arrivalsAndDepartures;
  }

  public List<String> getNearbyStopIds() {
    return nearbyStopIds;
  }

  public void setNearbyStopIds(List<String> nearbyStopIds) {
    this.nearbyStopIds = nearbyStopIds;
  }

  public List<String> getSituationIds() {
    return situationIds;
  }

  public void setSituationIds(List<String> situationIds) {
    this.situationIds = situationIds;
  }
}
