package org.onebusaway.transit_data.model.blocks;

import java.io.Serializable;
import java.util.List;

public final class BlockBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;

  private List<BlockConfigurationBean> configurations;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<BlockConfigurationBean> getConfigurations() {
    return configurations;
  }

  public void setConfigurations(List<BlockConfigurationBean> configurations) {
    this.configurations = configurations;
  }
}
