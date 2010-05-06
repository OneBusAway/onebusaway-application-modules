package org.onebusaway.tripplanner.model;

import org.onebusaway.tripplanner.services.StopTimeInstanceProxy;

public class VehicleDepartureState extends VehicleState {

  public VehicleDepartureState(StopTimeInstanceProxy sti) {
    super(sti, false);
  }

  @Override
  public String toString() {
    StopTimeInstanceProxy sti = getStopTimeInstance();
    return "departure(ts=" + getCurrentTimeString() + " stop=" + sti.getStop().getStopId() + " trip="
        + getStopTimeInstance().getTripId() + ")";
  }

}
