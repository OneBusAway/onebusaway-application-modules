package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.util.List;

import org.onebusaway.transit_data_federation.services.tripplanner.StopHop;

public class StopHops {

  private final List<StopHop> hopsFromStop;

  private final List<StopHop> hopsToStop;

  public StopHops(List<StopHop> hopsFromStop, List<StopHop> hopsToStop) {
    this.hopsFromStop = hopsFromStop;
    this.hopsToStop = hopsToStop;
  }

  public List<StopHop> getHopsFromStop() {
    return hopsFromStop;
  }

  public List<StopHop> getHopsToStop() {
    return hopsToStop;
  }
}
