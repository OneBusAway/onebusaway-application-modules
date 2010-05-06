package org.onebusaway.transit_data_federation.services.narrative;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;

public interface TripNarrativeService {
  public TripNarrative getTripForId(AgencyAndId tripId);
}
