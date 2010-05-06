package org.onebusaway.tripplanner.model;

import com.vividsolutions.jts.geom.Point;

public class EndState extends TripState {

  public EndState(long currentTime, Point location) {
    super(currentTime, location);
  }

  @Override
  public String toString() {
    return "end(ts=" + getCurrentTimeString() + " location=" + getLocation() + ")";
  }
}
