package org.onebusaway.transit_data_federation.model.tripplanner;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;

public class VehicleContinuationState extends VehicleState {

  public VehicleContinuationState(StopTimeInstanceProxy sti) {
    super(sti,false);
  }

  @Override
  public String toString() {
    StopTimeInstanceProxy sti = getStopTimeInstance();
    return "continuation(ts=" + getCurrentTimeString() + " stop="
        + sti.getStop().getId() + ")";
  }

}
