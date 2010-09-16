package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceIdIntervals;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.BlockFirstTimeComparator;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndex;
import org.onebusaway.transit_data_federation.services.blocks.ServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockIndicesFactory {

  private static Logger _log = LoggerFactory.getLogger(BlockIndicesFactory.class);

  private static final BlockFirstTimeComparator _blockComparator = new BlockFirstTimeComparator();

  private CalendarService _calendarService;

  private boolean _verbose = false;

  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  public void setVerbose(boolean verbose) {
    _verbose = verbose;
  }

  public List<BlockIndexData> createData(Iterable<BlockEntry> blocks) {

    List<BlockIndex> indices = createIndices(blocks);
    List<BlockIndexData> allData = new ArrayList<BlockIndexData>();

    for (BlockIndex index : indices) {

      List<AgencyAndId> blockIds = MappingLibrary.map(index.getBlocks(), "id");
      ServiceIdIntervals serviceIdIntervals = index.getServiceIdIntervals();
      Map<LocalizedServiceId, ServiceIntervalBlock> intervalsByServiceId = index.getIntervalsByServiceId();

      BlockIndexData data = new BlockIndexData(blockIds, serviceIdIntervals,
          intervalsByServiceId);
      allData.add(data);
    }

    return allData;
  }

  public List<BlockIndex> createIndices(Iterable<BlockEntry> blocks) {

    List<BlockIndex> allIndices = new ArrayList<BlockIndex>();

    Map<BlockSequenceKey, List<BlockEntry>> blocksByKey = new FactoryMap<BlockSequenceKey, List<BlockEntry>>(
        new ArrayList<BlockEntry>());

    if (_verbose)
      _log.info("grouping blocks by sequence key");

    for (BlockEntry block : blocks) {
      BlockSequenceKey key = getBlockAsKey(block);
      blocksByKey.get(key).add(block);
    }

    if (_verbose)
      _log.info("groups found: " + blocksByKey.size());

    int count = 0;

    for (List<BlockEntry> blocksWithSameSequence : blocksByKey.values()) {

      if (_verbose && count % 100 == 0)
        _log.info("groups processed: " + count + "/" + blocksByKey.size());

      count++;

      List<List<BlockEntry>> groupedBlocks = ensureGroups(blocksWithSameSequence);

      for (List<BlockEntry> group : groupedBlocks) {
        BlockIndex index = createIndexForGroupOfBlocks(group);
        allIndices.add(index);
      }
    }

    return allIndices;
  }

  public BlockIndex createIndex(BlockEntry block) {
    List<BlockEntry> blocks = Arrays.asList(block);
    return createIndexForGroupOfBlocks(blocks);
  }

  /****
   * Private Methods
   ****/

  private BlockSequenceKey getBlockAsKey(BlockEntry block) {
    List<TripSequenceKey> keys = new ArrayList<TripSequenceKey>();
    for (TripEntry trip : block.getTrips()) {
      TripSequenceKey key = getTripAsKey(trip);
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

  private List<List<BlockEntry>> ensureGroups(List<BlockEntry> blocks) {

    Collections.sort(blocks, _blockComparator);

    List<List<BlockEntry>> lists = new ArrayList<List<BlockEntry>>();

    for (BlockEntry block : blocks) {
      List<BlockEntry> list = getBestList(lists, block);
      if (list == null) {
        list = new ArrayList<BlockEntry>();
        lists.add(list);
      }
      list.add(block);
    }

    return lists;
  }

  private List<BlockEntry> getBestList(List<List<BlockEntry>> lists,
      BlockEntry block) {

    for (List<BlockEntry> list : lists) {

      if (list.isEmpty())
        return list;

      BlockEntry prev = list.get(list.size() - 1);

      List<StopTimeEntry> stopTimesA = prev.getStopTimes();
      List<StopTimeEntry> stopTimesB = block.getStopTimes();

      boolean allGood = true;

      for (int i = 0; i < stopTimesA.size(); i++) {
        StopTimeEntry stopTimeA = stopTimesA.get(i);
        StopTimeEntry stopTimeB = stopTimesB.get(i);
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

  private BlockIndex createIndexForGroupOfBlocks(List<BlockEntry> blocks) {
    ServiceIdIntervals serviceIdIntervals = getBlocksAsServiceIdIntervals(blocks);

    Map<LocalizedServiceId, ServiceIntervalBlock> intervalsByServiceId = getBlocksAsBlockIntervals(
        blocks, serviceIdIntervals);
    BlockIndex index = new BlockIndex(blocks, serviceIdIntervals,
        intervalsByServiceId);
    return index;
  }

  private ServiceIdIntervals getBlocksAsServiceIdIntervals(
      List<BlockEntry> group) {
    ServiceIdIntervals serviceIdIntervals = new ServiceIdIntervals();

    for (BlockEntry block : group) {
      for (TripEntry trip : block.getTrips()) {

        AgencyAndId tripId = trip.getId();
        AgencyAndId serviceId = trip.getServiceId();
        LocalizedServiceId lsid = _calendarService.getLocalizedServiceIdForAgencyAndServiceId(
            tripId.getAgencyId(), serviceId);

        List<StopTimeEntry> stopTimes = trip.getStopTimes();
        StopTimeEntry first = stopTimes.get(0);
        StopTimeEntry last = stopTimes.get(stopTimes.size() - 1);
        serviceIdIntervals.addStopTime(lsid, first.getArrivalTime(),
            first.getDepartureTime());
        serviceIdIntervals.addStopTime(lsid, last.getArrivalTime(),
            last.getDepartureTime());
      }
    }
    return serviceIdIntervals;
  }

  private Map<LocalizedServiceId, ServiceIntervalBlock> getBlocksAsBlockIntervals(
      List<BlockEntry> blocks, ServiceIdIntervals serviceIdIntervals) {

    int n = blocks.size();

    Map<LocalizedServiceId, ServiceIntervalBlock> intervalsByServiceId = new HashMap<LocalizedServiceId, ServiceIntervalBlock>();

    for (LocalizedServiceId serviceId : serviceIdIntervals.getServiceIds()) {

      int[] minArrivals = new int[n];
      int[] minDepartures = new int[n];
      int[] maxArrivals = new int[n];
      int[] maxDepartures = new int[n];

      int index = 0;

      for (BlockEntry block : blocks) {

        ServiceInterval interval = null;

        for (TripEntry trip : block.getTrips()) {
          if (!serviceId.getId().equals(trip.getServiceId()))
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

      ServiceIntervalBlock intervals = new ServiceIntervalBlock(minArrivals,
          minDepartures, maxArrivals, maxDepartures);
      intervalsByServiceId.put(serviceId, intervals);
    }
    return intervalsByServiceId;
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
