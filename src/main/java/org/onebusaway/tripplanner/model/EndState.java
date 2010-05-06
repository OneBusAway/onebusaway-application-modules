package org.onebusaway.tripplanner.model;

import com.vividsolutions.jts.geom.Point;

public class EndState extends TripState {

  private Point _location;

  public EndState(long currentTime, Point location) {
    super(currentTime);
    _location = location;
  }

  @Override
  public Point getLocation() {
    return _location;
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj))
      return false;
    EndState es = (EndState) obj;
    return _location.equals(es._location);
  }

  @Override
  public int hashCode() {
    return super.hashCode() + _location.hashCode();
  }

  @Override
  public String toString() {
    return "end(ts=" + getCurrentTimeString() + " location=" + _location + ")";
  }
}
