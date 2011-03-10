package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;

/**
 * Service methods for querying the real-time status and position of a
 * particular trip.
 * 
 * @author bdferris
 * @see TripStatusBean
 * @see TripDetailsBean
 */
public interface TripStatusBeanService {

  /**
   * @param tripId see {@link Trip#getId()}
   * @param serviceDate the service date the trip is operating under (Unix-time)
   * @param time the time of operation to query
   * @return the status info for a particular trip operating on the specified
   *         service date and time
   */
  public TripStatusBean getTripStatusForTripId(AgencyAndId tripId,
      long serviceDate, long time);

  /**
   * 
   * @param vehicleId
   * @param time
   * @param tripDetailsInclusionBean controls what will be included in the
   *          response
   * @return trip details for the trip operated by the specified vehicle at the
   *         specified time, or null if not found
   */
  public TripDetailsBean getTripStatusForVehicleAndTime(AgencyAndId vehicleId,
      long time, TripDetailsInclusionBean tripDetailsInclusionBean);

  /**
   * @param query
   * @return the list of active trips matching the query criteria
   */
  public ListBean<TripDetailsBean> getActiveTripForBounds(
      TripsForBoundsQueryBean query);
}
