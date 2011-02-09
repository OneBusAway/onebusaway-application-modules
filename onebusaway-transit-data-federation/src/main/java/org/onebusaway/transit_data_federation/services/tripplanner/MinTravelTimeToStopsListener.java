package org.onebusaway.transit_data_federation.services.tripplanner;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public interface MinTravelTimeToStopsListener {
  public void putTrip(StopEntry stop, long duration);
  public void setComplete();
}
