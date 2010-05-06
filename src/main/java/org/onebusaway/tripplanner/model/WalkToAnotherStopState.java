package org.onebusaway.tripplanner.model;

import com.vividsolutions.jts.geom.Point;

import org.onebusaway.gtdf.model.Stop;

public class WalkToAnotherStopState extends TripState {

  private Stop _stop;

  public WalkToAnotherStopState(long currentTime, Stop stop) {
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
    return "walk(ts=" + getCurrentTimeString() + " stop=" + _stop.getId() + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj))
      return false;
    WalkToAnotherStopState ws = (WalkToAnotherStopState) obj;
    return _stop.equals(ws._stop);
  }

  @Override
  public int hashCode() {
    return super.hashCode() + _stop.hashCode();
  }
}
