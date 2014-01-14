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
package org.onebusaway.transit_data_federation.services.blocks;

import java.io.Serializable;

import org.onebusaway.gtfs.model.AgencyAndId;

public class BlockConfigurationReference implements Serializable {

  private static final long serialVersionUID = 1L;

  private final AgencyAndId blockId;

  private final int configurationIndex;

  public BlockConfigurationReference(AgencyAndId blockId, int configurationIndex) {
    if (blockId == null)
      throw new IllegalArgumentException();
    this.blockId = blockId;
    this.configurationIndex = configurationIndex;
  }

  public AgencyAndId getBlockId() {
    return blockId;
  }

  public int getConfigurationIndex() {
    return configurationIndex;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + blockId.hashCode();
    result = prime * result + configurationIndex;
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
    BlockConfigurationReference other = (BlockConfigurationReference) obj;
    if (!blockId.equals(other.blockId))
      return false;
    if (configurationIndex != other.configurationIndex)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "blockId=" + blockId + " index=" + configurationIndex;
  }
}
