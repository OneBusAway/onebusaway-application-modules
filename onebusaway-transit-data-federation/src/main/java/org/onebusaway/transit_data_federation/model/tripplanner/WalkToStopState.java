package org.onebusaway.transit_data_federation.model.tripplanner;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class WalkToStopState extends AtStopState {

  public WalkToStopState(long currentTime, StopEntry stop) {
    super(currentTime, stop);
  }

  public WalkToStopState shift(long offset) {
    return new WalkToStopState(getCurrentTime() + offset, getStop());
  }

  @Override
  public String toString() {
    return "WalkToStop(ts=" + getCurrentTimeString() + " stop=" + getStop() + ")";
  }
}
