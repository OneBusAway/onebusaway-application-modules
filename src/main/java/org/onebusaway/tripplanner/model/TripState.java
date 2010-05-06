package org.onebusaway.tripplanner.model;

import com.vividsolutions.jts.geom.Point;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public abstract class TripState implements Comparable<TripState> {

  private static DateFormat _format = new SimpleDateFormat("HH:mm:ss");

  private final long currentTime;

  private final Point location;

  public TripState(long currentTime, Point location) {
    this.currentTime = currentTime;
    this.location = location;
  }

  public Point getLocation() {
    return location;
  }

  public long getCurrentTime() {
    return currentTime;
  }

  public int compareTo(TripState o) {
    return this.currentTime == o.currentTime ? 0 : (this.currentTime < o.currentTime ? -1 : 1);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (!getClass().equals(obj.getClass()))
      return false;
    TripState ts = (TripState) obj;
    return this.currentTime == ts.currentTime && this.location.getX() == ts.location.getX()
        && this.location.getY() == ts.location.getY();
  }

  @Override
  public int hashCode() {
    return ((int) this.currentTime) + new Double(this.location.getX()).hashCode()
        + new Double(this.location.getY()).hashCode();
  }

  /****
   *
   ****/

  protected String getCurrentTimeString() {
    return _format.format(currentTime);
  }
}
