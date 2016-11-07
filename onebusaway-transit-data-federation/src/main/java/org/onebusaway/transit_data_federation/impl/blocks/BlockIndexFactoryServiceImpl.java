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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.geospatial.services.GeometryLibrary;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.beans.AgencyBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexFactoryService;
import org.onebusaway.transit_data_federation.services.blocks.BlockLayoverIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockLayoverIndexData;
import org.onebusaway.transit_data_federation.services.blocks.BlockSequenceIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndexData;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripReference;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockTripIndexData;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.blocks.LayoverIntervalBlock;
import org.onebusaway.transit_data_federation.services.blocks.ReferencesLibrary;
import org.onebusaway.transit_data_federation.services.blocks.ServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.HasBlockStopTimes;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.transit_data_federation.util.LoggingIntervalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockIndexFactoryServiceImpl implements BlockIndexFactoryService {

  private static Logger _log = LoggerFactory.getLogger(BlockIndexFactoryServiceImpl.class);

  private static final Comparator<BlockTripEntry> _blockTripLooseComparator = new BlockStopTimeLooseComparator<BlockTripEntry>();

  private static final Comparator<BlockTripEntry> _blockTripStrictComparator = new BlockTripStrictComparator<BlockTripEntry>();

  private static final BlockTripLayoverTimeComparator _blockLayoverComparator = new BlockTripLayoverTimeComparator();

  private static final FrequencyComparator _frequencyComparator = new FrequencyComparator();

  private static final Comparator<BlockSequence> _blockSequenceLooseComparator = new BlockStopTimeLooseComparator<BlockSequence>();

  private static final Comparator<BlockSequence> _blockSequenceStrictComparator = new BlockTripStrictComparator<BlockSequence>();
  
  private AgencyService _agencyService;

  private AgencyBeanService _agencyBeanService;

  private boolean _verbose = false;

  /**
   * Time, in seconds
   */
  private int _maxSlackBetweenConsecutiveTrips = 5 * 60;

  private int _maxScheduledTimeBetweenConsecutiveTrips = 15 * 60;

  private Set<String> _privateAgencyIds = new HashSet<String>();

  @Autowired
  public void setAgencyService(AgencyService agencyService) {
    _agencyService = agencyService;
  }

  @Autowired
  public void setAgencyBeanService(AgencyBeanService agencyBeanService) {
    _agencyBeanService = agencyBeanService;
  }

  public void setVerbose(boolean verbose) {
    _verbose = verbose;
  }

  /**
   * 
   * @param maxSlackBetweenConsecutiveTrips time, in seconds
   */
  public void setMaxSlackBetweenConsecutiveTrips(
      int maxSlackBetweenConsecutiveTrips) {
    _maxSlackBetweenConsecutiveTrips = maxSlackBetweenConsecutiveTrips;
  }

  public void setMaxScheduledTimeBetweenConsecutiveTrips(
      int maxScheduledTimeBetweenConsecutiveTrips) {
    _maxScheduledTimeBetweenConsecutiveTrips = maxScheduledTimeBetweenConsecutiveTrips;
  }

  public BlockIndexFactoryServiceImpl() {

  }

  public BlockIndexFactoryServiceImpl(boolean verbose) {
    _verbose = verbose;
  }

  @PostConstruct
  public void setup() {
    for (String agencyId : _agencyService.getAllAgencyIds()) {
      AgencyBean bean = _agencyBeanService.getAgencyForId(agencyId);
      if (bean.isPrivateService())
        _privateAgencyIds.add(agencyId);
    }
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

  public List<BlockLayoverIndexData> createLayoverData(
      Iterable<BlockEntry> blocks) {

    List<BlockLayoverIndex> indices = createLayoverIndices(blocks);
    List<BlockLayoverIndexData> allData = new ArrayList<BlockLayoverIndexData>();

    for (BlockLayoverIndex index : indices) {

      List<BlockTripReference> references = new ArrayList<BlockTripReference>();

      for (BlockTripEntry trip : index.getTrips()) {
        BlockTripReference ref = ReferencesLibrary.getTripAsReference(trip);
        references.add(ref);
      }

      LayoverIntervalBlock layoverIntervalBlock = index.getLayoverIntervalBlock();

      BlockLayoverIndexData data = new BlockLayoverIndexData(references,
          layoverIntervalBlock);
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
    int logInterval = LoggingIntervalUtil.getAppropriateLoggingInterval(allIndices.size());

    Map<BlockSequenceKey, List<BlockTripEntry>> blockTripsByKey = new FactoryMap<BlockSequenceKey, List<BlockTripEntry>>(
        new ArrayList<BlockTripEntry>());

    if (_verbose)
      _log.info("grouping block trips by sequence key");

    int tripCount = 0;

    for (BlockEntry block : blocks) {

      if (block.getConfigurations().isEmpty()) {
        _log.warn("block has no active configurations: " + block.getId());
        continue;
      }

      if (BlockLibrary.isFrequencyBased(block))
        continue;

      for (BlockConfigurationEntry blockConfiguration : block.getConfigurations()) {
        for (BlockTripEntry blockTrip : blockConfiguration.getTrips()) {
          BlockSequenceKey key = getBlockTripAsTripSequenceKey(blockTrip);
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

      if (_verbose && count % logInterval == 0)
        _log.info("groups processed: " + count + "/" + blockTripsByKey.size());

      count++;

      List<List<BlockTripEntry>> groupedBlocks = BlockLibrary.createStrictlyOrderedGroups(
          tripsWithSameSequence, _blockTripLooseComparator,
          _blockTripStrictComparator);

      for (List<BlockTripEntry> group : groupedBlocks) {
        BlockTripIndex index = createTripIndexForGroupOfBlockTrips(group);
        allIndices.add(index);
      }
    }

    return allIndices;
  }

  public List<BlockLayoverIndex> createLayoverIndices(
      Iterable<BlockEntry> blocks) {

    List<BlockLayoverIndex> allIndices = new ArrayList<BlockLayoverIndex>();
    int logInterval = LoggingIntervalUtil.getAppropriateLoggingInterval(allIndices.size());

    Map<BlockLayoverSequenceKey, List<BlockTripEntry>> blockTripsByServiceIds = new FactoryMap<BlockLayoverSequenceKey, List<BlockTripEntry>>(
        new ArrayList<BlockTripEntry>());

    if (_verbose)
      _log.info("grouping block layovers by sequence key");

    int tripCount = 0;

    for (BlockEntry block : blocks) {

      if (block.getConfigurations().isEmpty()) {
        _log.warn("block has no active configurations: " + block.getId());
        continue;
      }

      if (BlockLibrary.isFrequencyBased(block))
        continue;

      for (BlockConfigurationEntry blockConfiguration : block.getConfigurations()) {
        BlockTripEntry prevTrip = null;
        for (BlockTripEntry blockTrip : blockConfiguration.getTrips()) {

          if (prevTrip != null) {
            BlockLayoverSequenceKey key = getBlockTripAsLayoverSequenceKey(blockTrip);
            blockTripsByServiceIds.get(key).add(blockTrip);
            tripCount++;
          }

          prevTrip = blockTrip;
        }

      }
    }

    if (_verbose)
      _log.info("groups found: " + blockTripsByServiceIds.size()
          + " out of trips: " + tripCount);

    int count = 0;

    for (List<BlockTripEntry> tripsWithSameSequence : blockTripsByServiceIds.values()) {

      if (_verbose && count % logInterval == 0)
        _log.info("groups processed: " + count + "/"
            + blockTripsByServiceIds.size());

      count++;

      List<List<BlockTripEntry>> groupedBlocks = ensureLayoverGroups(tripsWithSameSequence);

      for (List<BlockTripEntry> group : groupedBlocks) {
        BlockLayoverIndex index = createLayoverIndexForGroupOfBlockTrips(group);
        allIndices.add(index);
      }
    }

    return allIndices;
  }

  public List<FrequencyBlockTripIndex> createFrequencyTripIndices(
      Iterable<BlockEntry> blocks) {

    List<FrequencyBlockTripIndex> allIndices = new ArrayList<FrequencyBlockTripIndex>();
    int logInterval = LoggingIntervalUtil.getAppropriateLoggingInterval(allIndices.size());

    Map<BlockSequenceKey, List<BlockTripEntry>> blockTripsByKey = new FactoryMap<BlockSequenceKey, List<BlockTripEntry>>(
        new ArrayList<BlockTripEntry>());

    if (_verbose)
      _log.info("grouping frequency blocks by sequence key");

    int tripCount = 0;

    for (BlockEntry block : blocks) {

      if (block.getConfigurations().isEmpty()) {
        _log.warn("block has no configurations: " + block.getId());
        continue;
      }

      if (!BlockLibrary.isFrequencyBased(block))
        continue;

      for (BlockConfigurationEntry blockConfiguration : block.getConfigurations()) {
        for (BlockTripEntry blockTrip : blockConfiguration.getTrips()) {
          BlockSequenceKey key = getBlockTripAsTripSequenceKey(blockTrip);
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

      if (_verbose && count % logInterval == 0)
        _log.info("frequency groups processed: " + count + "/"
            + blockTripsByKey.size());

      count++;

      List<FrequencyTripGroup> groupedTrips = ensureFrequencyTripGroups(tripsWithSameSequence);

      for (FrequencyTripGroup group : groupedTrips) {
        group.trimToSize();
        FrequencyBlockTripIndex index = createFrequencyIndexForTrips(
            group.getTrips(), group.getFrequencies());
        allIndices.add(index);
      }
    }

    return allIndices;
  }

  public List<BlockSequenceIndex> createSequenceIndices(
      Iterable<BlockEntry> blocks) {

    List<BlockSequenceIndex> allIndices = new ArrayList<BlockSequenceIndex>();

    Map<BlockSequenceKey, List<BlockSequence>> blockSequencesByKey = new FactoryMap<BlockSequenceKey, List<BlockSequence>>(
        new ArrayList<BlockSequence>());

    if (_verbose)
      _log.info("grouping block trips into sequence indices");

    int sequenceCount = 0;
    int blockTripCount = 0;

    for (BlockEntry block : blocks) {

      if (block.getConfigurations().isEmpty()) {
        _log.warn("block has no active configurations: " + block.getId());
        continue;
      }

      if (BlockLibrary.isFrequencyBased(block))
        continue;

      for (BlockConfigurationEntry blockConfiguration : block.getConfigurations()) {
        blockTripCount += blockConfiguration.getTrips().size();
        List<BlockSequence> sequences = groupTrips(blockConfiguration);
        for (BlockSequence sequence : sequences) {
          BlockSequenceKey key = getBlockSequenceAsSequenceKey(sequence);
          blockSequencesByKey.get(key).add(sequence);
          sequenceCount++;
        }
      }
    }

    if (_verbose)
      _log.info("groups found: " + blockSequencesByKey.size()
          + " out of sequences: " + sequenceCount + " and trips: "
          + blockTripCount);

    for (List<BlockSequence> sequences : blockSequencesByKey.values()) {

      List<List<BlockSequence>> groupedBlocks = BlockLibrary.createStrictlyOrderedGroups(
          sequences, _blockSequenceLooseComparator,
          _blockSequenceStrictComparator);

      for (List<BlockSequence> group : groupedBlocks) {
        BlockSequenceIndex index = createSequenceIndexForGroupOfBlockSequences(group);
        allIndices.add(index);
      }
    }

    return allIndices;
  }

  /****
   * 
   ****/

  public BlockTripIndex createTripIndexForGroupOfBlockTrips(
      List<BlockTripEntry> blocks) {
    ServiceIntervalBlock serviceIntervalBlock = getBlockStopTimesAsBlockInterval(blocks);
    return new BlockTripIndex(blocks, serviceIntervalBlock);
  }

  public BlockLayoverIndex createLayoverIndexForGroupOfBlockTrips(
      List<BlockTripEntry> trips) {
    LayoverIntervalBlock layoverIntervalBlock = getBlockTripsAsLayoverInterval(trips);
    return new BlockLayoverIndex(trips, layoverIntervalBlock);
  }

  public FrequencyBlockTripIndex createFrequencyIndexForTrips(
      List<BlockTripEntry> trips, List<FrequencyEntry> frequencies) {
    FrequencyServiceIntervalBlock serviceIntervalBlock = getBlockTripsAsFrequencyBlockInterval(
        trips, frequencies);
    return new FrequencyBlockTripIndex(trips, frequencies, serviceIntervalBlock);
  }

  public BlockSequenceIndex createSequenceIndexForGroupOfBlockSequences(
      List<BlockSequence> sequences) {
    ServiceIntervalBlock serviceIntervalBlock = getBlockStopTimesAsBlockInterval(sequences);
    String agencyId = sequences.get(0).getBlockConfig().getBlock().getId().getAgencyId();
    boolean privateService = _privateAgencyIds.contains(agencyId);
    return new BlockSequenceIndex(sequences, serviceIntervalBlock,
        privateService);
  }

  private BlockSequenceKey getBlockTripAsTripSequenceKey(
      BlockTripEntry blockTrip) {
    List<AgencyAndId> stopIds = new ArrayList<AgencyAndId>();
    for (StopTimeEntry stopTime : blockTrip.getTrip().getStopTimes())
      stopIds.add(stopTime.getStop().getId());
    return new BlockSequenceKey(
        blockTrip.getBlockConfiguration().getServiceIds(), stopIds);
  }

  private BlockSequenceKey getBlockSequenceAsSequenceKey(BlockSequence sequence) {
    List<AgencyAndId> stopIds = new ArrayList<AgencyAndId>();
    for (BlockStopTimeEntry stopTime : sequence.getStopTimes())
      stopIds.add(stopTime.getStopTime().getStop().getId());
    return new BlockSequenceKey(sequence.getBlockConfig().getServiceIds(),
        stopIds);
  }

  private BlockLayoverSequenceKey getBlockTripAsLayoverSequenceKey(
      BlockTripEntry blockTrip) {
    AgencyAndId firstStopId = blockTrip.getTrip().getStopTimes().get(0).getStop().getId();
    return new BlockLayoverSequenceKey(
        blockTrip.getBlockConfiguration().getServiceIds(), firstStopId);
  }

  private List<List<BlockTripEntry>> ensureLayoverGroups(
      List<BlockTripEntry> blockTrips) {

    Collections.sort(blockTrips, _blockLayoverComparator);

    List<List<BlockTripEntry>> lists = new ArrayList<List<BlockTripEntry>>();

    for (BlockTripEntry block : blockTrips) {
      List<BlockTripEntry> list = getBestLayoverList(lists, block);
      if (list == null) {
        list = new ArrayList<BlockTripEntry>();
        lists.add(list);
      }
      list.add(block);
    }

    return lists;
  }

  private List<BlockTripEntry> getBestLayoverList(
      List<List<BlockTripEntry>> lists, BlockTripEntry trip) {

    for (List<BlockTripEntry> list : lists) {

      if (list.isEmpty())
        return list;

      BlockTripEntry prev = list.get(list.size() - 1);

      boolean allGood = areLayoverTimesAlwaysIncreasingOrEqual(prev, trip);

      if (allGood)
        return list;
    }

    return null;
  }

  private boolean areLayoverTimesAlwaysIncreasingOrEqual(BlockTripEntry tripA,
      BlockTripEntry tripB) {

    int layoverStartA = BlockTripLayoverTimeComparator.getLayoverStartTimeForTrip(tripA);
    int layoverEndA = BlockTripLayoverTimeComparator.getLayoverEndTimeForTrip(tripA);

    int layoverStartB = BlockTripLayoverTimeComparator.getLayoverStartTimeForTrip(tripB);
    int layoverEndB = BlockTripLayoverTimeComparator.getLayoverEndTimeForTrip(tripB);

    return layoverStartA <= layoverStartB && layoverEndA <= layoverEndB;
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

  private <T extends HasBlockStopTimes> ServiceIntervalBlock getBlockStopTimesAsBlockInterval(
      List<T> entries) {

    int n = entries.size();

    int[] minArrivals = new int[n];
    int[] minDepartures = new int[n];
    int[] maxArrivals = new int[n];
    int[] maxDepartures = new int[n];

    int index = 0;

    for (T entry : entries) {

      ServiceInterval interval = null;

      List<BlockStopTimeEntry> stopTimes = entry.getStopTimes();
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

  private LayoverIntervalBlock getBlockTripsAsLayoverInterval(
      List<BlockTripEntry> trips) {

    int n = trips.size();

    int[] startTimes = new int[n];
    int[] endTimes = new int[n];

    int index = 0;

    for (BlockTripEntry trip : trips) {

      startTimes[index] = BlockTripLayoverTimeComparator.getLayoverStartTimeForTrip(trip);
      endTimes[index] = BlockTripLayoverTimeComparator.getLayoverEndTimeForTrip(trip);

      index++;
    }

    return new LayoverIntervalBlock(startTimes, endTimes);
  }

  private FrequencyServiceIntervalBlock getBlockTripsAsFrequencyBlockInterval(
      List<BlockTripEntry> trips, List<FrequencyEntry> frequencies) {

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

  private List<BlockSequence> groupTrips(BlockConfigurationEntry blockConfig) {

    List<BlockTripEntry> trips = blockConfig.getTrips();

    if (trips.isEmpty())
      return Collections.emptyList();

    List<List<BlockTripEntry>> groups = new ArrayList<List<BlockTripEntry>>();

    List<BlockTripEntry> group = new ArrayList<BlockTripEntry>();
    groups.add(group);

    BlockTripEntry prev = null;
    for (BlockTripEntry trip : trips) {
      if (prev != null) {

        if (!areBlockTripsContinuous(prev, trip)) {
          group = new ArrayList<BlockTripEntry>();
          groups.add(group);
        }
      }

      group.add(trip);

      prev = trip;
    }

    List<BlockSequence> sequences = new ArrayList<BlockSequence>();
    for (List<BlockTripEntry> g : groups) {

      BlockTripEntry firstTrip = g.get(0);
      BlockTripEntry lastTrip = g.get(g.size() - 1);

      int from = firstTrip.getAccumulatedStopTimeIndex();
      int to = blockConfig.getStopTimes().size();
      if (lastTrip.getNextTrip() != null) {
        BlockTripEntry next = lastTrip.getNextTrip();
        to = next.getAccumulatedStopTimeIndex();
      }

      BlockSequence sequence = new BlockSequence(blockConfig, from, to);
      sequences.add(sequence);
    }

    return sequences;
  }

  private boolean areBlockTripsContinuous(BlockTripEntry prevBlockTrip,
      BlockTripEntry nextBlockTrip) {

    List<BlockStopTimeEntry> prevStopTimes = prevBlockTrip.getStopTimes();
    List<BlockStopTimeEntry> nextStopTimes = nextBlockTrip.getStopTimes();

    BlockStopTimeEntry from = prevStopTimes.get(prevStopTimes.size() - 1);
    BlockStopTimeEntry to = nextStopTimes.get(0);

    int slack = to.getAccumulatedSlackTime()
        - (from.getAccumulatedSlackTime() + from.getStopTime().getSlackTime());

    int schedTime = to.getStopTime().getArrivalTime()
        - from.getStopTime().getDepartureTime();

    /**
     * If the slack time is too much, the trips are not continuous
     */
    if (slack >= _maxSlackBetweenConsecutiveTrips)
      return false;

    /**
     * If the sched time is too much, the trips are not continuous
     */
    if (schedTime >= _maxScheduledTimeBetweenConsecutiveTrips)
      return false;

    TripEntry prevTrip = prevBlockTrip.getTrip();
    TripEntry nextTrip = nextBlockTrip.getTrip();

    AgencyAndId lineIdA = prevTrip.getRouteCollection().getId();
    AgencyAndId lineIdB = nextTrip.getRouteCollection().getId();

    String directionA = prevTrip.getDirectionId();
    String directionB = nextTrip.getDirectionId();

    /**
     * If the route has not changed, but the direction has, the trips are not
     * continuous
     */
    if (lineIdA.equals(lineIdB)
        && (directionA == null || !directionA.equals(directionB)))
      return false;

    double prevOrientation = computeDirectionOfTravel(prevStopTimes);
    double nextOrientation = computeDirectionOfTravel(nextStopTimes);
    double delta = GeometryLibrary.getAngleDifference(prevOrientation,
        nextOrientation);

    // System.out.println(delta + " " + prevTrip.getId() + " " +
    // nextTrip.getId());

    return true;
  }

  private double computeDirectionOfTravel(List<BlockStopTimeEntry> bsts) {
    BlockStopTimeEntry fromBst = bsts.get(0);
    BlockStopTimeEntry toBst = bsts.get(bsts.size() - 1);
    StopEntry fromStop = fromBst.getStopTime().getStop();
    StopEntry toStop = toBst.getStopTime().getStop();
    return SphericalGeometryLibrary.getOrientation(fromStop.getStopLat(),
        fromStop.getStopLon(), toStop.getStopLat(), toStop.getStopLon());
  }

  private ServiceInterval extend(ServiceInterval interval,
      StopTimeEntry stopTime) {
    if (interval == null)
      return new ServiceInterval(stopTime.getArrivalTime(),
          stopTime.getDepartureTime());
    return interval.extend(stopTime.getArrivalTime(),
        stopTime.getDepartureTime());
  }

  /****
   * Internal Classes
   ****/

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
