package org.onebusaway.transit_data_federation.services;

import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data_federation.model.StopSequence;

/**
 * Service methods for generating {@link StopSequence} objects from a collection
 * of {@link Trip} objects and each trip's {@link StopTime} objects. Recall that
 * a stop sequence is a unique sequence of stops visited by a transit trip. So
 * typically, multiple trips will often refer to the same stop sequence, but not
 * always. Here we find all unique sequences for the set of trips.
 * 
 * @author bdferris
 * @see StopSequence
 */
public interface StopSequencesService {

  /**
   * Recall that a stop sequence is a unique sequence of stops visited by a
   * transit trip. So typically, multiple trips will often refer to the same
   * stop sequence, but not always. Here we find all unique sequences for the
   * set of trips.
   * 
   * @param stopTimesByTrip set of trips along with the stop times for each trip
   * @return the set of unique stop sequences visited by the trips
   */
  public List<StopSequence> getStopSequencesForTrips(
      Map<Trip, List<StopTime>> stopTimesByTrip);

}