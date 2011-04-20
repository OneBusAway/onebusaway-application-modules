package org.onebusaway.transit_data_federation.services;

import java.util.Date;
import java.util.List;

import org.onebusaway.collections.Range;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;

/**
 * Service methods for determining the set of active stop times at a particular
 * stop and time.
 * 
 * @author bdferris
 * @see StopTimeInstance
 */
public interface StopTimeService {

  /**
   * Determines the set of active stop time instances at a given stop, taking
   * into account information like active service dates, etc
   * 
   * @param stopId the starget stop id
   * @param from
   * @param to
   * @return the set of active stop time instances in the specified time range
   */
  public List<StopTimeInstance> getStopTimeInstancesInTimeRange(
      AgencyAndId stopId, Date from, Date to);

  public List<StopTimeInstance> getStopTimeInstancesInTimeRange(
      StopEntry stopEntry, Date from, Date to);

  public List<StopTimeInstance> getNextScheduledBlockTripDeparturesForStop(
      StopEntry stopEntry, long time);

  public Range getDepartureForStopAndServiceDate(AgencyAndId stopId,
      ServiceDate serviceDate);

  public StopTimeInstance getNextStopTimeInstance(StopTimeInstance instance);

  public List<Pair<StopTimeInstance>> getDepartureSegmentsInRange(
      StopEntry fromStop, StopEntry toStop, Date fromDepartureTime,
      Date toDepartureTime);
}
