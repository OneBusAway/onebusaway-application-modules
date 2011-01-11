package org.onebusaway.transit_data.model.blocks;

import java.io.Serializable;
import java.util.List;

public final class BlockConfigurationBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String blockId;

  private List<String> activeServiceIds;

  private List<String> inactiveServiceIds;

  private List<BlockTripBean> trips;
  
  private String timeZone;

  public String getBlockId() {
    return blockId;
  }

  public void setBlockId(String blockId) {
    this.blockId = blockId;
  }

  public List<String> getActiveServiceIds() {
    return activeServiceIds;
  }

  public void setActiveServiceIds(List<String> activeServiceIds) {
    this.activeServiceIds = activeServiceIds;
  }

  public List<String> getInactiveServiceIds() {
    return inactiveServiceIds;
  }

  public void setInactiveServiceIds(List<String> inactiveServiceIds) {
    this.inactiveServiceIds = inactiveServiceIds;
  }

  public List<BlockTripBean> getTrips() {
    return trips;
  }

  public void setTrips(List<BlockTripBean> trips) {
    this.trips = trips;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }
}
