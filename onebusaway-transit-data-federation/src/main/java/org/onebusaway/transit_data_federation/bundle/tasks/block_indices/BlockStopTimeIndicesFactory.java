package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.impl.transit_graph.FrequencyBlockStopTimeEntryImpl;
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
        _log.warn("block has no active configurations: " + block.getId());
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

    int count = 0;

    for (List<BlockStopTimeEntry> stopTimes : stopTimesByKey.values()) {

      if (_verbose && count % 1000 == 0)
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
      BlockStopTimeEntry blockStopTime = frequencyBlockStopTime.getStopTime();
      StopTimeEntry stopTime = blockStopTime.getStopTime();
      frequencies.add(frequencyBlockStopTime.getFrequency());
      blockConfigs.add(blockStopTime.getTrip().getBlockConfiguration());
      stopIndices[i] = blockStopTime.getBlockSequence();
      interval = ServiceInterval.extend(interval, stopTime.getArrivalTime(),
          stopTime.getDepartureTime());
    }

    return new FrequencyBlockStopTimeIndex(frequencies, blockConfigs,
        stopIndices, interval);
  }
}
