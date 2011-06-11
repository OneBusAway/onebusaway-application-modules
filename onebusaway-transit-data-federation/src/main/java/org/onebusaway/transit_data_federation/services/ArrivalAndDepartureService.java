package org.onebusaway.transit_data_federation.services;

import java.util.List;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;

/**
 * Service methods for determining the set of active stop times at a particular
 * stop and time.
 * 
 * @author bdferris
 * @see StopTimeInstance
 */
public interface ArrivalAndDepartureService {

  /**
   * Determines the set of active arrivals and departures at a given stop,
   * taking into account real-time arrival information.
   * 
   */
  public List<ArrivalAndDepartureInstance> getArrivalsAndDeparturesForStopInTimeRange(
      StopEntry stop, TargetTime targetTime, long fromTime, long toTime);

  /**
   * Determines the set of active arrivals and departures at a given stop, NOT
   * taking into account real-time arrival information.
   * 
   */
  public List<ArrivalAndDepartureInstance> getScheduledArrivalsAndDeparturesForStopInTimeRange(
      StopEntry stop, long currentTime, long fromTime, long toTime);

  /**
   * 
   * @param stop
   * @param time
   * @param includePrivateService TODO
   * @return
   */
  public List<ArrivalAndDepartureInstance> getNextScheduledBlockTripDeparturesForStop(
      StopEntry stop, long time, boolean includePrivateService);

  public ArrivalAndDepartureInstance getArrivalAndDepartureForStop(
      ArrivalAndDepartureQuery query);

  /**
   * Given an arrival and departure instance, compute the arrival and departure
   * instance for the previous stop along the block. If at the start of the
   * block, this method will return null.
   * 
   * @param instance
   * @return
   */
  public ArrivalAndDepartureInstance getPreviousStopArrivalAndDeparture(
      ArrivalAndDepartureInstance instance);

  /**
   * Given an arrival and departure instance, compute the arrival and departure
   * instance for the next stop along the block. If at the end of the block,
   * this method will return null.
   * 
   * @param instance
   * @return
   */
  public ArrivalAndDepartureInstance getNextStopArrivalAndDeparture(
      ArrivalAndDepartureInstance instance);

  /**
   * Given an arrival and departure instance, compute the arrival and departure
   * instance for the next stop along the block. If at the end of the block,
   * this method will return null.
   * 
   * @param instance
   * @return
   */
  public ArrivalAndDepartureInstance getNextTransferStopArrivalAndDeparture(
      ArrivalAndDepartureInstance instance);

  /**
   * 
   * @param fromStop
   * @param toStop
   * @param targetTime
   * @param query query parameters
   * @return
   */
  public List<Pair<ArrivalAndDepartureInstance>> getNextDeparturesForStopPair(
      StopEntry fromStop, StopEntry toStop, TargetTime targetTime,
      ArrivalAndDeparturePairQuery query);

  /**
   * 
   * @param fromStop
   * @param toStop
   * @param targetTime
   * @param query query parameters
   * @return
   */
  public List<Pair<ArrivalAndDepartureInstance>> getPreviousArrivalsForStopPair(
      StopEntry fromStop, StopEntry toStop, TargetTime targetTime,
      ArrivalAndDeparturePairQuery query);
}
