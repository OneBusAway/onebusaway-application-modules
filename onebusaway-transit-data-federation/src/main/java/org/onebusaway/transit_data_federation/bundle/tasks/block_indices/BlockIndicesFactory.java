package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.bundle.tasks.transit_graph.FrequencyComparator;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.blocks.ServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockIndicesFactory {

  private static Logger _log = LoggerFactory.getLogger(BlockIndicesFactory.class);

  private static final BlockTripFirstTimeComparator _blockTripComparator = new BlockTripFirstTimeComparator();

  private static final FrequencyComparator _frequencyComparator = new FrequencyComparator();

  private boolean _verbose = false;

  public void setVerbose(boolean verbose) {
    _verbose = verbose;
  }

  /****
   * 
   ****/

  public List<BlockTripIndexData> createTripData(Iterable<BlockEntry> blocks) {

    List<BlockTripIndex> indices = createTripIndices(blocks);
    List<BlockTripIndexData> allData = new ArrayList<BlockTripIndexData>();

    for (BlockTripIndex index : indices) {

      List<BlockTripReference> references = new ArrayList<BlockTripReference>();

      for (BlockTripEntry trip : index.getTrips()) {
        BlockTripReference ref = ReferencesLibrary.getTripAsReference(trip);
        references.add(ref);
      }

      ServiceIntervalBlock serviceIntervalBlock = index.getServiceIntervalBlock();

      BlockTripIndexData data = new BlockTripIndexData(references,
          serviceIntervalBlock);
      allData.add(data);
    }

    return allData;
  }

  public List<FrequencyBlockTripIndexData> createFrequencyTripData(
      Iterable<BlockEntry> blocks) {

    List<FrequencyBlockTripIndex> indices = createFrequencyTripIndices(blocks);
    List<FrequencyBlockTripIndexData> allData = new ArrayList<FrequencyBlockTripIndexData>();

    for (FrequencyBlockTripIndex index : indices) {

      List<BlockTripReference> tripReferences = new ArrayList<BlockTripReference>();

      for (BlockTripEntry entry : index.getTrips()) {
        BlockTripReference reference = ReferencesLibrary.getTripAsReference(entry);
        tripReferences.add(reference);
      }

      FrequencyServiceIntervalBlock serviceIntervalBlock = index.getServiceIntervalBlock();

      FrequencyBlockTripIndexData data = new FrequencyBlockTripIndexData(
          tripReferences, index.getFrequencies(), serviceIntervalBlock);
      allData.add(data);
    }

    return allData;
  }

  /****
   * 
   ****/

  public List<BlockTripIndex> createTripIndices(Iterable<BlockEntry> blocks) {

    List<BlockTripIndex> allIndices = new ArrayList<BlockTripIndex>();

    Map<BlockTripSequenceKey, List<BlockTripEntry>> blockTripsByKey = new FactoryMap<BlockTripSequenceKey, List<BlockTripEntry>>(
        new ArrayList<BlockTripEntry>());

    if (_verbose)
      _log.info("grouping block trips by sequence key");

    int tripCount = 0;

    for (BlockEntry block : blocks) {
      
      if( block.getConfigurations().isEmpty() ) {
        _log.warn("block has no configurations: " + block.getId());
        continue;
      }

      if (isFrequencyBased(block))
        continue;

      for (BlockConfigurationEntry blockConfiguration : block.getConfigurations()) {
        for (BlockTripEntry blockTrip : blockConfiguration.getTrips()) {
          BlockTripSequenceKey key = getBlockTripAsKey(blockTrip);
          blockTripsByKey.get(key).add(blockTrip);
          tripCount++;
        }

      }
    }

    if (_verbose)
      _log.info("groups found: " + blockTripsByKey.size() + " out of trips: "
          + tripCount);

    int count = 0;

    for (List<BlockTripEntry> tripsWithSameSequence : blockTripsByKey.values()) {

      if (_verbose && count % 100 == 0)
        _log.info("groups processed: " + count + "/" + blockTripsByKey.size());

      count++;

      List<List<BlockTripEntry>> groupedBlocks = ensureTripGroups(tripsWithSameSequence);

      for (List<BlockTripEntry> group : groupedBlocks) {
        BlockTripIndex index = createIndexForGroupOfBlockTrips(group);
        allIndices.add(index);
      }
    }

    return allIndices;
  }

  public List<FrequencyBlockTripIndex> createFrequencyTripIndices(
      Iterable<BlockEntry> blocks) {

    List<FrequencyBlockTripIndex> allIndices = new ArrayList<FrequencyBlockTripIndex>();

    Map<BlockTripSequenceKey, List<BlockTripEntry>> blockTripsByKey = new FactoryMap<BlockTripSequenceKey, List<BlockTripEntry>>(
        new ArrayList<BlockTripEntry>());

    if (_verbose)
      _log.info("grouping blocks by sequence key");

    int tripCount = 0;

    for (BlockEntry block : blocks) {

      if( block.getConfigurations().isEmpty() ) {
        _log.warn("block has no configurations: " + block.getId());
        continue;
      }
      
      if (!isFrequencyBased(block))
        continue;

      for (BlockConfigurationEntry blockConfiguration : block.getConfigurations()) {
        for (BlockTripEntry blockTrip : blockConfiguration.getTrips()) {
          BlockTripSequenceKey key = getBlockTripAsKey(blockTrip);
          blockTripsByKey.get(key).add(blockTrip);
          tripCount++;
        }
      }
    }

    if (_verbose)
      _log.info("frequency groups found: " + blockTripsByKey.size()
          + " out of trips: " + tripCount);

    int count = 0;

    for (List<BlockTripEntry> tripsWithSameSequence : blockTripsByKey.values()) {

      if (_verbose && count % 100 == 0)
        _log.info("frequency groups processed: " + count + "/"
            + blockTripsByKey.size());

      count++;

      List<FrequencyTripGroup> groupedTrips = ensureFrequencyTripGroups(tripsWithSameSequence);

      for (FrequencyTripGroup group : groupedTrips) {
        FrequencyBlockTripIndex index = createFrequencyIndexForGroupOfBlockTrips(group);
        allIndices.add(index);
      }
    }

    return allIndices;
  }

  /****
   * 
   ****/

  public BlockTripIndex createIndexForGroupOfBlockTrips(
      List<BlockTripEntry> blocks) {
    ServiceIntervalBlock serviceIntervalBlock = getBlockTripsAsBlockInterval(blocks);
    return new BlockTripIndex(blocks, serviceIntervalBlock);
  }

  public FrequencyBlockTripIndex createFrequencyIndexForGroupOfBlockTrips(
      FrequencyTripGroup group) {
    FrequencyServiceIntervalBlock serviceIntervalBlock = getBlockTripssAsFrequencyBlockInterval(group);
    group.trimToSize();
    return new FrequencyBlockTripIndex(group.getTrips(),
        group.getFrequencies(), serviceIntervalBlock);
  }

  /****
   * Private Methods
   ****/

  private boolean isFrequencyBased(BlockEntry blockEntry) {
    return blockEntry.getConfigurations().get(0).getFrequencies() != null;
  }

  private BlockTripSequenceKey getBlockTripAsKey(BlockTripEntry blockTrip) {
    List<AgencyAndId> stopIds = new ArrayList<AgencyAndId>();
    for (StopTimeEntry stopTime : blockTrip.getTrip().getStopTimes())
      stopIds.add(stopTime.getStop().getId());
    return new BlockTripSequenceKey(
        blockTrip.getBlockConfiguration().getServiceIds(), stopIds);
  }

  private List<List<BlockTripEntry>> ensureTripGroups(
      List<BlockTripEntry> blockTrips) {

    Collections.sort(blockTrips, _blockTripComparator);

    List<List<BlockTripEntry>> lists = new ArrayList<List<BlockTripEntry>>();

    for (BlockTripEntry block : blockTrips) {
      List<BlockTripEntry> list = getBestTripList(lists, block);
      if (list == null) {
        list = new ArrayList<BlockTripEntry>();
        lists.add(list);
      }
      list.add(block);
    }

    return lists;
  }

  private List<BlockTripEntry> getBestTripList(
      List<List<BlockTripEntry>> lists, BlockTripEntry trip) {

    for (List<BlockTripEntry> list : lists) {

      if (list.isEmpty())
        return list;

      BlockTripEntry prev = list.get(list.size() - 1);

      List<BlockStopTimeEntry> stopTimesA = prev.getStopTimes();
      List<BlockStopTimeEntry> stopTimesB = trip.getStopTimes();

      boolean allGood = areStopTimesAlwaysIncreasingOrEqual(stopTimesA,
          stopTimesB);

      if (allGood)
        return list;
    }

    return null;
  }

  private boolean areStopTimesAlwaysIncreasingOrEqual(
      List<BlockStopTimeEntry> stopTimesA, List<BlockStopTimeEntry> stopTimesB) {
    boolean allGood = true;

    for (int i = 0; i < stopTimesA.size(); i++) {
      StopTimeEntry stopTimeA = stopTimesA.get(i).getStopTime();
      StopTimeEntry stopTimeB = stopTimesB.get(i).getStopTime();
      if (!(stopTimeA.getArrivalTime() <= stopTimeB.getArrivalTime() && stopTimeA.getDepartureTime() <= stopTimeB.getDepartureTime())) {
        allGood = false;
        break;
      }
    }
    return allGood;
  }

  private List<FrequencyTripGroup> ensureFrequencyTripGroups(
      List<BlockTripEntry> tripsWithSameSequence) {

    List<BlockTripWithFrequency> btwfs = new ArrayList<BlockTripWithFrequency>();

    for (BlockTripEntry trip : tripsWithSameSequence) {
      BlockConfigurationEntry blockConfig = trip.getBlockConfiguration();
      for (FrequencyEntry frequency : blockConfig.getFrequencies())
        btwfs.add(new BlockTripWithFrequency(trip, frequency));
    }

    Collections.sort(btwfs);

    List<FrequencyTripGroup> lists = new ArrayList<FrequencyTripGroup>();

    for (BlockTripWithFrequency btwf : btwfs) {
      FrequencyTripGroup group = getBestFrequencyTripGroup(lists, btwf);
      if (group == null) {
        group = new FrequencyTripGroup();
        lists.add(group);
      }
      group.addEntry(btwf);
    }

    return lists;
  }

  private FrequencyTripGroup getBestFrequencyTripGroup(
      List<FrequencyTripGroup> groups, BlockTripWithFrequency bcwf) {

    for (FrequencyTripGroup group : groups) {

      if (group.isEmpty())
        return group;

      List<FrequencyEntry> frequencies = group.getFrequencies();

      FrequencyEntry prev = frequencies.get(frequencies.size() - 1);
      FrequencyEntry next = bcwf.getFrequency();

      if (prev.getEndTime() <= next.getStartTime())
        return group;
    }

    return null;
  }

  /****
   * 
   ****/

  private ServiceIntervalBlock getBlockTripsAsBlockInterval(
      List<BlockTripEntry> trips) {

    int n = trips.size();

    int[] minArrivals = new int[n];
    int[] minDepartures = new int[n];
    int[] maxArrivals = new int[n];
    int[] maxDepartures = new int[n];

    int index = 0;

    for (BlockTripEntry trip : trips) {

      ServiceInterval interval = null;

      List<BlockStopTimeEntry> stopTimes = trip.getStopTimes();
      StopTimeEntry first = stopTimes.get(0).getStopTime();
      StopTimeEntry last = stopTimes.get(stopTimes.size() - 1).getStopTime();
      interval = extend(interval, first);
      interval = extend(interval, last);

      minArrivals[index] = interval.getMinArrival();
      minDepartures[index] = interval.getMinDeparture();
      maxArrivals[index] = interval.getMaxArrival();
      maxDepartures[index] = interval.getMaxDeparture();

      index++;
    }

    return new ServiceIntervalBlock(minArrivals, minDepartures, maxArrivals,
        maxDepartures);
  }

  private FrequencyServiceIntervalBlock getBlockTripssAsFrequencyBlockInterval(
      FrequencyTripGroup group) {

    List<BlockTripEntry> trips = group.getTrips();
    List<FrequencyEntry> frequencies = group.getFrequencies();

    int n = trips.size();

    int[] startTimes = new int[n];
    int[] endTimes = new int[n];

    for (int index = 0; index < n; index++) {

      FrequencyEntry freq = frequencies.get(index);

      startTimes[index] = freq.getStartTime();
      endTimes[index] = freq.getEndTime();
    }

    return new FrequencyServiceIntervalBlock(startTimes, endTimes);
  }

  private ServiceInterval extend(ServiceInterval interval,
      StopTimeEntry stopTime) {
    if (interval == null)
      return new ServiceInterval(stopTime.getArrivalTime(),
          stopTime.getDepartureTime());
    return interval.extend(stopTime.getArrivalTime(),
        stopTime.getDepartureTime());
  }

  private static class BlockTripWithFrequency implements
      Comparable<BlockTripWithFrequency> {

    private BlockTripEntry _trip;

    private FrequencyEntry _frequency;

    public BlockTripWithFrequency(BlockTripEntry trip, FrequencyEntry frequency) {
      _trip = trip;
      _frequency = frequency;
    }

    public FrequencyEntry getFrequency() {
      return _frequency;
    }

    @Override
    public int compareTo(BlockTripWithFrequency obj) {
      return _frequencyComparator.compare(_frequency, obj._frequency);
    }
  }

  private static class FrequencyTripGroup {

    private ArrayList<BlockTripEntry> _trips = new ArrayList<BlockTripEntry>();

    private ArrayList<FrequencyEntry> _frequencies = new ArrayList<FrequencyEntry>();

    public void addEntry(BlockTripWithFrequency bcwf) {
      _trips.add(bcwf._trip);
      _frequencies.add(bcwf._frequency);
    }

    public boolean isEmpty() {
      return _trips.isEmpty();
    }

    public List<BlockTripEntry> getTrips() {
      return _trips;
    }

    public List<FrequencyEntry> getFrequencies() {
      return _frequencies;
    }

    public void trimToSize() {
      _trips.trimToSize();
      _frequencies.trimToSize();
    }
  }
}
