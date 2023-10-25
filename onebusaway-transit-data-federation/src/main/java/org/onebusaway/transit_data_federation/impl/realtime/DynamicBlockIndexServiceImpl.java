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
package org.onebusaway.transit_data_federation.impl.realtime;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.blocks.BlockIndexFactoryServiceImpl;
import org.onebusaway.transit_data_federation.impl.blocks.BlockStopTimeIndicesFactory;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.transit_graph.DynamicGraph;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.DynamicBlockIndexService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
/**
 * Counterpart to BlockLocationService, handling dynamic trips
 * ( ADDED / DUPLICATED )
 */
public class DynamicBlockIndexServiceImpl implements DynamicBlockIndexService {

  private static Logger _log = LoggerFactory.getLogger(DynamicBlockIndexServiceImpl.class);
  private static final int CACHE_TIMEOUT = 18 * 60 * 60 * 1000; // 18 hours

  @Autowired
  private BlockIndexFactoryServiceImpl blockIndexFactoryService;
  private NarrativeService _narrativeService;

  private DynamicGraph _dynamicGraph;

  private Map<AgencyAndId, List<BlockTripIndex>> blockTripIndexByRouteCollectionId = new PassiveExpiringMap<>(CACHE_TIMEOUT);
  private BlockStopTimeIndicesFactory blockStopTimeIndicesFactory = new BlockStopTimeIndicesFactory();

  private Map<AgencyAndId, BlockInstance> cacheByBlockId = new PassiveExpiringMap<>(CACHE_TIMEOUT);

  private Map<AgencyAndId, Set<BlockStopTimeIndex>> blockStopTimeIndicesByStopId = new PassiveExpiringMap<>(CACHE_TIMEOUT);

  private Map<AgencyAndId, List<BlockTripIndex>> blockTripByBlockId = new PassiveExpiringMap<>(CACHE_TIMEOUT);
  @Autowired
  public void setNarrativeService(NarrativeService narrativeService) {
    _narrativeService = narrativeService;
  }

  @Autowired
  public void setDynamicGraph(DynamicGraph dynamicGraph) {
    _dynamicGraph = dynamicGraph;
  }

  @Refreshable(dependsOn = RefreshableResources.TRANSIT_GRAPH)
  public void reset() {
    // we don't clear these here as they transcend the bundle swap (they aren't dependent on bundle data)
//    blockTripIndexByRouteCollectionId.clear();
//    cacheByBlockId.clear();
//    blockStopTimeIndicesByStopId.clear();
//    blockTripByBlockId.clear();
  }
  @Override
  public List<BlockStopTimeIndex> getStopTimeIndicesForStop(StopEntry stopEntry) {
    synchronized (blockStopTimeIndicesByStopId) {
      if (!blockStopTimeIndicesByStopId.containsKey(stopEntry.getId())) {
        return null;
      }
      Set<BlockStopTimeIndex> set = blockStopTimeIndicesByStopId.get(stopEntry.getId());
      return new ArrayList<>(set);
    }
  }

