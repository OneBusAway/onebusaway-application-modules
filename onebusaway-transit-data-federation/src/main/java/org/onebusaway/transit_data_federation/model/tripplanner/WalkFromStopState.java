package org.onebusaway.transit_data_federation.model.tripplanner;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class WalkFromStopState extends AtStopState {

  public WalkFromStopState(long currentTime, StopEntry stop) {
    super(currentTime, stop);
  }

  @Override
  public String toString() {
    return "WalkFromStop(ts=" + getCurrentTimeString() + " stop=" + getStop() + ")";
  }
}
