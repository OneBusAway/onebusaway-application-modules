package org.onebusaway.tripplanner.model;

import org.onebusaway.gtdf.model.Route;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.gtdf.model.Trip;
import org.onebusaway.where.model.StopTimeInstance;

public class VehicleDepartureState extends VehicleState {

  public VehicleDepartureState(StopTimeInstance sti) {
    super(sti.getDepartureTime().getTime(), sti);
  }

  @Override
  public String toString() {
    StopTimeInstance sti = getStopTimeInstance();
    StopTime st = sti.getStopTime();
    Stop stop = st.getStop();
    Trip trip = st.getTrip();
    Route route = trip.getRoute();
    return "departure(ts=" + getCurrentTimeString() + " stop=" + stop.getId()
        + " route=" + route.getShortName() + ")";
  }

}
