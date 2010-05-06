package org.onebusaway.tripplanner.model;

import com.vividsolutions.jts.geom.Point;

import java.text.DateFormat;

public abstract class TripState implements Comparable<TripState> {

  private static DateFormat _format = DateFormat.getTimeInstance(DateFormat.SHORT);

  private final long currentTime;

  public TripState(long currentTime) {
    this.currentTime = currentTime;
  }

  public abstract Point getLocation();

  public long getCurrentTime() {
    return currentTime;
  }

  public int compareTo(TripState o) {
    return this.currentTime == o.currentTime ? 0
        : (this.currentTime < o.currentTime ? -1 : 1);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (!getClass().equals(obj.getClass()))
      return false;
    TripState ts = (TripState) obj;
    return this.currentTime == ts.currentTime;
  }

  @Override
  public int hashCode() {
    return (int) this.currentTime;
  }

  /****
   *
   ****/

  protected String getCurrentTimeString() {
    return _format.format(currentTime);
  }

}
