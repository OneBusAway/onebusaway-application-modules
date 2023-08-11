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
package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.io.Serializable;

/**
 * Key for Stop direction route swap -- aka wrong way concurrency.
 */
public class StopDirectionSwapKey implements Serializable {
  private AgencyAndId routeId;
  private String directionId;
  private AgencyAndId stopId;

  public StopDirectionSwapKey(AgencyAndId routeId, String directionId, AgencyAndId fromStop) {
    this.routeId = routeId;
    this.directionId = directionId;
    this.stopId = fromStop;
  }
  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (!(obj instanceof StopDirectionSwapKey))
      return false;
    StopDirectionSwapKey k = (StopDirectionSwapKey) obj;
    return k.routeId.equals(routeId)
            && k.directionId.equals(directionId)
            && k.stopId.equals(stopId);
  }

  @Override
  public int hashCode() {
    return routeId.hashCode() + directionId.hashCode()
            + stopId.hashCode();
  }
}
