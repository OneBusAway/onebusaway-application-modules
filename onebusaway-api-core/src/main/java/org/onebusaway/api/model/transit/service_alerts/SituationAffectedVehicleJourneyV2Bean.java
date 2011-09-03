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
package org.onebusaway.api.model.transit.service_alerts;

import java.io.Serializable;
import java.util.List;

public final class SituationAffectedVehicleJourneyV2Bean implements
    Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * A Line in SIRI/TransModel speak is equivalent to a Route Collection in OBA
   */
  private String lineId;

  private String directionId;

  private List<SituationAffectedCallV2Bean> calls;

  private List<String> tripIds;

  public String getLineId() {
    return lineId;
  }

  public void setLineId(String lineId) {
    this.lineId = lineId;
  }

  public String getDirectionId() {
    return directionId;
  }

  public void setDirectionId(String directionId) {
    this.directionId = directionId;
  }

  public List<SituationAffectedCallV2Bean> getCalls() {
    return calls;
  }

  public void setCalls(List<SituationAffectedCallV2Bean> calls) {
    this.calls = calls;
  }

  public List<String> getTripIds() {
    return tripIds;
  }

  public void setTripIds(List<String> tripIds) {
    this.tripIds = tripIds;
  }
}
