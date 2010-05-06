package org.onebusaway.tripplanner.model;

import com.vividsolutions.jts.geom.Point;

import org.onebusaway.gtdf.model.Stop;

public class WaitingAtStopState extends TripState {

  private final Stop _stop;

  public WaitingAtStopState(long currentTime, Stop stop) {
    super(currentTime);
    _stop = stop;
  }

  public Stop getStop() {
    return _stop;
  }

  @Override
  public Point getLocation() {
    return _stop.getLocation();
  }

  @Override
  public String toString() {
    return "waiting(ts=" + getCurrentTimeString() + " stop=" + _stop.getId()
        + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj))
      return false;
    WaitingAtStopState ws = (WaitingAtStopState) obj;
    return _stop.equals(ws._stop);
  }

  @Override
  public int hashCode() {
    return super.hashCode() + _stop.hashCode();
  }

}
