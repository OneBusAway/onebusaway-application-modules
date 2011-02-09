package org.onebusaway.transit_data_federation.model.tripplanner;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;

public abstract class VehicleState extends AtStopState {

  private final StopTimeInstance _sti;

  public VehicleState(StopTimeInstance sti, boolean arrival) {
    super(arrival ? sti.getArrivalTime() : sti.getDepartureTime(), sti.getStopTime().getStopTime().getStop());
    _sti = sti;
  }

  public StopTimeInstance getStopTimeInstance() {
    return _sti;
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj))
      return false;
    VehicleState vs = (VehicleState) obj;
    return _sti.equals(vs._sti);
  }

  @Override
  public int hashCode() {
    return super.hashCode() + _sti.hashCode();
  }
}