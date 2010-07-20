package org.onebusaway.transit_data_federation.services;

import org.onebusaway.transit_data_federation.model.TripPosition;
import org.onebusaway.transit_data_federation.model.predictions.TripTimePrediction;
import org.onebusaway.transit_data_federation.services.tripplanner.TripInstanceProxy;

/**
 * Service methods for accessing the position of a transit vehicle give a trip
 * instance and target time.
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
   * @param targetTime the target time
   * @return the trip position
   */
  public TripPosition getPositionForTripInstance(TripInstanceProxy tripInstance,
      long targetTime);

  /**
   * 
   * @param tripInstance
   * @param prediction
   * @param targetTime
   * @return
   */
  public TripPosition getPositionForTripInstance(TripInstanceProxy tripInstance,
      TripTimePrediction prediction, long targetTime);
}
