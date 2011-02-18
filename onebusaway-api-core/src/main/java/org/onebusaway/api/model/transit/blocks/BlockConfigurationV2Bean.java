package org.onebusaway.api.model.transit.blocks;

import java.util.List;

public class BlockConfigurationV2Bean {

  private static final long serialVersionUID = 1L;

  private List<String> activeServiceIds;

  private List<String> inactiveServiceIds;

  private List<BlockTripV2Bean> trips;

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

  public List<BlockTripV2Bean> getTrips() {
    return trips;
  }

  public void setTrips(List<BlockTripV2Bean> trips) {
    this.trips = trips;
  }
}
