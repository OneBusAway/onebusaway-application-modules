package org.onebusaway.tripplanner.model;

import org.onebusaway.tripplanner.services.StopProxy;

public class AtStopState extends TripState {

  private final StopProxy _stop;

  public AtStopState(long currentTime, StopProxy stop) {
    super(currentTime, stop.getStopLocation());
    _stop = stop;
  }

  public StopProxy getStop() {
    return _stop;
  }

  public String getStopId() {
    return _stop.getStopId();
  }

  @Override
  public String toString() {
    return "atStop(ts=" + getCurrentTimeString() + " stop=" + _stop.getStopId() + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj))
      return false;
    AtStopState ws = (AtStopState) obj;
    return _stop.equals(ws._stop);
  }

  @Override
  public int hashCode() {
    return super.hashCode() + _stop.hashCode();
  }

}
