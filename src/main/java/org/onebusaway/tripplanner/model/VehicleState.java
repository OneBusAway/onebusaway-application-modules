package org.onebusaway.tripplanner.model;

import org.onebusaway.tripplanner.services.StopTimeInstanceProxy;

public abstract class VehicleState extends AtStopState {

  private final StopTimeInstanceProxy _sti;

  public VehicleState(StopTimeInstanceProxy sti, boolean arrival) {
    super(arrival ? sti.getArrivalTime() : sti.getDepartureTime(), sti.getStop());
    _sti = sti;
  }

  public StopTimeInstanceProxy getStopTimeInstance() {
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