package org.onebusaway.tripplanner.model;

import org.onebusaway.tripplanner.services.StopProxy;

public class WalkToStopState extends AtStopState {

  public WalkToStopState(long currentTime, StopProxy stop) {
    super(currentTime, stop);
  }

  public WalkToStopState shift(long offset) {
    return new WalkToStopState(getCurrentTime() + offset, getStop());
  }

  @Override
  public String toString() {
    return "WalkToStop(ts=" + getCurrentTimeString() + " stop=" + getStop().getStopId() + ")";
  }
}
