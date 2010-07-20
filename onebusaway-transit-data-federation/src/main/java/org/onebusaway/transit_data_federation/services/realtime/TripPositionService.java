package org.onebusaway.transit_data_federation.services.realtime;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.TripPosition;
import org.onebusaway.transit_data_federation.services.tripplanner.TripInstanceProxy;

/**
 * Service methods for accessing/interpolating the position of a transit vehicle
 * give a trip instance and target time.
 * 
 * @author bdferris
 * 
 */
public interface TripPositionService {

  /**
   * Given a trip instance and a target time, determine the vehicle position at
   * that time. Use real-time data when available, but otherwise provide
   * schedule position data.
   * 
   * @param tripInstance the trip instance to query
   * @param targetTime the target time (Unix-time)
   * @return the trip position
   */
  public TripPosition getPositionForTripInstance(
      TripInstanceProxy tripInstance, long targetTime);

  /**
   * @param vehicleId the target vehicle id
   * @param time the target time (Unix-time)
   * @return the trip position for the specified vehicle id and time, or null if
   *         not found
   */
  public TripPosition getPositionForVehicleAndTime(AgencyAndId vehicleId,
      long time);
}
