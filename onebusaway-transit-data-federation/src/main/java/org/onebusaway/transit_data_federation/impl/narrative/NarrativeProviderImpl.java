/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.impl.narrative;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.narrative.AgencyNarrative;
import org.onebusaway.transit_data_federation.model.narrative.RouteCollectionNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

public final class NarrativeProviderImpl implements Serializable {

  private static final long serialVersionUID = 2L;

  private Map<String, AgencyNarrative> _agencyNarratives = new HashMap<String, AgencyNarrative>();

  private Map<AgencyAndId, StopNarrative> _stopNarratives = new HashMap<AgencyAndId, StopNarrative>();

  private Map<AgencyAndId, RouteCollectionNarrative> _routeCollectionNarratives = new HashMap<AgencyAndId, RouteCollectionNarrative>();

  private Map<AgencyAndId, TripNarrative> _tripNarratives = new HashMap<AgencyAndId, TripNarrative>();

  private Map<AgencyAndId, List<StopTimeNarrative>> _stopTimeNarrativesByTripIdAndStopTimeSequence = new HashMap<AgencyAndId, List<StopTimeNarrative>>();

  private Map<AgencyAndId, ShapePoints> _shapePointsById = new HashMap<AgencyAndId, ShapePoints>();

  public void setNarrativeForAgency(String agencyId, AgencyNarrative narrative) {
    _agencyNarratives.put(agencyId, narrative);
  }

  public void setNarrativeForStop(AgencyAndId stopId, StopNarrative narrative) {
    _stopNarratives.put(stopId, narrative);
  }

  public void setNarrativeForRouteCollectionId(AgencyAndId id,
      RouteCollectionNarrative narrative) {
    _routeCollectionNarratives.put(id, narrative);
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

  public void setShapePointsForId(AgencyAndId shapeId, ShapePoints shapePoints) {
    _shapePointsById.put(shapeId, shapePoints);
  }
  
  public AgencyNarrative getNarrativeForAgencyId(String agencyId) {
    return _agencyNarratives.get(agencyId);
  }
  
  public RouteCollectionNarrative getNarrativeForRouteCollectionId(AgencyAndId routeCollectionId) {
    return _routeCollectionNarratives.get(routeCollectionId);
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

  public RouteCollectionNarrative getRouteCollectionNarrativeForId(
      AgencyAndId routeCollectionId) {
    return _routeCollectionNarratives.get(routeCollectionId);
  }

  public TripNarrative getNarrativeForTripId(AgencyAndId tripId) {
    return _tripNarratives.get(tripId);
  }

  public ShapePoints getShapePointsForId(AgencyAndId id) {
    return _shapePointsById.get(id);
  }
}
