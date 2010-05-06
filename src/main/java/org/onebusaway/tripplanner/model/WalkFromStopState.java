package org.onebusaway.tripplanner.model;

import org.onebusaway.tripplanner.services.StopProxy;

public class WalkFromStopState extends AtStopState {

  public WalkFromStopState(long currentTime, StopProxy stop) {
    super(currentTime, stop);
  }

  @Override
  public String toString() {
    return "WalkFromStop(ts=" + getCurrentTimeString() + " stop=" + getStop().getStopId() + ")";
  }
}
