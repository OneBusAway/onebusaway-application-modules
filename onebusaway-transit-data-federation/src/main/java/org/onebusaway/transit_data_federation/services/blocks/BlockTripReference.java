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

public class BlockTripReference implements Serializable {

  private static final long serialVersionUID = 1L;

  private final BlockConfigurationReference blockConfigurationReference;

  private final int tripIndex;

  public BlockTripReference(
      BlockConfigurationReference blockConfigurationReference, int tripIndex) {
    if (blockConfigurationReference == null)
      throw new IllegalArgumentException();
    this.blockConfigurationReference = blockConfigurationReference;
    this.tripIndex = tripIndex;
  }

  public BlockConfigurationReference getBlockConfigurationReference() {
    return blockConfigurationReference;
  }

  public int getTripIndex() {
    return tripIndex;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + blockConfigurationReference.hashCode();
    result = prime * result + tripIndex;
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
    BlockTripReference other = (BlockTripReference) obj;
    if (!blockConfigurationReference.equals(other.blockConfigurationReference))
      return false;
    if (tripIndex != other.tripIndex)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "blockConfig=" + blockConfigurationReference + " tripIndex="
        + tripIndex;
  }
}
