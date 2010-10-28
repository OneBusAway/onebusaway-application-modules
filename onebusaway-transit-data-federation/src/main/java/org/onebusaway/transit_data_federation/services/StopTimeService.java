package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;

import java.util.Date;
import java.util.List;

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

  public List<StopTimeInstance> getStopTimeInstancesInRange(Date from,
      Date to, StopEntry stopEntry);
}
