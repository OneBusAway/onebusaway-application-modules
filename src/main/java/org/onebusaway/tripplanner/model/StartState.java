package org.onebusaway.tripplanner.model;

import com.vividsolutions.jts.geom.Point;

public class StartState extends TripState {

  private final Point _location;

  public StartState(long currentTime, Point location) {
    super(currentTime);
    _location = location;
  }

  public Point getLocation() {
    return _location;
  }

  @Override
  public String toString() {
    return "start(ts=" + getCurrentTimeString() + " location=" + _location
        + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj))
      return false;

    StartState ss = (StartState) obj;
    return _location.equals(ss._location);
  }

  @Override
  public int hashCode() {
    return super.hashCode() + _location.hashCode();
  }

}
