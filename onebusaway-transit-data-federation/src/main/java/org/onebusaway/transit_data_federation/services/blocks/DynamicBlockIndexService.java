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
package org.onebusaway.transit_data_federation.services.blocks;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

import java.util.List;

public interface DynamicBlockIndexService {
  List<BlockStopTimeIndex> getStopTimeIndicesForStop(StopEntry stopEntry);

  void register(BlockInstance blockInstance, int effectiveTime);

  BlockInstance getDynamicBlockInstance(AgencyAndId blockId);

  List<BlockTripIndex> getBlockTripIndicesForRouteCollectionId(AgencyAndId routeCollectionId);

  List<BlockTripIndex> getBlockTripIndicesForBlock(AgencyAndId blockId);
}
