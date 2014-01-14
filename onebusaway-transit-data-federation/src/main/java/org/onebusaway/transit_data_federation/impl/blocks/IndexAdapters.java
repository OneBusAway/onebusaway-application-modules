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

import org.onebusaway.transit_data_federation.impl.time.GenericBinarySearch.IndexAdapter;
import org.onebusaway.transit_data_federation.services.blocks.HasIndexedBlockStopTimes;
import org.onebusaway.transit_data_federation.services.blocks.HasIndexedFrequencyBlockTrips;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;

public class IndexAdapters {

  public static final IndexAdapter<HasIndexedBlockStopTimes> BLOCK_STOP_TIME_ARRIVAL_INSTANCE = new BlockStopTimeArrivalTimeIndexAdapter();

  public static final IndexAdapter<HasIndexedBlockStopTimes> BLOCK_STOP_TIME_DEPARTURE_INSTANCE = new BlockStopTimeDepartureTimeIndexAdapter();

  public static final IndexAdapter<BlockConfigurationEntry> BLOCK_CONFIG_DISTANCE_INSTANCE = new BlockConfigDistanceAlongBlockIndexAdapter();

  public static final IndexAdapter<HasIndexedFrequencyBlockTrips> FREQUENCY_END_TIME_INSTANCE = new FrequencyEndTimeIndexAdapter();

  public static final IndexAdapter<HasIndexedFrequencyBlockTrips> FREQUENCY_START_TIME_INSTANCE = new FrequencyStartTimeIndexAdapter();

  /****
   * 
   ****/

  private static class BlockStopTimeArrivalTimeIndexAdapter implements
      IndexAdapter<HasIndexedBlockStopTimes> {

    @Override
    public double getValue(HasIndexedBlockStopTimes source, int index) {
      return source.getArrivalTimeForIndex(index);
    }
  }

  private static class BlockStopTimeDepartureTimeIndexAdapter implements
      IndexAdapter<HasIndexedBlockStopTimes> {

    @Override
    public double getValue(HasIndexedBlockStopTimes source, int index) {
      return source.getDepartureTimeForIndex(index);
    }
  }

  private static class BlockConfigDistanceAlongBlockIndexAdapter implements
      IndexAdapter<BlockConfigurationEntry> {

    @Override
    public double getValue(BlockConfigurationEntry source, int index) {
      return source.getDistanceAlongBlockForIndex(index);
    }
  }

  private static class FrequencyEndTimeIndexAdapter implements
      IndexAdapter<HasIndexedFrequencyBlockTrips> {

    @Override
    public double getValue(HasIndexedFrequencyBlockTrips source, int index) {
      return source.getEndTimeForIndex(index);
    }
  }

  private static class FrequencyStartTimeIndexAdapter implements
      IndexAdapter<HasIndexedFrequencyBlockTrips> {

    @Override
    public double getValue(HasIndexedFrequencyBlockTrips source, int index) {
      return source.getStartTimeForIndex(index);
    }
  }
}
