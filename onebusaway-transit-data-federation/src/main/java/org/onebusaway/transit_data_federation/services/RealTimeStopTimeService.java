package org.onebusaway.transit_data_federation.services;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;

/**
 * Service methods for determining the set of active stop times at a particular
 * stop and time.
 * 
 * @author bdferris
 * @see StopTimeInstance
 */
public interface RealTimeStopTimeService {

  /**
   * Determines the set of active arrivals and departures at a given stop,
   * taking into account real-time arrival information.
   * 
   */
  public List<ArrivalAndDepartureInstance> getArrivalsAndDeparturesForStopInTimeRange(
      StopEntry stop, long currentTime, long fromTime, long toTime);

  public ArrivalAndDepartureInstance getArrivalAndDepartureForStop(
      StopEntry stop, int stopSequence, TripEntry trip, long serviceDate,
      AgencyAndId vehicleId, long time);
}
