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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.narrative.AgencyNarrative;
import org.onebusaway.transit_data_federation.model.narrative.RouteCollectionNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NarrativeServiceImpl implements NarrativeService {

  private static Logger _log = LoggerFactory.getLogger(NarrativeServiceImpl.class);
  private Map<AgencyAndId, TripNarrative> _dynamicTripCache = new HashMap<>();

  private NarrativeProviderImpl _provider;

  private FederatedTransitDataBundle _bundle;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  public void setStopTimeNarrativeProvider(NarrativeProviderImpl provider) {
    _provider = provider;
  }

  @PostConstruct
  @Refreshable(dependsOn = RefreshableResources.NARRATIVE_DATA)
  public void setup() throws IOException, ClassNotFoundException {
//    _dynamicTripCache.clear();
    File path = _bundle.getNarrativeProviderPath();
    if (path.exists()) {
      _provider = ObjectSerializationLibrary.readObject(path);
    } else {
      _provider = new NarrativeProviderImpl();
    }
  }

  /****
   * {@link NarrativeService} Interface
   ****/

  @Override
  public AgencyNarrative getAgencyForId(String agencyId) {
    return _provider.getNarrativeForAgencyId(agencyId);
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
  public RouteCollectionNarrative getRouteCollectionForId(
      AgencyAndId routeCollectionId) {
    return _provider.getRouteCollectionNarrativeForId(routeCollectionId);
  }

  @Override
  public TripNarrative getTripForId(AgencyAndId tripId) {
    TripNarrative tn = _provider.getNarrativeForTripId(tripId);
    if (tn == null)
      tn = _dynamicTripCache.get(tripId);
    return tn;
  }
  
  @Override
  public ShapePoints getShapePointsForId(AgencyAndId id) {
    return _provider.getShapePointsForId(id);
  }

  @Override
  public void addDynamicTrip(BlockTripIndex blockTripIndex) {
    addDynamicTrip(blockTripIndex, false);
  }

  @Override
  public void updateDynamicTrip(BlockTripIndex blockTripIndex) {
    addDynamicTrip(blockTripIndex, true);
  }

  private void addDynamicTrip(BlockTripIndex blockTripIndex, boolean forceUpdate) {
    BlockTripEntry blockTripEntry = blockTripIndex.getTrips().get(0);
    TripEntry trip = blockTripEntry.getTrip();
    AgencyAndId tripId = trip.getId();

    if (_dynamicTripCache.containsKey(tripId) && !forceUpdate) {
      return; // nothing to do
    }
    TripNarrative.Builder builder = TripNarrative.builder();

    RouteCollectionNarrative routeNarrative = getRouteCollectionForId(trip.getRoute().getId());
    List<StopTimeEntry> stopTimes = trip.getStopTimes();
    int lastStopIndex = stopTimes.size() - 1;
    AgencyAndId stopId = stopTimes.get(lastStopIndex).getStop().getId();
    StopNarrative stopForId = getStopForId(stopId);
    if (stopForId == null) {
      _log.error("no such stop {}", stopId);
      return;
    }

    builder.setTripHeadsign(stopForId.getName());
    builder.setRouteShortName(routeNarrative.getShortName());
    _dynamicTripCache.put(tripId, builder.create());
  }

  @Override
  public List<StopTimeNarrative> getStopTimeNarrativesForPattern(AgencyAndId routeId, String directionId, List<AgencyAndId> stopIds) {
    return _provider.getStopTimeNarrativesForPattern(routeId, directionId, stopIds);
  }

  @Override
  public StopTimeNarrative getStopTimeNarrativeForPattern(AgencyAndId routeId, AgencyAndId stopId, String directionId) {
    return _provider.getStopTimeNarrativeForPattern(routeId, stopId, directionId);
  }

  @Override
  public void addShapePoints(ShapePoints shapePoints) {
    _provider.addShapePoints(shapePoints);
  }

  @Override
  public List<AgencyAndId> getStaticRoutes(AgencyAndId stopId) {
    return _provider.getStaticRoutes(stopId);
  }
}
