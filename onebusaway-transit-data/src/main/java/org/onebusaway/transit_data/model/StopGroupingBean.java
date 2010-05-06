package org.onebusaway.transit_data.model;

import java.io.Serializable;
import java.util.List;

public class StopGroupingBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String type;

  private boolean ordered;

  private List<StopGroupBean> stopGroups;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isOrdered() {
    return ordered;
  }

  public void setOrdered(boolean ordered) {
    this.ordered = ordered;
  }

  public List<StopGroupBean> getStopGroups() {
    return stopGroups;
  }

  public void setStopGroups(List<StopGroupBean> groups) {
    this.stopGroups = groups;
  }
}
