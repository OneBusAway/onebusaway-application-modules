package org.onebusaway.transit_data_federation.model.tripplanner;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;

public class VehicleArrivalState extends VehicleState {

  public VehicleArrivalState(StopTimeInstanceProxy sti) {
    super(sti,true);
  }

  @Override
  public String toString() {
    StopTimeInstanceProxy sti = getStopTimeInstance();
    return "arrival(ts=" + getCurrentTimeString() + " stop="
        + sti.getStop().getId() + ")";
  }
}
