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
package org.onebusaway.transit_data_federation.services.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.realtime.api.OccupancyStatus;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

/**
 * Provides an index over arrivals and departures at a particular stop.
 * Specifically, provides an ordered list over {@link BlockStopTimeEntry}
 * entries at the stop, where each arrival and departure in the list is greater
 * than or equal to the previous stop time in the list. All stop times also have
 * the same {@link ServiceIdActivation}.
 * 
 * @author bdferris
 * 
 */
@TransitTimeIndex
public class BlockStopTimeIndex extends AbstractBlockStopTimeIndex implements
    HasIndexedBlockStopTimes {

  public static BlockStopTimeIndex create(BlockTripIndex blockTripIndex,
      int blockSequence) {

    List<BlockTripEntry> tripsList = blockTripIndex.getTrips();
    int n = tripsList.size();

    List<BlockConfigurationEntry> blockConfigs = new ArrayList<BlockConfigurationEntry>(
        n);

    for (BlockTripEntry trip : tripsList)
      blockConfigs.add(trip.getBlockConfiguration());

    int[] stopIndices = new int[n];
    Arrays.fill(stopIndices, blockSequence);

    ServiceInterval serviceInterval = computeServiceInterval(blockTripIndex,
        blockSequence);

    return new BlockStopTimeIndex(blockConfigs, stopIndices, serviceInterval);
  }

  public BlockStopTimeIndex(List<BlockConfigurationEntry> blockConfigs,
      int[] stopIndices, ServiceInterval serviceInterval) {
    super(blockConfigs, stopIndices, serviceInterval);
  }

  /****
   * {@link HasIndexedBlockStopTimes} Interface
   ****/

  @Override
  public int getArrivalTimeForIndex(int index) {
    BlockConfigurationEntry blockConfig = _blockConfigs.get(index);
    int stopIndex = _stopIndices[index];
    return blockConfig.getArrivalTimeForIndex(stopIndex);
  }

  @Override
  public int getDepartureTimeForIndex(int index) {
    BlockConfigurationEntry blockConfig = _blockConfigs.get(index);
    int stopIndex = _stopIndices[index];
    return blockConfig.getDepartureTimeForIndex(stopIndex);
  }

  public double getDistanceAlongBlockForIndex(int index) {
    BlockConfigurationEntry blockConfig = _blockConfigs.get(index);
    int stopIndex = _stopIndices[index];
    return blockConfig.getDistanceAlongBlockForIndex(stopIndex);
  }

  public OccupancyStatus getOccupancyForIndex(int index) {
    BlockConfigurationEntry blockConfig = _blockConfigs.get(index);
    int stopIndex = _stopIndices[index];
    return blockConfig.getOccupancyForIndex(stopIndex);
  }
}
