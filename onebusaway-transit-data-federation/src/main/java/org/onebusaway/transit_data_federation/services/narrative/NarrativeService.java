package org.onebusaway.transit_data_federation.services.narrative;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data_federation.model.narrative.AgencyNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;

/**
 * Service methods supporting narrative querying. We define "narrative" as any
 * information that isn't used in raw schedule, trip planning, and routing
 * computations, but instead is simply there to provide human-readable labels to
 * results. For example, a stop's id is a raw data attribute that will be used
 * in method queries and data structures, but the stop name is a human-readable
 * element that typically isn't needed until constructing a result to display to
 * the user.
 * 
 * The narrative service has methods for querying narrative objects for various
 * low-level objects, such as {@link Agency}, {@link Stop}, {@link Trip}, and
 * {@link StopTime}.
 * 
 * @author bdferris
 * @see AgencyNarrative
 * @see StopNarrative
 * @see TripNarrative
 * @see StopTimeNarrative
 */
public interface NarrativeService {
  
  public AgencyNarrative getAgencyForId(String agencyId);

  public StopNarrative getStopForId(AgencyAndId stopId);

  public TripNarrative getTripForId(AgencyAndId tripId);

  public StopTimeNarrative getStopTimeForEntry(StopTimeEntry entry);
}
