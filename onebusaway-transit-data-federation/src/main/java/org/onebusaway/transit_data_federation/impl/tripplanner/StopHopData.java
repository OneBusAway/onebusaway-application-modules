package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.io.Serializable;

import org.onebusaway.gtfs.model.AgencyAndId;

public final class StopHopData implements Serializable {

  private static final long serialVersionUID = 1L;

  private final AgencyAndId stopId;

  private final int minTravelTime;

  public StopHopData(AgencyAndId stopId, int minTravelTime) {
    this.stopId = stopId;
    this.minTravelTime = minTravelTime;
  }

  public AgencyAndId getStopId() {
    return stopId;
  }

  public int getMinTravelTime() {
    return minTravelTime;
  }
}
