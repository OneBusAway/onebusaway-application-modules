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
import java.util.Map;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.util.LoggingIntervalUtil;
import org.onebusaway.transit_data_federation.impl.transit_graph.FrequencyBlockStopTimeEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyBlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Construct {@link BlockStopTimeIndex} indices from all {@link BlockEntry} in
 * the transit graph. The indices created by this factory are grouped by
 * serviceIds, such that all {@link BlockStopTimeEntry} stop entries with the
 * same active service ids are grouped together.
 * 
 * @author bdferris
 * @see BlockStopTimeIndex
 * @see BlockIndexService
 */
public class BlockStopTimeIndicesFactory {

  private static Logger _log = LoggerFactory.getLogger(BlockStopTimeIndicesFactory.class);

  private static final BlockStopTimeComparator _blockStopTimeLooseComparator = new BlockStopTimeComparator();

  private static final BlockStopTimeStrictComparator _blockStopTimeStrictComparator = new BlockStopTimeStrictComparator();

  private static final FrequencyBlockStopTimeComparator _frequencyBlockStopTimeLooseComparator = new FrequencyBlockStopTimeComparator();

  private static final FrequencyBlockStopTimeStrictComparator _frequencyBlockStopTimeStrictComparator = new FrequencyBlockStopTimeStrictComparator();
  
  private boolean _verbose = false;

  public void setVerbose(boolean verbose) {
    _verbose = verbose;
  }

  /****
   * 
   ****/

  public List<BlockStopTimeIndex> createIndices(Iterable<BlockEntry> blocks) {

    Map<BlockStopTimeKey, List<BlockStopTimeEntry>> stopTimesByKey = groupBlockStopTimes(
        blocks, false);

    return createIndicesFromGroups(stopTimesByKey);
  }

  public List<FrequencyBlockStopTimeIndex> createFrequencyIndices(
      Iterable<BlockEntry> blocks) {

    Map<BlockStopTimeKey, List<BlockStopTimeEntry>> stopTimesByKey = groupBlockStopTimes(
        blocks, true);

    return createFrequencyIndicesFromGroups(stopTimesByKey);
  }

  /****
   * 
   ****/

  private Map<BlockStopTimeKey, List<BlockStopTimeEntry>> groupBlockStopTimes(
      Iterable<BlockEntry> blocks, boolean frequencyBased) {

    Map<BlockStopTimeKey, List<BlockStopTimeEntry>> stopTimesByKey = new FactoryMap<BlockStopTimeKey, List<BlockStopTimeEntry>>(
        new ArrayList<BlockStopTimeEntry>());

    if (_verbose)
      _log.info("grouping block stop times by key");

    int stopTimeCount = 0;

    for (BlockEntry block : blocks) {

      List<BlockConfigurationEntry> configurations = block.getConfigurations();

      if (configurations.isEmpty()) {
        _log.warn("block is not referred to in calendars (no active configurations): " + block.getId());
        continue;
      }

      if (BlockLibrary.isFrequencyBased(block) != frequencyBased)
        continue;

      for (BlockConfigurationEntry blockConfiguration : configurations) {

        List<BlockStopTimeEntry> blockStopTimes = blockConfiguration.getStopTimes();

        for (BlockStopTimeEntry blockStopTime : blockStopTimes) {

          BlockStopTimeKey key = getBlockStopTimeAsKey(blockStopTime);
          List<BlockStopTimeEntry> stopTimesForKey = stopTimesByKey.get(key);
          stopTimesForKey.add(blockStopTime);
          stopTimeCount++;
        }
      }
    }

    if (_verbose)
      _log.info("groups found: " + stopTimesByKey.size()
          + " out of stopTimes: " + stopTimeCount);
    return stopTimesByKey;
  }

  private BlockStopTimeKey getBlockStopTimeAsKey(
      BlockStopTimeEntry blockStopTime) {

    BlockTripEntry blockTrip = blockStopTime.getTrip();
    BlockConfigurationEntry blockConfig = blockTrip.getBlockConfiguration();

    StopTimeEntry stopTime = blockStopTime.getStopTime();
    StopEntry stop = stopTime.getStop();

    return new BlockStopTimeKey(blockConfig.getServiceIds(), stop.getId());
  }

  /****
   * 
   ****/

  private List<BlockStopTimeIndex> createIndicesFromGroups(
      Map<BlockStopTimeKey, List<BlockStopTimeEntry>> stopTimesByKey) {

    List<BlockStopTimeIndex> allIndices = new ArrayList<BlockStopTimeIndex>();
    int logInterval = LoggingIntervalUtil.getAppropriateLoggingInterval(allIndices.size()) * 10;

    int count = 0;

    for (List<BlockStopTimeEntry> stopTimes : stopTimesByKey.values()) {

      if (_verbose && count % logInterval == 0)
        _log.info("groups processed: " + count + "/" + stopTimesByKey.size());

      count++;

      List<List<BlockStopTimeEntry>> groupedStopTimes = BlockLibrary.createStrictlyOrderedGroups(
          stopTimes, _blockStopTimeLooseComparator,
          _blockStopTimeStrictComparator);

      for (List<BlockStopTimeEntry> group : groupedStopTimes) {
        BlockStopTimeIndex index = createBlockStopTimeIndexForGroup(group);
        allIndices.add(index);
      }
    }

    return allIndices;
  }

