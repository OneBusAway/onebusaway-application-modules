package org.onebusaway.transit_data_federation.model.tripplanner;

import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;

public class AtStopState extends TripState {

  private final StopEntry _stop;

  public AtStopState(long currentTime, StopEntry stop) {
    super(currentTime);
    _stop = stop;
  }

  public StopEntry getStop() {
    return _stop;
  }

  @Override
  public String toString() {
    return "atStop(ts=" + getCurrentTimeString() + " stop=" + _stop + ")";
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
