package org.onebusaway.transit_data_federation.services.realtime;

import java.util.Map;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.tripplanner.TripInstanceProxy;

/**
 * Service methods for accessing/interpolating the position of a transit vehicle
 * give a trip instance and target time.
 * 
 * @author bdferris
 * 
 */
public interface TripLocationService {

  /**
   * Given a coordinate region and time, determine the set of trips that are
   * scheduled to be active in that region at the specified time.
   * 
   * @param bounds
   * @param time
   * @return the set of trip instances along with their positions
   */
  public Map<TripInstanceProxy, TripLocation> getScheduledTripsForBounds(
      CoordinateBounds bounds, long time);

  /**
   * Given a trip instance and a target time, determine the vehicle position at
   * that time. Use real-time data when available, but otherwise provide
   * schedule position data.
   * 
   * @param tripInstance the trip instance to query
   * @param targetTime the target time (Unix-time)
   * @return the trip position
   */
  public TripLocation getPositionForTripInstance(
      TripInstanceProxy tripInstance, long targetTime);

  /**
   * @param vehicleId the target vehicle id
   * @param time the target time (Unix-time)
   * @return the trip position for the specified vehicle id and time, or null if
   *         not found
   */
  public TripLocation getPositionForVehicleAndTime(AgencyAndId vehicleId,
      long time);
}
