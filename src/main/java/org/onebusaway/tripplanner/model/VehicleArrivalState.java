package org.onebusaway.tripplanner.model;

import org.onebusaway.tripplanner.services.StopTimeInstanceProxy;

public class VehicleArrivalState extends VehicleState {

  public VehicleArrivalState(StopTimeInstanceProxy sti) {
    super(sti,true);
  }

  @Override
  public String toString() {
    StopTimeInstanceProxy sti = getStopTimeInstance();
    return "arrival(ts=" + getCurrentTimeString() + " stop="
        + sti.getStop().getStopId() + ")";
  }
}
