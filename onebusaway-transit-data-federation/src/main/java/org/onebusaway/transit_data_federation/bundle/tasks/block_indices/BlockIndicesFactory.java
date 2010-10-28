package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndex;
import org.onebusaway.transit_data_federation.services.blocks.ServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockIndicesFactory {

  private static Logger _log = LoggerFactory.getLogger(BlockIndicesFactory.class);

  private static final BlockFirstTimeComparator _blockComparator = new BlockFirstTimeComparator();

  private boolean _verbose = false;

  public void setVerbose(boolean verbose) {
    _verbose = verbose;
  }

  public List<BlockIndexData> createData(Iterable<BlockEntry> blocks) {

    List<BlockIndex> indices = createIndices(blocks);
    List<BlockIndexData> allData = new ArrayList<BlockIndexData>();

    for (BlockIndex index : indices) {

      List<BlockConfigurationIndex> configurationIndices = new ArrayList<BlockConfigurationIndex>();

      for (BlockConfigurationEntry entry : index.getBlocks()) {
        BlockEntry block = entry.getBlock();
        int configurationIndex = block.getConfigurations().indexOf(entry);
        configurationIndices.add(new BlockConfigurationIndex(block.getId(),
            configurationIndex));
      }

      ServiceIntervalBlock serviceIntervalBlock = index.getServiceIntervalBlock();

      BlockIndexData data = new BlockIndexData(configurationIndices,
          serviceIntervalBlock);
      allData.add(data);
    }

    return allData;
  }

  public List<BlockIndex> createIndices(Iterable<BlockEntry> blocks) {

    List<BlockIndex> allIndices = new ArrayList<BlockIndex>();

    Map<BlockSequenceKey, List<BlockConfigurationEntry>> blocksByKey = new FactoryMap<BlockSequenceKey, List<BlockConfigurationEntry>>(
        new ArrayList<BlockConfigurationEntry>());

    if (_verbose)
      _log.info("grouping blocks by sequence key");

    for (BlockEntry block : blocks) {
      for (BlockConfigurationEntry blockConfiguration : block.getConfigurations()) {
        BlockSequenceKey key = getBlockConfigurationAsKey(blockConfiguration);
        blocksByKey.get(key).add(blockConfiguration);
      }
    }

    if (_verbose)
      _log.info("groups found: " + blocksByKey.size());

    int count = 0;

    for (List<BlockConfigurationEntry> blocksWithSameSequence : blocksByKey.values()) {

      if (_verbose && count % 100 == 0)
        _log.info("groups processed: " + count + "/" + blocksByKey.size());

      count++;

      List<List<BlockConfigurationEntry>> groupedBlocks = ensureGroups(blocksWithSameSequence);

      for (List<BlockConfigurationEntry> group : groupedBlocks) {
        BlockIndex index = createIndexForGroupOfBlocks(group);
        allIndices.add(index);
      }
    }

    return allIndices;
  }

  public BlockIndex createIndexForGroupOfBlocks(
      List<BlockConfigurationEntry> blocks) {
    ServiceIntervalBlock serviceIntervalBlock = getBlocksAsBlockInterval(blocks);
    return new BlockIndex(blocks, serviceIntervalBlock);
  }

  /****
   * Private Methods
   ****/

  private BlockSequenceKey getBlockConfigurationAsKey(
      BlockConfigurationEntry block) {
    List<TripSequenceKey> keys = new ArrayList<TripSequenceKey>();
    for (BlockTripEntry blockTrip : block.getTrips()) {
      TripSequenceKey key = getTripAsKey(blockTrip.getTrip());
      keys.add(key);
    }
    return new BlockSequenceKey(keys);
  }

  private TripSequenceKey getTripAsKey(TripEntry trip) {
    List<AgencyAndId> stopIds = new ArrayList<AgencyAndId>();
    for (StopTimeEntry stopTime : trip.getStopTimes())
      stopIds.add(stopTime.getStop().getId());
    return new TripSequenceKey(trip.getServiceId(), stopIds);
  }

  private List<List<BlockConfigurationEntry>> ensureGroups(
      List<BlockConfigurationEntry> blocks) {

    Collections.sort(blocks, _blockComparator);

    List<List<BlockConfigurationEntry>> lists = new ArrayList<List<BlockConfigurationEntry>>();

    for (BlockConfigurationEntry block : blocks) {
      List<BlockConfigurationEntry> list = getBestList(lists, block);
      if (list == null) {
        list = new ArrayList<BlockConfigurationEntry>();
        lists.add(list);
      }
      list.add(block);
    }

    return lists;
  }

  private List<BlockConfigurationEntry> getBestList(
      List<List<BlockConfigurationEntry>> lists, BlockConfigurationEntry block) {

    for (List<BlockConfigurationEntry> list : lists) {

      if (list.isEmpty())
        return list;

      BlockConfigurationEntry prev = list.get(list.size() - 1);

      List<BlockStopTimeEntry> stopTimesA = prev.getStopTimes();
      List<BlockStopTimeEntry> stopTimesB = block.getStopTimes();

      boolean allGood = true;

      for (int i = 0; i < stopTimesA.size(); i++) {
        StopTimeEntry stopTimeA = stopTimesA.get(i).getStopTime();
        StopTimeEntry stopTimeB = stopTimesB.get(i).getStopTime();
        if (!(stopTimeA.getArrivalTime() <= stopTimeB.getArrivalTime() && stopTimeA.getDepartureTime() <= stopTimeB.getDepartureTime())) {
          allGood = false;
          break;
        }
      }

      if (allGood)
        return list;
    }

    return null;
  }

  private ServiceIntervalBlock getBlocksAsBlockInterval(
      List<BlockConfigurationEntry> blocks) {

    BlockConfigurationEntry firstBlock = blocks.get(0);
    ServiceIdActivation serviceIdActivation = firstBlock.getServiceIds();
    Set<LocalizedServiceId> serviceIds = new HashSet<LocalizedServiceId>(
        serviceIdActivation.getActiveServiceIds());

    int n = blocks.size();

    int[] minArrivals = new int[n];
    int[] minDepartures = new int[n];
    int[] maxArrivals = new int[n];
    int[] maxDepartures = new int[n];

    int index = 0;

    for (BlockConfigurationEntry block : blocks) {

      ServiceInterval interval = null;

      for (BlockTripEntry blockTrip : block.getTrips()) {
        TripEntry trip = blockTrip.getTrip();
        if (!serviceIds.contains(trip.getServiceId()))
          continue;
        List<StopTimeEntry> stopTimes = trip.getStopTimes();
        StopTimeEntry first = stopTimes.get(0);
        StopTimeEntry last = stopTimes.get(stopTimes.size() - 1);
        interval = extend(interval, first);
        interval = extend(interval, last);
      }

      minArrivals[index] = interval.getMinArrival();
      minDepartures[index] = interval.getMinDeparture();
      maxArrivals[index] = interval.getMaxArrival();
      maxDepartures[index] = interval.getMaxDeparture();

      index++;
    }

    return new ServiceIntervalBlock(minArrivals, minDepartures, maxArrivals,
        maxDepartures);
  }

  private ServiceInterval extend(ServiceInterval interval,
      StopTimeEntry stopTime) {
    if (interval == null)
      return new ServiceInterval(stopTime.getArrivalTime(),
          stopTime.getDepartureTime());
    return interval.extend(stopTime.getArrivalTime(),
        stopTime.getDepartureTime());
  }
}
