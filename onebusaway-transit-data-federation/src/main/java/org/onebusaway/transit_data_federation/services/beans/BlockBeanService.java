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
package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data.model.blocks.BlockBean;
import org.onebusaway.transit_data.model.blocks.BlockInstanceBean;
import org.onebusaway.transit_data.model.blocks.BlockTripBean;
import org.onebusaway.transit_data.model.blocks.ScheduledBlockLocationBean;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

/**
 * Service methods to lookup a {@link BlockBean} representation of a block: a
 * series of contiguous {@link Trip} objects.
 * 
 * @author bdferris
 * @see BlockBean
 * @see Trip
 */
public interface BlockBeanService {

  /**
   * @param block see {@link Trip#getBlockId()}
   * @return retrieve a bean representation of the specified block, or null if
   *         not found
   */
  public BlockBean getBlockForId(AgencyAndId blockId);

  public BlockTripBean getBlockTripAsBean(BlockTripEntry activeTrip);

  public BlockInstanceBean getBlockInstance(AgencyAndId blockId,
      long serviceDate);
  
  public ScheduledBlockLocationBean getScheduledBlockLocationFromScheduledTime(AgencyAndId blockId, long serviceDate, int scheduledTime);

  BlockInstanceBean getBlockInstanceAsBean(BlockInstance blockInstance);
}
