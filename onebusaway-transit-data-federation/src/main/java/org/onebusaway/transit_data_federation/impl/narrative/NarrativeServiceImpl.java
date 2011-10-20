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
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NarrativeServiceImpl implements NarrativeService {

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
    return _provider.getNarrativeForTripId(tripId);
  }
  
  @Override
  public ShapePoints getShapePointsForId(AgencyAndId id) {
    return _provider.getShapePointsForId(id);
  }
}
