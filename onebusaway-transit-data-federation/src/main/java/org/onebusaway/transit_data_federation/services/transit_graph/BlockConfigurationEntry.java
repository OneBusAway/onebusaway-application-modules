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
package org.onebusaway.transit_data_federation.services.transit_graph;

import java.util.List;

import org.onebusaway.realtime.api.OccupancyStatus;
import org.onebusaway.transit_data_federation.services.blocks.HasIndexedBlockStopTimes;

/**
 * A block configuration i
 * 
 * @author bdferris
 * 
 */
public interface BlockConfigurationEntry extends HasIndexedBlockStopTimes {

  public BlockEntry getBlock();

  public ServiceIdActivation getServiceIds();

  public List<BlockTripEntry> getTrips();

  /**
   * A block with frequency information can be one of two types:
   * 
   * 1) A traditional frequency-based block where the block configuration
   * defines the trip patterns for the entire frequency range.
   * 
   * 2) A frequency-in-name-only block where block trips are operated on a fixed
   * schedule, but marketed as frequency-based to riders.
   * 
   * @return the frequency entries associated with the trips of this block
   *         configuration, or null if it's not a frequency-based block
   */
  public List<FrequencyEntry> getFrequencies();

  /**
   * @return distance, in meters
   */
  public double getTotalBlockDistance();

  public double getDistanceAlongBlockForIndex(int blockSequence);
  public OccupancyStatus getOccupancyForIndex(int stopIndex);
}
