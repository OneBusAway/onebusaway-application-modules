package org.onebusaway.transit_data_federation.model.tripplanner;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;

public class VehicleArrivalState extends VehicleState {

  public VehicleArrivalState(StopTimeInstance sti) {
    super(sti,true);
  }

  @Override
  public String toString() {
    StopTimeInstance sti = getStopTimeInstance();
    return "arrival(ts=" + getCurrentTimeString() + " stop="
        + sti.getStop().getId() + ")";
  }
}
