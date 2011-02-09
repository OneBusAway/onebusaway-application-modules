package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopBean;

import java.util.List;

/**
 * Service for explicity finding stops near a target stop.
 * 
 * @author bdferris
 */
public interface NearbyStopsBeanService {

  /**
   * @param stopBean the target stop to search for nearby stops
   * @param radius the search radius in meters around the stop location
   * @return ids of stops within the radius of the target stop (but excludes the
   *         target stop id itself)
   */
  public List<AgencyAndId> getNearbyStops(StopBean stopBean, double radius);
}
