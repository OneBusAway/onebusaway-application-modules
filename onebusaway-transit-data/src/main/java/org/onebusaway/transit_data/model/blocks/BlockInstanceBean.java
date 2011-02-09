package org.onebusaway.transit_data.model.blocks;

import java.io.Serializable;

import org.onebusaway.transit_data.model.schedule.FrequencyBean;

public class BlockInstanceBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String blockId;

  private BlockConfigurationBean blockConfiguration;

  private long serviceDate;

  private FrequencyBean frequency;

  public String getBlockId() {
    return blockId;
  }

  public void setBlockId(String blockId) {
    this.blockId = blockId;
  }

  public BlockConfigurationBean getBlockConfiguration() {
    return blockConfiguration;
  }

  public void setBlockConfiguration(BlockConfigurationBean blockConfiguration) {
    this.blockConfiguration = blockConfiguration;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public FrequencyBean getFrequency() {
    return frequency;
  }

  public void setFrequency(FrequencyBean frequency) {
    this.frequency = frequency;
  }
}
