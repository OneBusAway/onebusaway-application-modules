package org.onebusaway.api.model.transit.blocks;

import org.onebusaway.api.model.transit.FrequencyV2Bean;

public class BlockInstanceV2Bean {

  private String blockId;

  private BlockConfigurationV2Bean blockConfiguration;

  private long serviceDate;

  private FrequencyV2Bean frequency;

  public String getBlockId() {
    return blockId;
  }

  public void setBlockId(String blockId) {
    this.blockId = blockId;
  }

  public BlockConfigurationV2Bean getBlockConfiguration() {
    return blockConfiguration;
  }

  public void setBlockConfiguration(BlockConfigurationV2Bean blockConfiguration) {
    this.blockConfiguration = blockConfiguration;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public FrequencyV2Bean getFrequency() {
    return frequency;
  }

  public void setFrequency(FrequencyV2Bean frequency) {
    this.frequency = frequency;
  }
}
