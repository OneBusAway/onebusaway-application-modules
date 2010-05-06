package org.onebusaway.tripplanner.model;

import com.vividsolutions.jts.geom.Point;

public class StartState extends TripState {

  public StartState(long currentTime, Point location) {
    super(currentTime, location);
  }

  public StartState shift(long offset) {
    return new StartState(getCurrentTime() + offset, getLocation());
  }

  @Override
  public String toString() {
    return "start(ts=" + getCurrentTimeString() + " location=" + getLocation() + ")";
  }
}
