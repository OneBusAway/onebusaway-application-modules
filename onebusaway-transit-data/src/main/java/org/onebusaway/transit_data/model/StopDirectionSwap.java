package org.onebusaway.transit_data.model;
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
import org.onebusaway.gtfs.model.AgencyAndId;

import java.io.Serializable;

/**
 * Swap stops for a given stop - direction - stop.  Aka wrong way concurrency.
 */
public class StopDirectionSwap implements Serializable {
  private AgencyAndId routeId;
  private String directionId;
  private AgencyAndId fromStop;
  private AgencyAndId toStop;

  public AgencyAndId getRouteId() {
    return routeId;
  }

  public void setRouteId(AgencyAndId routeId) {
    this.routeId = routeId;
  }

  public String getDirectionId() {
    return directionId;
  }

  public void setDirectionId(String directionId) {
    this.directionId = directionId;
  }

  public AgencyAndId getFromStop() {
    return fromStop;
  }

  public void setFromStop(AgencyAndId fromStop) {
    this.fromStop = fromStop;
  }

  public AgencyAndId getToStop() {
    return toStop;
  }

  public void setToStop(AgencyAndId toStop) {
    this.toStop = toStop;
  }
}
