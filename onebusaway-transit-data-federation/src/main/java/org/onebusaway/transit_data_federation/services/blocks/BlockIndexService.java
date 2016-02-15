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
package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

/**
 * The {@link BlockIndexService}
 * @author bdferris
 *
 */
public interface BlockIndexService {

  public List<BlockTripIndex> getBlockTripIndices();

  public List<BlockTripIndex> getBlockTripIndicesForAgencyId(String agencyId);

  public List<BlockTripIndex> getBlockTripIndicesForRouteCollectionId(
      AgencyAndId routeCollectionId);

  public List<BlockTripIndex> getBlockTripIndicesForBlock(AgencyAndId blockId);

  public List<BlockStopTimeIndex> getStopTimeIndicesForStop(StopEntry stopEntry);

  public List<BlockStopSequenceIndex> getStopSequenceIndicesForStop(
      StopEntry stopEntry);

  public List<BlockSequenceIndex> getAllBlockSequenceIndices();


  /****
   * Layover Indices
   ****/
  
  public List<BlockLayoverIndex> getBlockLayoverIndices();

  public List<BlockLayoverIndex> getBlockLayoverIndicesForAgencyId(
      String agencyId);

  public List<BlockLayoverIndex> getBlockLayoverIndicesForRouteCollectionId(
      AgencyAndId rotueCollectionId);

  public List<BlockLayoverIndex> getBlockLayoverIndicesForBlock(
      AgencyAndId blockId);

  /****
   * Frequency Indices
   ****/

  public List<FrequencyBlockTripIndex> getFrequencyBlockTripIndices();

  public List<FrequencyBlockTripIndex> getFrequencyBlockTripIndicesForAgencyId(
      String agencyId);

  public List<FrequencyBlockTripIndex> getFrequencyBlockTripIndicesForRouteCollectionId(
      AgencyAndId routeCollectionId);

  public List<FrequencyBlockTripIndex> getFrequencyBlockTripIndicesForBlock(
      AgencyAndId blockId);

  public List<FrequencyBlockStopTimeIndex> getFrequencyStopTimeIndicesForStop(
      StopEntry stopEntry);

  public List<FrequencyStopTripIndex> getFrequencyStopTripIndicesForStop(
      StopEntry stop);

  /****
   * Block Sequence Indices
   ****/

}
