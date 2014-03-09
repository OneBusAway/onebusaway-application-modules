/**
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;

class FrequencyLabelKey {

  private final AgencyAndId routeId;

  private final AgencyAndId serviceId;

  private final List<AgencyAndId> stopIds;

  public FrequencyLabelKey(AgencyAndId routeId, AgencyAndId serviceId,
      List<AgencyAndId> stopIds) {
    if (routeId == null)
      throw new IllegalArgumentException();
    if (serviceId == null)
      throw new IllegalArgumentException();
    if (stopIds == null)
      throw new IllegalArgumentException();
    this.routeId = routeId;
    this.serviceId = serviceId;
    this.stopIds = stopIds;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + routeId.hashCode();
    result = prime * result + serviceId.hashCode();
    result = prime * result + stopIds.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FrequencyLabelKey other = (FrequencyLabelKey) obj;
    if (routeId == null) {
      if (other.routeId != null)
        return false;
    } else if (!routeId.equals(other.routeId))
      return false;
    if (serviceId == null) {
      if (other.serviceId != null)
        return false;
    } else if (!serviceId.equals(other.serviceId))
      return false;
    if (stopIds == null) {
      if (other.stopIds != null)
        return false;
    } else if (!stopIds.equals(other.stopIds))
      return false;
    return true;
  }

}
