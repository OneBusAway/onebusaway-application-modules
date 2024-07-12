/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.realtime.DynamicHelper;
import org.onebusaway.transit_data_federation.services.blocks.*;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
/**
 * refactored to be a wrapper of both static and dynamic services. The class
 * that this used to be is now StaticBlockIndexServiceImpl
 */
public class BlockIndexServiceImpl implements BlockIndexService {

  @Qualifier("staticBlockIndexServiceImpl")
  @Autowired
  StaticBlockIndexService staticBlockIndexService;
  @Qualifier("dynamicBlockIndexServiceImpl")
  @Autowired
  DynamicBlockIndexService dynamicBlockIndexService;

  private DynamicHelper helper = new DynamicHelper();

  public void setStaticBlockIndexService(StaticBlockIndexService staticBlockIndexService) {
    this.staticBlockIndexService = staticBlockIndexService;
  }
  public void setDynamicBlockIndexService(DynamicBlockIndexService dynamicBlockIndexService) {
    this.dynamicBlockIndexService = dynamicBlockIndexService;
  }

  @Override
  public List<BlockTripIndex> getBlockTripIndices() {
    return staticBlockIndexService.getBlockTripIndices();
  }

  @Override
  public List<BlockTripIndex> getBlockTripIndicesForAgencyId(String agencyId) {
    return staticBlockIndexService.getBlockTripIndicesForAgencyId(agencyId);
  }

  @Override
  public List<BlockTripIndex> getBlockTripIndicesForRouteCollectionId(AgencyAndId routeCollectionId) {
    List<BlockTripIndex> list = new ArrayList<>();
    List<BlockTripIndex> indices = staticBlockIndexService.getBlockTripIndicesForRouteCollectionId(routeCollectionId);
    if (indices != null) {
      list.addAll(indices);
    }
    if (dynamicBlockIndexService != null) {
      indices = dynamicBlockIndexService.getBlockTripIndicesForRouteCollectionId(routeCollectionId);
      if (indices != null) {
        list.addAll(indices);
      }
    }
    return list;
  }

  @Override
  public List<BlockTripIndex> getBlockTripIndicesForBlock(AgencyAndId blockId) {
    List<BlockTripIndex> indices = new ArrayList<>();
    List<BlockTripIndex> staticIndices = staticBlockIndexService.getBlockTripIndicesForBlock(blockId);
    if (staticIndices != null) {
      indices.addAll(staticIndices);
    }
    if (dynamicBlockIndexService != null) {
      List<BlockTripIndex> dynamicIndices = dynamicBlockIndexService.getBlockTripIndicesForBlock(blockId);
      if (dynamicIndices != null) {
        indices.addAll(dynamicIndices);
      }
    }
    return indices;
  }

  @Override
  public List<BlockStopTimeIndex> getStopTimeIndicesForStop(StopEntry stopEntry) {
    List<BlockStopTimeIndex> list = new ArrayList<>();
    List<BlockStopTimeIndex> staticIndices = staticBlockIndexService.getStopTimeIndicesForStop(stopEntry);
    if (staticIndices != null)
      list.addAll(staticIndices);
    if (dynamicBlockIndexService != null) {
      List<BlockStopTimeIndex> dynamicIndicies = dynamicBlockIndexService.getStopTimeIndicesForStop(stopEntry);
      if (dynamicIndicies != null)
        list.addAll(dynamicIndicies);
    }
    return list;
  }

  @Override
  public void register(BlockInstance blockInstance, long currentTime) {
    dynamicBlockIndexService.register(blockInstance, currentTime);
  }

  @Override
  public BlockInstance getDynamicBlockInstance(AgencyAndId blockId) {
   return dynamicBlockIndexService.getDynamicBlockInstance(blockId);
  }

  @Override
  public List<BlockStopSequenceIndex> getStopSequenceIndicesForStop(StopEntry stopEntry) {
    return staticBlockIndexService.getStopSequenceIndicesForStop(stopEntry);
  }

  @Override
  public List<BlockSequenceIndex> getAllBlockSequenceIndices() {
    return staticBlockIndexService.getAllBlockSequenceIndices();
  }

  @Override
  public List<BlockLayoverIndex> getBlockLayoverIndices() {
    return staticBlockIndexService.getBlockLayoverIndices();
  }

  @Override
  public List<BlockLayoverIndex> getBlockLayoverIndicesForAgencyId(String agencyId) {
    return staticBlockIndexService.getBlockLayoverIndicesForAgencyId(agencyId);
  }

  @Override
  public List<BlockLayoverIndex> getBlockLayoverIndicesForRouteCollectionId(AgencyAndId rotueCollectionId) {
    return staticBlockIndexService.getBlockLayoverIndicesForRouteCollectionId(rotueCollectionId);
  }

  @Override
  public List<BlockLayoverIndex> getBlockLayoverIndicesForBlock(AgencyAndId blockId) {
    return staticBlockIndexService.getBlockLayoverIndicesForBlock(blockId);
  }

  @Override
  public List<FrequencyBlockTripIndex> getFrequencyBlockTripIndices() {
    return staticBlockIndexService.getFrequencyBlockTripIndices();
  }

  @Override
  public List<FrequencyBlockTripIndex> getFrequencyBlockTripIndicesForAgencyId(String agencyId) {
    return staticBlockIndexService.getFrequencyBlockTripIndicesForAgencyId(agencyId);
  }

  @Override
  public List<FrequencyBlockTripIndex> getFrequencyBlockTripIndicesForRouteCollectionId(AgencyAndId routeCollectionId) {
    return staticBlockIndexService.getFrequencyBlockTripIndicesForRouteCollectionId(routeCollectionId);
  }

  @Override
  public List<FrequencyBlockTripIndex> getFrequencyBlockTripIndicesForBlock(AgencyAndId blockId) {
    return staticBlockIndexService.getFrequencyBlockTripIndicesForBlock(blockId);
  }

  @Override
  public List<FrequencyBlockStopTimeIndex> getFrequencyStopTimeIndicesForStop(StopEntry stopEntry) {
    return staticBlockIndexService.getFrequencyStopTimeIndicesForStop(stopEntry);
  }

  @Override
  public List<FrequencyStopTripIndex> getFrequencyStopTripIndicesForStop(StopEntry stop) {
    return staticBlockIndexService.getFrequencyStopTripIndicesForStop(stop);
  }
  @Override
  public boolean isDynamicTrip(TripEntry trip) {
    return helper.isServiceIdDynamic(trip.getServiceId().toString());
  }
  public boolean isDynamicBlock(BlockEntry block) {
    return helper.isBlockDynamic(block);
  }
}
