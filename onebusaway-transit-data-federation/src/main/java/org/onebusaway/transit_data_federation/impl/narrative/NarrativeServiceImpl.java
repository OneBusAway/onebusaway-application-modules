package org.onebusaway.transit_data_federation.impl.narrative;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.narrative.AgencyNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NarrativeServiceImpl implements NarrativeService {

  private NarrativeProviderImpl _provider;

  @Override
  public AgencyNarrative getAgencyForId(String agencyId) {
    return _provider.getNarrativeForAgencyId(agencyId);
  }

  @Autowired
  public void setStopTimeNarrativeProvider(NarrativeProviderImpl provider) {
    _provider = provider;
  }

  @Override
  public StopNarrative getStopForId(AgencyAndId stopId) {
    return _provider.getNarrativeForStopId(stopId);
  }

  @Override
  public StopTimeNarrative getStopTimeForEntry(StopTimeEntry entry) {
    return _provider.getNarrativeForStopTimeEntry(entry);
  }

  @Override
  public TripNarrative getTripForId(AgencyAndId tripId) {
    return _provider.getNarrativeForTripId(tripId);
  }
}
