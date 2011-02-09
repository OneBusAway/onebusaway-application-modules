package org.onebusaway.transit_data_federation.model.tripplanner;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;

public class VehicleDepartureState extends VehicleState {

  public VehicleDepartureState(StopTimeInstance sti) {
    super(sti, false);
  }

  @Override
  public String toString() {
    StopTimeInstance sti = getStopTimeInstance();
    return "departure(ts=" + getCurrentTimeString() + " stop=" + sti.getStop().getId() + " trip="
        + getStopTimeInstance().getTrip() + ")";
  }

}
