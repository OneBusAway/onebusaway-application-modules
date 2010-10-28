package org.onebusaway.transit_data_federation.impl.narrative;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.narrative.AgencyNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

public final class NarrativeProviderImpl implements Serializable {

  private static final long serialVersionUID = 1L;

  private Map<String, AgencyNarrative> _agencyNarratives = new HashMap<String, AgencyNarrative>();

  private Map<AgencyAndId, StopNarrative> _stopNarratives = new HashMap<AgencyAndId, StopNarrative>();

  private Map<AgencyAndId, TripNarrative> _tripNarratives = new HashMap<AgencyAndId, TripNarrative>();

  private Map<AgencyAndId, List<StopTimeNarrative>> _stopTimeNarrativesByTripIdAndStopTimeSequence = new HashMap<AgencyAndId, List<StopTimeNarrative>>();

  public void setNarrativeForAgency(String agencyId, AgencyNarrative narrative) {
    _agencyNarratives.put(agencyId, narrative);
  }

  public void setNarrativeForStop(AgencyAndId stopId, StopNarrative narrative) {
    _stopNarratives.put(stopId, narrative);
  }

  public void setNarrativeForTripId(AgencyAndId tripId, TripNarrative narrative) {
    _tripNarratives.put(tripId, narrative);
  }

  public void setNarrativeForStopTimeEntry(AgencyAndId tripId, int index,
      StopTimeNarrative narrative) {

    List<StopTimeNarrative> narratives = _stopTimeNarrativesByTripIdAndStopTimeSequence.get(tripId);
    if (narratives == null) {
      narratives = new ArrayList<StopTimeNarrative>();
      _stopTimeNarrativesByTripIdAndStopTimeSequence.put(tripId, narratives);
    }

    while (narratives.size() <= index)
      narratives.add(null);
    narratives.set(index, narrative);
  }
  
  public AgencyNarrative getNarrativeForAgencyId(String agencyId) {
    return _agencyNarratives.get(agencyId);
  }

  public StopNarrative getNarrativeForStopId(AgencyAndId stopId) {
    return _stopNarratives.get(stopId);
  }

  public StopTimeNarrative getNarrativeForStopTimeEntry(StopTimeEntry entry) {
    TripEntry trip = entry.getTrip();
    List<StopTimeNarrative> narratives = _stopTimeNarrativesByTripIdAndStopTimeSequence.get(trip.getId());
    if (narratives == null)
      return null;
    int index = entry.getSequence();
    return narratives.get(index);
  }

  public TripNarrative getNarrativeForTripId(AgencyAndId tripId) {
    return _tripNarratives.get(tripId);
  }

}
