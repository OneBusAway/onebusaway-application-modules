package org.onebusaway.transit_data_federation.impl.tripplanner;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.MinTravelTimeToStopsListener;

import java.util.HashMap;
import java.util.Map;

public class MinTravelTimeToStopsHandler implements MinTravelTimeToStopsListener {

  private Map<StopEntry, Long> _minTravelTimeToStop = new HashMap<StopEntry, Long>();

  public Map<StopEntry, Long> getResults() {
    return _minTravelTimeToStop;
  }

  /*****************************************************************************
   * {@link MinTravelTimeToStopsListener} Interface
   ****************************************************************************/

  public void putTrip(StopEntry stop, long duration) {

    Long existingTime = _minTravelTimeToStop.get(stop);

    if (existingTime == null || existingTime > duration) {
      _minTravelTimeToStop.put(stop, duration);
    }
  }

  public void setComplete() {
    
  }
}
