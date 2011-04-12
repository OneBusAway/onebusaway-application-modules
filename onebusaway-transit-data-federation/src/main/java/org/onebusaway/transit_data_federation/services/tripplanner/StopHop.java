package org.onebusaway.transit_data_federation.services.tripplanner;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public final class StopHop {

  private final StopEntry stop;

  private final int minTravelTime;

  public StopHop(StopEntry stop, int minTravelTime) {
    this.stop = stop;
    this.minTravelTime = minTravelTime;
  }

  public StopEntry getStop() {
    return stop;
  }

  public int getMinTravelTime() {
    return minTravelTime;
  }
}
