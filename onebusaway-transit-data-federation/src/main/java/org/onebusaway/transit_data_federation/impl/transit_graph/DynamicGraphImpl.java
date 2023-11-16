/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl.transit_graph;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.model.transit_graph.DynamicGraph;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * implementation of the DynamicGraph, the dynamic equivalent to the TransitGraph,
 * but populated via real-time and not the bundle.
 */
@Component
public class DynamicGraphImpl implements DynamicGraph {

  private static final int CACHE_TIMEOUT = 18 * 60 * 60 * 1000; // 18 hours
  private Map<AgencyAndId, BlockEntry> blockEntryById = new PassiveExpiringMap<>(CACHE_TIMEOUT);
  private Map<AgencyAndId, TripEntry> tripEntryById = new PassiveExpiringMap<>(CACHE_TIMEOUT);
  private Map<AgencyAndId, RouteEntry> routeEntryById = new PassiveExpiringMap<>(CACHE_TIMEOUT);

  @Override
  public TripEntry getTripEntryForId(AgencyAndId id) {
    return tripEntryById.get(id);
  }

  @Refreshable(dependsOn = RefreshableResources.TRANSIT_GRAPH)
  public void reset() {
    // these maps expire passively, this data is not dependent upon graph reloading
//    blockEntryById.clear();
//    tripEntryById.clear();
//    routeEntryById.clear();
  }
  @Override
  public void registerTrip(TripEntry tripEntry) {
    if (!tripEntryById.containsKey(tripEntry.getId())) {
      tripEntryById.put(tripEntry.getId(), tripEntry);
    }
  }

  @Override
  public RouteEntry getRoutEntryForId(AgencyAndId id) {
    return routeEntryById.get(id);
  }

  @Override
  public void registerRoute(RouteEntry routeEntry) {
    if (!routeEntryById.containsKey(routeEntry.getId())) {
      routeEntryById.put(routeEntry.getId(), routeEntry);
    }
  }

  @Override
  public BlockEntry getBlockEntryForId(AgencyAndId id) {
    return blockEntryById.get(id);
  }

  @Override
  public void registerBlock(BlockEntry blockEntry) {
    if (!blockEntryById.containsKey(blockEntry.getId())) {
      blockEntryById.put(blockEntry.getId(), blockEntry);
    }
  }
}
