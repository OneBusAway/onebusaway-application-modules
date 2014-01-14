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
package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

class BlockSequenceKey {

  private final ServiceIdActivation _serviceIds;

  private final List<AgencyAndId> _stopIds;


  public BlockSequenceKey(ServiceIdActivation serviceIds, List<AgencyAndId> stopIds) {
    _serviceIds = serviceIds;
    _stopIds = stopIds;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _serviceIds.hashCode();
    result = prime * result + _stopIds.hashCode();
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
    BlockSequenceKey other = (BlockSequenceKey) obj;
    return _serviceIds.equals(other._serviceIds)
        && _stopIds.equals(other._stopIds);
  }
}