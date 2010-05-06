package org.onebusaway.transit_data_federation.model.tripplanner;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public abstract class TripState implements Comparable<TripState> {

  private static DateFormat _format = new SimpleDateFormat("HH:mm:ss");

  private final long currentTime;

  public TripState(long currentTime) {
    this.currentTime = currentTime;
  }

  public long getCurrentTime() {
    return currentTime;
  }

  public int compareTo(TripState o) {
    return this.currentTime == o.currentTime ? 0 : (this.currentTime < o.currentTime ? -1 : 1);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (currentTime ^ (currentTime >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TripState other = (TripState) obj;
    if (currentTime != other.currentTime)
      return false;
    return true;
  }

  /****
   *
   ****/

  protected String getCurrentTimeString() {
    return _format.format(currentTime);
  }
}