  @Override
  public void register(BlockInstance blockInstance, int effectiveTime) {
    // if the vehicle changes trips, we rely on the cache record to expire
    // therefore there may be a brief period of overlap
    AgencyAndId id = blockInstance.getBlock().getBlock().getId();
    if (cacheByBlockId.containsKey(id)) {
      if (isCached(id)) {
        return; // nothing to do
      }
    }
    cacheByBlockId.put(id, blockInstance);

    List<BlockEntry> blocks = new ArrayList<>();
    blocks.add(blockInstance.getBlock().getBlock());
    _dynamicGraph.registerBlock(blockInstance.getBlock().getBlock());
    List<BlockTripIndex> blockTripIndexList = blockIndexFactoryService.createTripIndices(blocks);
    for (BlockTripIndex blockTripIndex : blockTripIndexList) {
      if (!blockTripByBlockId.containsKey(id)) {
        blockTripByBlockId.put(id, new ArrayList<>());
      }
      blockTripByBlockId.get(id).add(blockTripIndex);
      TripEntry trip = blockTripIndex.getTrips().get(0).getTrip();
      _dynamicGraph.registerTrip(trip);
      RouteEntry route = trip.getRoute();
      _dynamicGraph.registerRoute(route);
      if (!blockTripIndexByRouteCollectionId.containsKey(route.getId())) {
        blockTripIndexByRouteCollectionId.put(route.getId(), new ArrayList<>());
      }
      blockTripIndexByRouteCollectionId.get(route.getId()).add(blockTripIndex);
      _narrativeService.addDynamicTrip(blockTripIndex);

      List<AgencyAndId> stopIds = new ArrayList<>();
      for (BlockStopTimeEntry blockStopTimeEntry : blockTripIndex.getTrips().get(0).getStopTimes()) {
          stopIds.add(blockStopTimeEntry.getStopTime().getStop().getId());
      }
    }


    List<BlockStopTimeIndex> indices = blockStopTimeIndicesFactory.createIndices(blocks);
    synchronized (blockStopTimeIndicesByStopId) {
      for (BlockStopTimeIndex sti : indices) {
        AgencyAndId stopId = sti.getStop().getId();
        if (!blockStopTimeIndicesByStopId.containsKey(stopId)) {
          // a set to prevent duplicates
          blockStopTimeIndicesByStopId.put(stopId, new HashSet<>());
        }
        if (!containsTrip(blockStopTimeIndicesByStopId.get(stopId), sti)) {
          blockStopTimeIndicesByStopId.get(stopId).add(sti);
        }
      }
    }
  }

  /**
   * as caches expire, validate the expected entries are present.
   * @param id
   * @return
   */
  private boolean isCached(AgencyAndId id) {
    // make sure the info we have matches indicies
    BlockEntry testBlock = _dynamicGraph.getBlockEntryForId(id);
    if (testBlock == null) {
      _log.debug("lost block {}", id);
      return false;
    }
    List<BlockTripIndex> blockTripIndices = blockTripByBlockId.get(id);
    if (blockTripIndices == null || blockTripIndices.isEmpty()) {
      _log.debug("lost blockTripIndices {}", id);
      return false;
    }

    TripEntry tripEntryForId = _dynamicGraph.getTripEntryForId(id);
    if (tripEntryForId == null) {
      _log.debug("lost trip {}", id);
      return false;
    }
    RouteEntry routEntryForId = _dynamicGraph.getRoutEntryForId(tripEntryForId.getRoute().getId());
    if (routEntryForId == null) {
      _log.debug("lost route {}", id);
      return false;
    }

    List<BlockTripIndex> list = blockTripIndexByRouteCollectionId.get(routEntryForId.getId());
    if (list == null || list.isEmpty()) {
      _log.debug("missing blockTripIndex {}", routEntryForId.getId());
      return false;
    }
    return true;
  }

  private boolean containsTrip(Set<BlockStopTimeIndex> blockStopTimeIndices, BlockStopTimeIndex sti) {
    for (BlockStopTimeIndex blockStopTimeIndex : blockStopTimeIndices) {
      if (sti.getTrips().get(0).getTrip().getId().equals(blockStopTimeIndex.getTrips().get(0).getTrip().getId())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public BlockInstance getDynamicBlockInstance(AgencyAndId blockId) {
    return cacheByBlockId.get(blockId);
  }

  @Override
  public List<BlockTripIndex> getBlockTripIndicesForRouteCollectionId(AgencyAndId routeCollectionId) {
    return blockTripIndexByRouteCollectionId.get(routeCollectionId);
  }

  @Override
  public List<BlockTripIndex> getBlockTripIndicesForBlock(AgencyAndId blockId) {
    return blockTripByBlockId.get(blockId);
  }
}
