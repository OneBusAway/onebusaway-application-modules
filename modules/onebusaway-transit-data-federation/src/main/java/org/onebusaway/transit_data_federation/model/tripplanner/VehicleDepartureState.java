package org.onebusaway.transit_data_federation.model.tripplanner;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;

public class VehicleDepartureState extends VehicleState {

  public VehicleDepartureState(StopTimeInstanceProxy sti) {
    super(sti, false);
  }

  @Override
  public String toString() {
    StopTimeInstanceProxy sti = getStopTimeInstance();
    return "departure(ts=" + getCurrentTimeString() + " stop=" + sti.getStop().getId() + " trip="
        + getStopTimeInstance().getTrip() + ")";
  }

}
