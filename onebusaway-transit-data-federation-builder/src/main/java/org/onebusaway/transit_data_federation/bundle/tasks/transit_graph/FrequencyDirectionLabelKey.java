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

class FrequencyDirectionLabelKey extends FrequencyRouteLabelKey {

  private final String directionId;

  public FrequencyDirectionLabelKey(AgencyAndId routeId, AgencyAndId serviceId,
      String directionId) {
    super(routeId, serviceId);
    this.directionId = directionId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + super.hashCode();
    result = prime * result
        + (directionId == null ? 0 : directionId.hashCode());
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
    if (!super.equals(obj))
      return false;
    FrequencyDirectionLabelKey other = (FrequencyDirectionLabelKey) obj;
    if (directionId == null && other.directionId != null) {
      return false;
    } else if (!directionId.equals(other.directionId)) {
      return false;
    }
    return true;
  }
}
