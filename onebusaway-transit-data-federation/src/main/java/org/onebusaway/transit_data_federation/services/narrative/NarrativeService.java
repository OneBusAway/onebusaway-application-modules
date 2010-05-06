package org.onebusaway.transit_data_federation.services.narrative;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.narrative.AgencyNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;

public interface NarrativeService {
  public AgencyNarrative getAgencyForId(String agencyId);
  public StopNarrative getStopForId(AgencyAndId stopId);
  public TripNarrative getTripForId(AgencyAndId tripId);
  public StopTimeNarrative getStopTimeForEntry(StopTimeEntry entry);
}
