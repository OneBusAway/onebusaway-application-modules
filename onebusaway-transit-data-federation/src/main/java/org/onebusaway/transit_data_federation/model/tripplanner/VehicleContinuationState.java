package org.onebusaway.transit_data_federation.model.tripplanner;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;

public class VehicleContinuationState extends VehicleState {

  public VehicleContinuationState(StopTimeInstance sti) {
    super(sti,false);
  }

  @Override
  public String toString() {
    StopTimeInstance sti = getStopTimeInstance();
    return "continuation(ts=" + getCurrentTimeString() + " stop="
        + sti.getStop().getId() + ")";
  }

}
