package org.onebusaway.api.model.transit.blocks;

import java.util.List;

public class BlockV2Bean {
  private String id;

  private List<BlockConfigurationV2Bean> configurations;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<BlockConfigurationV2Bean> getConfigurations() {
    return configurations;
  }

  public void setConfigurations(List<BlockConfigurationV2Bean> configurations) {
    this.configurations = configurations;
  }
}
