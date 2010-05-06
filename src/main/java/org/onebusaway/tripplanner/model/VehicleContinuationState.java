package org.onebusaway.tripplanner.model;

import org.onebusaway.tripplanner.services.StopTimeInstanceProxy;

public class VehicleContinuationState extends VehicleState {

  public VehicleContinuationState(StopTimeInstanceProxy sti) {
    super(sti,false);
  }

  @Override
  public String toString() {
    StopTimeInstanceProxy sti = getStopTimeInstance();
    return "continuation(ts=" + getCurrentTimeString() + " stop="
        + sti.getStop().getStopId() + ")";
  }

}
