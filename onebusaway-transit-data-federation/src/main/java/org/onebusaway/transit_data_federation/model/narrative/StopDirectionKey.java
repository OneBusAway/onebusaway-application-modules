/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.model.narrative;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.io.Serializable;

/**
 * Key for pattern narrative.
 */
public class StopDirectionKey implements Serializable {

  private AgencyAndId stopId;
  private String directionId;

  public StopDirectionKey(AgencyAndId stopId, String directionId) {
    this.stopId = stopId;
    this.directionId = directionId;
  }

  public AgencyAndId getStopId() {
    return stopId;
  }

  public String getDirectionId() {
    return directionId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (!(obj instanceof StopDirectionKey))
      return false;
    StopDirectionKey sd = (StopDirectionKey) obj;
    if (sd.stopId == null && stopId != null)
      return false;
    if (sd.directionId == null && directionId != null)
      return false;
    return stopId.equals(sd.stopId)
            && directionId.equals(sd.directionId);
  }
  @Override
  public int hashCode() {
    int hash = 17;
    if (stopId != null)
      hash += stopId.hashCode();
    if (directionId != null)
      hash += directionId.hashCode();
    return hash;
  }
}
