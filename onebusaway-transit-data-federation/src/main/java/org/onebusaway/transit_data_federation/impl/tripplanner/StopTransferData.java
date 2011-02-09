package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.io.Serializable;

import org.onebusaway.gtfs.model.AgencyAndId;

public final class StopTransferData implements Serializable {

  private static final long serialVersionUID = 1L;

  private final AgencyAndId stopId;

  private final int minTransferTime;

  private final double distance;

  public StopTransferData(AgencyAndId stopId, int minTransferTime,
      double distance) {
    this.stopId = stopId;
    this.minTransferTime = minTransferTime;
    this.distance = distance;
  }

  public AgencyAndId getStopId() {
    return stopId;
  }

  public int getMinTransferTime() {
    return minTransferTime;
  }

  public double getDistance() {
    return distance;
  }
}
