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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.realtime.DynamicCache;
import org.onebusaway.transit_data_federation.model.transit_graph.DynamicGraph;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * implementation of the DynamicGraph, the dynamic equivalent to the TransitGraph,
 * but populated via real-time and not the bundle.
 */
@Component
public class DynamicGraphImpl extends DynamicCache implements DynamicGraph {

  private static Logger _log = LoggerFactory.getLogger(DynamicGraphImpl.class);
  private Map<AgencyAndId, BlockEntry> blockEntryById = new HashMap<>();
  private Map<AgencyAndId, TripEntry> tripEntryById = new HashMap<>();
  private Map<AgencyAndId, RouteEntry> routeEntryById = new HashMap<>();

  @Override
  public TripEntry getTripEntryForId(AgencyAndId id) {
    return tripEntryById.get(id);
  }

  @Override
  public void registerTrip(TripEntry tripEntry, long currentTime) {
    if (needsPrune(currentTime)) {
      prune(currentTime);
    }
    if (!tripEntryById.containsKey(tripEntry.getId())) {
      tripEntryById.put(tripEntry.getId(), tripEntry);
    }
  }

  private void prune(long currentTime) {
    long start = System.currentTimeMillis();
    try {
      resetStats(currentTime);
      int effectiveTime = getEffectiveTime(currentTime);
      pruneBlockEntryById(effectiveTime);
      pruneTripEntryById(effectiveTime);
      pruneRouteEntryById(effectiveTime);
    } catch (Throwable t) {
      _log.error("prune exception {}", t, t);
    } finally {
      _log.info("cache prune complete in {}ms", System.currentTimeMillis()-start);
    }
  }

  private void pruneRouteEntryById(int effectiveTime) {
    // routes don't expire
  }

  private void pruneTripEntryById(int effectiveTime) {
    Iterator<Map.Entry<AgencyAndId, TripEntry>> iterator = tripEntryById.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<AgencyAndId, TripEntry> next = iterator.next();
      if (isExpired(next.getValue(), effectiveTime)) {
        iterator.remove();
      }
    }
  }

  private void pruneBlockEntryById(int effectiveTime) {
    Iterator<Map.Entry<AgencyAndId, BlockEntry>> iterator = blockEntryById.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<AgencyAndId, BlockEntry> next = iterator.next();
      if (isExpired(next.getValue(), effectiveTime)) {
        iterator.remove();
      }
    }
  }

  @Override
  public void updateTrip(TripEntry tripEntry) {
    tripEntryById.put(tripEntry.getId(), tripEntry);
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

  @Override
  public void updateBlock(BlockEntry blockEntry) {
    blockEntryById.put(blockEntry.getId(), blockEntry);
  }
}
