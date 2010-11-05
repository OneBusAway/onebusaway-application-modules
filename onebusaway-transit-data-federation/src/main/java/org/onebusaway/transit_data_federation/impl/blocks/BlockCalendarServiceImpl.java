package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.services.ExtendedCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.blocks.ServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
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

    /**
     * See the specific contract for {@link BlockEntry#getConfigurations()}
     * about the sort order of configurations
     */
    for (BlockConfigurationEntry configuration : configurations) {
      if (allServiceIdsAreActiveForServiceDate(configuration, date)) {
        return new BlockInstance(configuration, serviceDate);
      }

      index++;
    }

    return null;
  }

  @Override
  public List<BlockInstance> getActiveBlocks(AgencyAndId blockId,
      long timeFrom, long timeTo) {

    List<BlockIndex> indices = _blockIndexService.getBlockIndicesForBlock(blockId);
    List<FrequencyBlockIndex> frequencyIndices = _blockIndexService.getFrequencyBlockIndicesForBlock(blockId);
    
    return getActiveBlocksInTimeRange(indices, frequencyIndices, timeFrom,
        timeTo);
  }

  @Override
  public List<BlockInstance> getActiveBlocksForAgencyInTimeRange(
      String agencyId, long timeFrom, long timeTo) {
    List<BlockIndex> indices = _blockIndexService.getBlockIndicesForAgencyId(agencyId);
    List<FrequencyBlockIndex> frequencyIndices = _blockIndexService.getFrequencyBlockIndicesForAgencyId(agencyId);
    return getActiveBlocksInTimeRange(indices, frequencyIndices, timeFrom,
        timeTo);
  }

  @Override
  public List<BlockInstance> getActiveBlocksForRouteInTimeRange(
      AgencyAndId routeId, long timeFrom, long timeTo) {
    List<BlockIndex> indices = _blockIndexService.getBlockIndicesForRouteCollectionId(routeId);
    List<FrequencyBlockIndex> frequencyIndices = _blockIndexService.getFrequencyBlockIndicesForRouteCollectionId(routeId);
    return getActiveBlocksInTimeRange(indices, frequencyIndices, timeFrom,
        timeTo);
  }

  @Override
  public List<BlockInstance> getActiveBlocksInTimeRange(
      Iterable<BlockIndex> indices,
      Iterable<FrequencyBlockIndex> frequencyIndices, long timeFrom, long timeTo) {
    List<BlockInstance> instances = new ArrayList<BlockInstance>();
    for (BlockIndex index : indices)
      getActiveBlocksInTimeRange(index, timeFrom, timeTo, instances);
    for (FrequencyBlockIndex index : frequencyIndices)
      getActiveFrequencyBlocksInTimeRange(index, timeFrom, timeTo, instances);
    return instances;
  }

  /****
   * Private Methods
   ****/

  private boolean allServiceIdsAreActiveForServiceDate(
      BlockConfigurationEntry configuration, Date serviceDate) {

    Set<Date> serviceDates = _calendarService.getDatesForServiceIds(configuration.getServiceIds());
    return serviceDates.contains(serviceDate);
  }

  private void getActiveBlocksInTimeRange(BlockIndex index, long timeFrom,
      long timeTo, List<BlockInstance> results) {

    Date dateFrom = new Date(timeFrom);
    Date dateTo = new Date(timeTo);

    handleBlockIndex(index, dateFrom, dateTo, results);
  }

  private List<BlockInstance> handleBlockIndex(BlockIndex index, Date timeFrom,
      Date timeTo, List<BlockInstance> instances) {

    List<BlockConfigurationEntry> blocks = index.getBlocks();

    ServiceIntervalBlock serviceIntervalBlock = index.getServiceIntervalBlock();
    ServiceInterval serviceInterval = serviceIntervalBlock.getRange();

    Collection<Date> serviceDates = _calendarService.getServiceDatesWithinRange(
        index.getServiceIds(), serviceInterval, timeFrom, timeTo);

    for (Date serviceDate : serviceDates) {

      findBlocksInRange(serviceIntervalBlock, serviceDate, timeFrom, timeTo,
          blocks, instances);
    }

    return instances;
  }

  private void findBlocksInRange(ServiceIntervalBlock intervals,
      Date serviceDate, Date timeFrom, Date timeTo,
      List<BlockConfigurationEntry> blocks, List<BlockInstance> instances) {

    int scheduledTimeFrom = (int) ((timeFrom.getTime() - serviceDate.getTime()) / 1000);
    int scheduledTimeTo = (int) ((timeTo.getTime() - serviceDate.getTime()) / 1000);

    int indexFrom = index(Arrays.binarySearch(intervals.getMaxDepartures(),
        scheduledTimeFrom));
    int indexTo = index(Arrays.binarySearch(intervals.getMinArrivals(),
        scheduledTimeTo));

    for (int in = indexFrom; in < indexTo; in++) {
      BlockConfigurationEntry block = blocks.get(in);
      BlockInstance instance = new BlockInstance(block, serviceDate.getTime());
      instances.add(instance);
    }
  }

  /****
   * Frequency Block Indices
   ****/

  private void getActiveFrequencyBlocksInTimeRange(FrequencyBlockIndex index,
      long timeFrom, long timeTo, List<BlockInstance> results) {

    Date dateFrom = new Date(timeFrom);
    Date dateTo = new Date(timeTo);

    handleFrequencyBlockIndex(index, dateFrom, dateTo, results);
  }

  private List<BlockInstance> handleFrequencyBlockIndex(
      FrequencyBlockIndex index, Date timeFrom, Date timeTo,
      List<BlockInstance> instances) {

    List<BlockConfigurationEntry> blocks = index.getBlocks();
    List<FrequencyEntry> frequencies = index.getFrequencies();

    FrequencyServiceIntervalBlock serviceIntervalBlock = index.getServiceIntervalBlock();
    ServiceInterval serviceInterval = serviceIntervalBlock.getRange();

    Collection<Date> serviceDates = _calendarService.getServiceDatesWithinRange(
        index.getServiceIds(), serviceInterval, timeFrom, timeTo);

    for (Date serviceDate : serviceDates) {

      findFrequencyBlocksInRange(serviceIntervalBlock, serviceDate, timeFrom,
          timeTo, blocks, frequencies, instances);
    }

    return instances;
  }

  private void findFrequencyBlocksInRange(
      FrequencyServiceIntervalBlock serviceIntervalBlock, Date serviceDate,
      Date timeFrom, Date timeTo, List<BlockConfigurationEntry> blocks,
      List<FrequencyEntry> frequencies, List<BlockInstance> instances) {

    int scheduledTimeFrom = (int) ((timeFrom.getTime() - serviceDate.getTime()) / 1000);
    int scheduledTimeTo = (int) ((timeTo.getTime() - serviceDate.getTime()) / 1000);

    int indexFrom = index(Arrays.binarySearch(
        serviceIntervalBlock.getEndTimes(), scheduledTimeFrom));
    int indexTo = index(Arrays.binarySearch(
        serviceIntervalBlock.getStartTimes(), scheduledTimeTo));

    for (int in = indexFrom; in < indexTo; in++) {
      BlockConfigurationEntry block = blocks.get(in);
      FrequencyEntry frequency = frequencies.get(in);
      BlockInstance instance = new BlockInstance(block, serviceDate.getTime(),
          frequency);
      instances.add(instance);
    }
  }

  private int index(int index) {
    if (index < 0)
      return -(index + 1);
    return index;
  }

}
