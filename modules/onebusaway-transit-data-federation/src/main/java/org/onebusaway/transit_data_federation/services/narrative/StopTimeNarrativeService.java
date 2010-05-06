package org.onebusaway.transit_data_federation.services.narrative;

import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;

public interface StopTimeNarrativeService {
  public StopTimeNarrative getStopTimeForEntry(StopTimeEntry entry);
}
