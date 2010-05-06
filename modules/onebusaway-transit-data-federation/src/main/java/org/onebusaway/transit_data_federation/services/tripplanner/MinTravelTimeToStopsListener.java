package org.onebusaway.transit_data_federation.services.tripplanner;

public interface MinTravelTimeToStopsListener {
  public void putTrip(StopEntry stop, long duration);
  public void setComplete();
}
