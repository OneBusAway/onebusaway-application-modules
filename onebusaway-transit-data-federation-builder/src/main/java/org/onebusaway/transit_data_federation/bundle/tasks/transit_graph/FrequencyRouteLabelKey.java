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

import org.onebusaway.gtfs.model.AgencyAndId;

class FrequencyRouteLabelKey {

  private final AgencyAndId routeId;

  private final AgencyAndId serviceId;

  public FrequencyRouteLabelKey(AgencyAndId routeId, AgencyAndId serviceId) {
    if (routeId == null)
      throw new IllegalArgumentException();
    if (serviceId == null)
      throw new IllegalArgumentException();
    this.routeId = routeId;
    this.serviceId = serviceId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + routeId.hashCode();
    result = prime * result + serviceId.hashCode();
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
    FrequencyRouteLabelKey other = (FrequencyRouteLabelKey) obj;
    if (!routeId.equals(other.routeId))
      return false;
    if (!serviceId.equals(other.serviceId))
      return false;
    return true;
  }

}
