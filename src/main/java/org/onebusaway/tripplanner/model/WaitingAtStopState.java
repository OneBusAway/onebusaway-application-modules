package org.onebusaway.tripplanner.model;

import org.onebusaway.tripplanner.services.StopProxy;

public class WaitingAtStopState extends AtStopState {

  public WaitingAtStopState(long currentTime, StopProxy stop) {
    super(currentTime, stop);
  }

  public WaitingAtStopState shift(long offset) {
    return new WaitingAtStopState(getCurrentTime() + offset, getStop());
  }

  @Override
  public String toString() {
    return "WaitingAtStop(ts=" + getCurrentTimeString() + " stop=" + getStop().getStopId() + ")";
  }
}
