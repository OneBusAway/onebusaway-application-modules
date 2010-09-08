package org.onebusaway.transit_data_federation.impl.realtime;

import java.util.List;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;

public interface StopTimeBlockService {

  /**
   * Given a trip and a target schedule time, we determine a list of
   * {@link StopTimeEntry} entries that surrounds the specified time. Note that
   * the in the case of a block of trips, the stop times could belong to a a
   * trip different than the one specified in the 'trip' argument, or the stop
   * times could span multiple trips if the target time falls between two trips
   * on the same block. Note that if the requested scheduleTime is outside the
   * range of the entire block or if the block has no stop times, then we will
   * return an empty set.
   * 
   * @param trip
   * @param scheduleTime
   * @return
   */
  public List<StopTimeEntry> getSurroundingStopTimes(TripEntry trip,
      int scheduleTime);

  /**
   * Given a trip and a target distance along block, we determine a list of
   * {@link StopTimeEntry} entries that surrounds the specified distance. Note
   * that the in the case of a block of trips, the stop times could belong to a
   * a trip different than the one specified in the 'trip' argument, or the stop
   * times could span multiple trips if the target distance falls between two
   * trips on the same block. Note that if the requested distance is outside the
   * range of the entire block or if the block has no stop times, then we will
   * return an empty set.
   * 
   * @param trip
   * @param scheduleTime
   * @return
   */
  public List<StopTimeEntry> getSurroundingStopTimesFromDistanceAlongBlock(
      TripEntry trip, double distanceAlongBlock);
}