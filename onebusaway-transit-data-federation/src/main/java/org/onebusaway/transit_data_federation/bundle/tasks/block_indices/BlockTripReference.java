package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

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
