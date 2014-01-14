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
package org.onebusaway.transit_data_federation.impl.realtime;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;

class BlockLocationRecordKey {
  private final BlockInstance blockInstance;
  private final AgencyAndId vehicleId;

  public BlockLocationRecordKey(BlockInstance blockInstance,
      AgencyAndId vehicleId) {
    if (blockInstance == null)
      throw new IllegalArgumentException("blockInstance is null");
    if (vehicleId == null)
      throw new IllegalArgumentException("vehicleId is null");
    this.blockInstance = blockInstance;
    this.vehicleId = vehicleId;
  }

  public BlockInstance getBlockInstance() {
    return blockInstance;
  }

  public AgencyAndId getVehicleId() {
    return vehicleId;
  }

  @Override
  public String toString() {
    return "key(blockInstance=" + blockInstance + " vehicleId=" + vehicleId
        + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + blockInstance.hashCode();
    result = prime * result + vehicleId.hashCode();
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
    BlockLocationRecordKey other = (BlockLocationRecordKey) obj;
    if (!blockInstance.equals(other.blockInstance))
      return false;
    if (!vehicleId.equals(other.vehicleId))
      return false;
    return true;
  }

}