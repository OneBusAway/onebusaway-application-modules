package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data.model.trips.TripBean;

/**
 * Service methods to lookup a {@link TripBean} representation of a {@link Trip}
 * object.
 * 
 * @author bdferris
 * @see TripBean
 * @see Trip
 */
public interface TripBeanService {

  /**
   * @param tripId see {@link Trip#getId()}
   * @return retrieve a bean representation of the specified trip, or null if
   *         not found
   */
  public TripBean getTripForId(AgencyAndId tripId);
}
