package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data.model.TripStopTimeBean;
import org.onebusaway.transit_data.model.TripStopTimesBean;

/**
 * Service methods for accessing the list of stop times for a specified trip.
 * Here, {@link TripStopTimesBean} and {@link TripStopTimeBean} serve as
 * high-level descriptors of low level {@link Trip} and {@link StopTime}
 * objects.
 * 
 * @author bdferris
 * 
 */
public interface TripStopTimesBeanService {

  /**
   * 
   * @param tripId see {@link Trip#getId()}
   * @return the list of stop times for the specified trip
   */
  public TripStopTimesBean getStopTimesForTrip(AgencyAndId tripId);

}
