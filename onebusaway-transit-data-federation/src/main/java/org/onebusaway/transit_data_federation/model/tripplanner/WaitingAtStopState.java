package org.onebusaway.transit_data_federation.model.tripplanner;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class WaitingAtStopState extends AtStopState {

  public WaitingAtStopState(long currentTime, StopEntry stop) {
    super(currentTime, stop);
  }

  public WaitingAtStopState shift(long offset) {
    return new WaitingAtStopState(getCurrentTime() + offset, getStop());
  }

  @Override
  public String toString() {
    return "WaitingAtStop(ts=" + getCurrentTimeString() + " stop=" + getStop() + ")";
  }
}
