package org.onebusaway.transit_data_federation.services.realtime;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;

import java.util.List;

/**
 * Service that applies real-time information to a set of stop time instance
 * objects, adding position and arrival prediction data where available.
 * 
 * @author bdferris
 */
public interface StopRealtimeService {

  /**
   * Apply real-time information to a set of {@link StopTimeInstanceProxy}
   * objects, adding position and arrival prediction data where available.
   * 
   * @param stopTimes the stop time instances to update with real-time data
   * @param targetTime the target time to evaluate real-time data against
   *          (typically just use {@link System#currentTimeMillis()}
   */
  public void applyRealtimeData(List<StopTimeInstanceProxy> stopTimes,
      long targetTime);
}
