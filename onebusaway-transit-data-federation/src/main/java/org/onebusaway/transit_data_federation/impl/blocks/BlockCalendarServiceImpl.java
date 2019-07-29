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
package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.collections.Min;
import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data.model.blocks.BlockInstanceBean;
import org.onebusaway.transit_data_federation.services.ExtendedCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockLayoverIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.blocks.InstanceState;
import org.onebusaway.transit_data_federation.services.blocks.LayoverIntervalBlock;
import org.onebusaway.transit_data_federation.services.blocks.ServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class BlockCalendarServiceImpl implements BlockCalendarService {

  private ExtendedCalendarService _calendarService;

  private BlockIndexService _blockIndexService;

  private TransitGraphDao _transitGraphDao;

  @Autowired
  public void setCalendarService(ExtendedCalendarService calendarService) {
    _calendarService = calendarService;
  }

  @Autowired
  public void setBlockIndexService(BlockIndexService blockIndexService) {
    _blockIndexService = blockIndexService;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  /****
   * {@link BlockCalendarService} Interface
   ****/

  @Cacheable(isValueSerializable = false)
  @Override
  public BlockInstance getBlockInstance(AgencyAndId blockId, long serviceDate) {

    BlockEntry block = _transitGraphDao.getBlockEntryForId(blockId);

    if (block == null)
      throw new IllegalArgumentException("unknown block: " + blockId);

    List<BlockConfigurationEntry> configurations = block.getConfigurations();
    int index = 0;

    Date date = new Date(serviceDate);
    InstanceState state = new InstanceState(serviceDate);

    /**
     * See the specific contract for {@link BlockEntry#getConfigurations()}
     * about the sort order of configurations
     */
    for (BlockConfigurationEntry configuration : configurations) {
      if (allServiceIdsAreActiveForServiceDate(configuration, date)) {
        return new BlockInstance(configuration, state);
      }

      index++;
    }

    return null;
  }

  @Override
  public List<BlockInstance> getActiveBlocks(AgencyAndId blockId,
      long timeFrom, long timeTo) {

    List<BlockTripIndex> indices = _blockIndexService.getBlockTripIndicesForBlock(blockId);
    List<BlockLayoverIndex> layoverIndices = _blockIndexService.getBlockLayoverIndicesForBlock(blockId);
    List<FrequencyBlockTripIndex> frequencyIndices = _blockIndexService.getFrequencyBlockTripIndicesForBlock(blockId);

    return getActiveBlocksInTimeRange(indices, layoverIndices,
        frequencyIndices, timeFrom, timeTo);
  }

  @Override
  public List<BlockInstance> getClosestActiveBlocks(AgencyAndId blockId,
      long time) {

    Date timeAsDate = new Date(time);

    Min<BlockInstance> m = new Min<BlockInstance>();

    BlockEntry blockEntry = _transitGraphDao.getBlockEntryForId(blockId);
    for (BlockConfigurationEntry blockConfig : blockEntry.getConfigurations()) {
      List<Date> serviceDates = _calendarService.getDatesForServiceIdsAsOrderedList(blockConfig.getServiceIds());

      int index = index(Collections.binarySearch(serviceDates, timeAsDate));

      if (index > 0) {
        BlockInstance instance = new BlockInstance(blockConfig,
            serviceDates.get(index - 1).getTime());
        long delta = getTimeToBlockInstance(instance, time);
        m.add(delta, instance);
      }

      if (index < serviceDates.size()) {
        BlockInstance instance = new BlockInstance(blockConfig,
            serviceDates.get(index).getTime());
        long delta = getTimeToBlockInstance(instance, time);
        m.add(delta, instance);
      }
    }

    return m.getMinElements();
  }

  @Override
  public List<BlockInstance> getActiveBlocksInTimeRange(long timeFrom,
      long timeTo) {
    List<BlockTripIndex> indices = _blockIndexService.getBlockTripIndices();
    List<BlockLayoverIndex> layoverIndices = _blockIndexService.getBlockLayoverIndices();
    List<FrequencyBlockTripIndex> frequencyIndices = _blockIndexService.getFrequencyBlockTripIndices();
    return getActiveBlocksInTimeRange(indices, layoverIndices,
        frequencyIndices, timeFrom, timeTo);
  }

  @Override
  public List<BlockInstance> getActiveBlocksForAgencyInTimeRange(
      String agencyId, long timeFrom, long timeTo) {
    List<BlockTripIndex> indices = _blockIndexService.getBlockTripIndicesForAgencyId(agencyId);
    List<BlockLayoverIndex> layoverIndices = _blockIndexService.getBlockLayoverIndicesForAgencyId(agencyId);
    List<FrequencyBlockTripIndex> frequencyIndices = _blockIndexService.getFrequencyBlockTripIndicesForAgencyId(agencyId);
    return getActiveBlocksInTimeRange(indices, layoverIndices,
        frequencyIndices, timeFrom, timeTo);
  }

  @Override
  public List<BlockInstance> getActiveBlocksForRouteInTimeRange(
      AgencyAndId routeId, long timeFrom, long timeTo) {
    List<BlockTripIndex> indices = _blockIndexService.getBlockTripIndicesForRouteCollectionId(routeId);
    List<BlockLayoverIndex> layoverIndices = _blockIndexService.getBlockLayoverIndicesForRouteCollectionId(routeId);
    List<FrequencyBlockTripIndex> frequencyIndices = _blockIndexService.getFrequencyBlockTripIndicesForRouteCollectionId(routeId);
    return getActiveBlocksInTimeRange(indices, layoverIndices,
        frequencyIndices, timeFrom, timeTo);
  }

  @Override
  public List<BlockInstance> getActiveBlocksInTimeRange(
      Iterable<BlockTripIndex> indices,
      Iterable<BlockLayoverIndex> layoverIndices,
      Iterable<FrequencyBlockTripIndex> frequencyIndices, long timeFrom,
      long timeTo) {

    Set<BlockInstance> instances = new HashSet<BlockInstance>();

    for (BlockTripIndex index : indices)
      getActiveBlocksInTimeRange(index, timeFrom, timeTo, instances);

    for (BlockLayoverIndex index : layoverIndices)
      getActiveLayoversInTimeRange(index, timeFrom, timeTo, instances);

    for (FrequencyBlockTripIndex index : frequencyIndices)
      getActiveFrequencyBlocksInTimeRange(index, timeFrom, timeTo, instances);

    return new ArrayList<BlockInstance>(instances);
  }

  /****
   * Private Methods
   ****/

  private boolean allServiceIdsAreActiveForServiceDate(
      BlockConfigurationEntry configuration, Date serviceDate) {

    Set<Date> serviceDates = _calendarService.getDatesForServiceIds(configuration.getServiceIds());
    return serviceDates.contains(serviceDate);
  }

  /****
   * 
   ****/

  private void getActiveBlocksInTimeRange(BlockTripIndex index, long timeFrom,
      long timeTo, Collection<BlockInstance> results) {

    Date dateFrom = new Date(timeFrom);
    Date dateTo = new Date(timeTo);

    handleBlockIndex(index, dateFrom, dateTo, results);
  }

  private Collection<BlockInstance> handleBlockIndex(BlockTripIndex index,
      Date timeFrom, Date timeTo, Collection<BlockInstance> instances) {

    List<BlockTripEntry> trips = index.getTrips();

    ServiceIntervalBlock serviceIntervalBlock = index.getServiceIntervalBlock();
    ServiceInterval serviceInterval = serviceIntervalBlock.getRange();

    Collection<Date> serviceDates = _calendarService.getServiceDatesWithinRange(
        index.getServiceIds(), serviceInterval, timeFrom, timeTo);

    for (Date serviceDate : serviceDates) {

      findBlockTripsInRange(serviceIntervalBlock, serviceDate, timeFrom,
          timeTo, trips, instances);
    }

    return instances;
  }

  private void findBlockTripsInRange(ServiceIntervalBlock intervals,
      Date serviceDate, Date timeFrom, Date timeTo, List<BlockTripEntry> trips,
      Collection<BlockInstance> instances) {

    int scheduledTimeFrom = (int) ((timeFrom.getTime() - serviceDate.getTime()) / 1000);
    int scheduledTimeTo = (int) ((timeTo.getTime() - serviceDate.getTime()) / 1000);

    int indexFrom = index(Arrays.binarySearch(intervals.getMaxDepartures(),
        scheduledTimeFrom));
    int indexTo = index(Arrays.binarySearch(intervals.getMinArrivals(),
        scheduledTimeTo));
    
    InstanceState state = new InstanceState(serviceDate.getTime());

    for (int in = indexFrom; in < indexTo; in++) {
      BlockTripEntry trip = trips.get(in);
      BlockConfigurationEntry block = trip.getBlockConfiguration();
      BlockInstance instance = new BlockInstance(block, state);
      instances.add(instance);
    }
  }

  /****
   * 
   ****/

  private void getActiveLayoversInTimeRange(BlockLayoverIndex index,
      long timeFrom, long timeTo, Collection<BlockInstance> results) {

    Date dateFrom = new Date(timeFrom);
    Date dateTo = new Date(timeTo);

    handleLayoverIndex(index, dateFrom, dateTo, results);
  }

  private Collection<BlockInstance> handleLayoverIndex(BlockLayoverIndex index,
      Date timeFrom, Date timeTo, Collection<BlockInstance> instances) {

    List<BlockTripEntry> trips = index.getTrips();

    LayoverIntervalBlock layoverIntervalBlock = index.getLayoverIntervalBlock();
    ServiceInterval serviceInterval = layoverIntervalBlock.getRange();

    Collection<Date> serviceDates = _calendarService.getServiceDatesWithinRange(
        index.getServiceIds(), serviceInterval, timeFrom, timeTo);

    for (Date serviceDate : serviceDates) {

      findBlockLayoversInRange(layoverIntervalBlock, serviceDate, timeFrom,
          timeTo, trips, instances);
    }

    return instances;
  }

  private void findBlockLayoversInRange(LayoverIntervalBlock intervals,
      Date serviceDate, Date timeFrom, Date timeTo, List<BlockTripEntry> trips,
      Collection<BlockInstance> instances) {

    int scheduledTimeFrom = (int) ((timeFrom.getTime() - serviceDate.getTime()) / 1000);
    int scheduledTimeTo = (int) ((timeTo.getTime() - serviceDate.getTime()) / 1000);

    int indexFrom = index(Arrays.binarySearch(intervals.getEndTimes(),
        scheduledTimeFrom));
    int indexTo = index(Arrays.binarySearch(intervals.getStartTimes(),
        scheduledTimeTo));
    
    InstanceState state = new InstanceState(serviceDate.getTime()); 

    for (int in = indexFrom; in < indexTo; in++) {
      BlockTripEntry trip = trips.get(in);
      BlockConfigurationEntry block = trip.getBlockConfiguration();
      BlockInstance instance = new BlockInstance(block, state);
      instances.add(instance);
    }
  }

  /****
   * Frequency Block Indices
   ****/

  private void getActiveFrequencyBlocksInTimeRange(
      FrequencyBlockTripIndex index, long timeFrom, long timeTo,
      Collection<BlockInstance> results) {

    Date dateFrom = new Date(timeFrom);
    Date dateTo = new Date(timeTo);

    handleFrequencyBlockIndex(index, dateFrom, dateTo, results);
  }

  private Collection<BlockInstance> handleFrequencyBlockIndex(
      FrequencyBlockTripIndex index, Date timeFrom, Date timeTo,
      Collection<BlockInstance> instances) {

    List<BlockTripEntry> trips = index.getTrips();
    List<FrequencyEntry> frequencies = index.getFrequencies();

    FrequencyServiceIntervalBlock serviceIntervalBlock = index.getServiceIntervalBlock();
    ServiceInterval serviceInterval = serviceIntervalBlock.getRange();

    Collection<Date> serviceDates = _calendarService.getServiceDatesWithinRange(
        index.getServiceIds(), serviceInterval, timeFrom, timeTo);

    for (Date serviceDate : serviceDates) {

      findFrequencyBlockTripsInRange(serviceIntervalBlock, serviceDate,
          timeFrom, timeTo, trips, frequencies, instances);
    }

    return instances;
  }

  private void findFrequencyBlockTripsInRange(
      FrequencyServiceIntervalBlock serviceIntervalIndex, Date serviceDate,
      Date timeFrom, Date timeTo, List<BlockTripEntry> trips,
      List<FrequencyEntry> frequencies, Collection<BlockInstance> instances) {

    int scheduledTimeFrom = (int) ((timeFrom.getTime() - serviceDate.getTime()) / 1000);
    int scheduledTimeTo = (int) ((timeTo.getTime() - serviceDate.getTime()) / 1000);

    int indexFrom = index(Arrays.binarySearch(
        serviceIntervalIndex.getEndTimes(), scheduledTimeFrom));
    int indexTo = index(Arrays.binarySearch(
        serviceIntervalIndex.getStartTimes(), scheduledTimeTo));

    for (int in = indexFrom; in < indexTo; in++) {
      BlockTripEntry trip = trips.get(in);
      BlockConfigurationEntry block = trip.getBlockConfiguration();
      FrequencyEntry frequency = frequencies.get(in);
      InstanceState state = new InstanceState(serviceDate.getTime(), frequency);
      BlockInstance instance = new BlockInstance(block, state);
      instances.add(instance);
    }
  }

  /****
   * 
   ****/

  private long getTimeToBlockInstance(BlockInstance instance, long time) {
    long serviceDate = instance.getServiceDate();
    BlockConfigurationEntry blockConfig = instance.getBlock();
    int n = blockConfig.getStopTimes().size();
    long from = serviceDate + blockConfig.getArrivalTimeForIndex(0) * 1000;
    long to = serviceDate + blockConfig.getDepartureTimeForIndex(n - 1) * 1000;
    return Math.abs((from + to) / 2 - time);
  }

  /****
   * 
   ****/

  private int index(int index) {
    if (index < 0)
      return -(index + 1);
    return index;
  }

}