  private BlockStopTimeIndex createBlockStopTimeIndexForGroup(
      List<BlockStopTimeEntry> group) {

    int n = group.size();

    List<BlockConfigurationEntry> blockConfigs = new ArrayList<BlockConfigurationEntry>(
        n);
    int[] stopIndices = new int[n];

    ServiceInterval interval = null;

    for (int i = 0; i < n; i++) {
      BlockStopTimeEntry blockStopTime = group.get(i);
      StopTimeEntry stopTime = blockStopTime.getStopTime();
      blockConfigs.add(blockStopTime.getTrip().getBlockConfiguration());
      stopIndices[i] = blockStopTime.getBlockSequence();
      interval = ServiceInterval.extend(interval, stopTime.getArrivalTime(),
          stopTime.getDepartureTime());
    }

    return new BlockStopTimeIndex(blockConfigs, stopIndices, interval);
  }

  /****
   * 
   ****/

  private List<FrequencyBlockStopTimeIndex> createFrequencyIndicesFromGroups(
      Map<BlockStopTimeKey, List<BlockStopTimeEntry>> stopTimesByKey) {

    List<FrequencyBlockStopTimeIndex> allIndices = new ArrayList<FrequencyBlockStopTimeIndex>();

    int count = 0;

    for (List<BlockStopTimeEntry> stopTimes : stopTimesByKey.values()) {

      if (_verbose && count % 100 == 0)
        _log.info("groups processed: " + count + "/" + stopTimesByKey.size());

      count++;

      List<FrequencyBlockStopTimeEntry> frequencyStopTimes = getStopTimesAsFrequencyStopTimes(stopTimes);

      List<List<FrequencyBlockStopTimeEntry>> groupedStopTimes = BlockLibrary.createStrictlyOrderedGroups(
          frequencyStopTimes, _frequencyBlockStopTimeLooseComparator,
          _frequencyBlockStopTimeStrictComparator);

      for (List<FrequencyBlockStopTimeEntry> group : groupedStopTimes) {
        FrequencyBlockStopTimeIndex index = createFrequencyBlockStopTimeIndexForGroup(group);
        allIndices.add(index);
      }
    }

    return allIndices;
  }

  private List<FrequencyBlockStopTimeEntry> getStopTimesAsFrequencyStopTimes(
      List<BlockStopTimeEntry> stopTimes) {

    List<FrequencyBlockStopTimeEntry> frequencyStopTimes = new ArrayList<FrequencyBlockStopTimeEntry>();

    for (BlockStopTimeEntry blockStopTime : stopTimes) {
      BlockTripEntry trip = blockStopTime.getTrip();
      BlockConfigurationEntry blockConfig = trip.getBlockConfiguration();
      for (FrequencyEntry frequency : blockConfig.getFrequencies()) {
        FrequencyBlockStopTimeEntry frequencyStopTime = new FrequencyBlockStopTimeEntryImpl(
            blockStopTime, frequency);
        frequencyStopTimes.add(frequencyStopTime);
      }
    }

    return frequencyStopTimes;
  }

  private FrequencyBlockStopTimeIndex createFrequencyBlockStopTimeIndexForGroup(
      List<FrequencyBlockStopTimeEntry> group) {

    int n = group.size();

    List<FrequencyEntry> frequencies = new ArrayList<FrequencyEntry>(n);
    List<BlockConfigurationEntry> blockConfigs = new ArrayList<BlockConfigurationEntry>(
        n);
    int[] stopIndices = new int[n];

    ServiceInterval interval = null;

    for (int i = 0; i < n; i++) {
      FrequencyBlockStopTimeEntry frequencyBlockStopTime = group.get(i);
      FrequencyEntry frequency = frequencyBlockStopTime.getFrequency();
      frequencies.add(frequency);
      BlockStopTimeEntry blockStopTime = frequencyBlockStopTime.getStopTime();
      blockConfigs.add(blockStopTime.getTrip().getBlockConfiguration());
      stopIndices[i] = blockStopTime.getBlockSequence();
      interval = ServiceInterval.extend(interval, frequency.getStartTime(),
          frequency.getStartTime());
      interval = ServiceInterval.extend(interval, frequency.getEndTime(),
          frequency.getEndTime());
    }

    return new FrequencyBlockStopTimeIndex(frequencies, blockConfigs,
        stopIndices, interval);
  }
}
