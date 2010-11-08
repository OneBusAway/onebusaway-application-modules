package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

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
