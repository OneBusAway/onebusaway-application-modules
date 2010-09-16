package org.onebusaway.transit_data_federation.impl.realtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.Min;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceIdIntervals;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockIndicesFactory;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.ServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.realtime.ActiveCalendarService;
import org.onebusaway.transit_data_federation.services.realtime.BlockInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ActiveCalendarServiceImpl implements ActiveCalendarService {

  private CalendarService _calendarService;

  private BlockIndexService _blockIndexService;

  private TransitGraphDao _transitGraphDao;

  @Autowired
  public void setCalendarService(CalendarService calendarService) {
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
   * {@link ActiveCalendarService} Interface
   ****/

  @Override
  public BlockInstance getActiveBlock(AgencyAndId blockId, long serviceDate,
      long time) {

    List<BlockInstance> blockInstances = getActiveBlocks(blockId, time, time);
    
    if (blockInstances.isEmpty())
      return null;
    else if (blockInstances.size() == 1)
      return blockInstances.get(0);

    Min<BlockInstance> m = new Min<BlockInstance>();

    for (BlockInstance blockInstance : blockInstances) {
      long someServiceDate = blockInstance.getServiceDate();
      double delta = Math.abs(someServiceDate - serviceDate);
      m.add(delta, blockInstance);
    }

    return m.getMinElement();
  }
  
  @Override
  public List<BlockInstance> getActiveBlocks(AgencyAndId blockId,
      long timeFrom, long timeTo) {
    
    BlockEntry block = _transitGraphDao.getBlockEntryForId(blockId);

    if (block == null)
      return null;

    BlockIndicesFactory factory = new BlockIndicesFactory();
    factory.setCalendarService(_calendarService);
    BlockIndex index = factory.createIndex(block);

    List<BlockInstance> blockInstances = new ArrayList<BlockInstance>();

    getActiveBlocksInTimeRange(index, timeFrom, timeTo, blockInstances);

    return blockInstances;
  }

  @Override
  public List<BlockInstance> getActiveBlocksForAgencyInTimeRange(
      String agencyId, long timeFrom, long timeTo) {
    List<BlockIndex> indices = _blockIndexService.getBlockIndicesForAgencyId(agencyId);
    return getActiveBlocksInTimeRange(indices, timeFrom, timeTo);
  }

  @Override
  public List<BlockInstance> getActiveBlocksForRouteInTimeRange(
      AgencyAndId routeId, long timeFrom, long timeTo) {
    List<BlockIndex> indices = _blockIndexService.getBlockIndicesForRouteCollectionId(routeId);
    return getActiveBlocksInTimeRange(indices, timeFrom, timeTo);
  }

  @Override
  public List<BlockInstance> getActiveBlocksInTimeRange(
      List<BlockIndex> indices, long timeFrom, long timeTo) {
    List<BlockInstance> instances = new ArrayList<BlockInstance>();
    for (BlockIndex index : indices)
      getActiveBlocksInTimeRange(index, timeFrom, timeTo, instances);
    return instances;
  }

  /****
   * Internal Methods
   *****/

  private void getActiveBlocksInTimeRange(BlockIndex index, long timeFrom,
      long timeTo, List<BlockInstance> results) {

    Date dateFrom = new Date(timeFrom);
    Date dateTo = new Date(timeTo);
    
    ServiceIdIntervals serviceIdIntervals = index.getServiceIdIntervals();
    Set<LocalizedServiceId> serviceIds = serviceIdIntervals.getServiceIds();
    if (serviceIds.size() == 1)
      handleBlockIndexWithSingleServiceId(index, dateFrom, dateTo, results);
    else
      handleBlockIndexWithMultipleServiceIds(index, dateFrom, dateTo, results);
  }

  private List<BlockInstance> handleBlockIndexWithSingleServiceId(
      BlockIndex index, Date timeFrom, Date timeTo,
      List<BlockInstance> instances) {

    List<BlockEntry> blocks = index.getBlocks();

    ServiceIdIntervals serviceIdIntervals = index.getServiceIdIntervals();
    Set<LocalizedServiceId> allServiceIds = serviceIdIntervals.getServiceIds();

    Map<LocalizedServiceId, List<Date>> serviceDatesWithinRange = _calendarService.getServiceDatesWithinRange(
        serviceIdIntervals, timeFrom, timeTo);

    for (Map.Entry<LocalizedServiceId, List<Date>> entry : serviceDatesWithinRange.entrySet()) {
      LocalizedServiceId serviceId = entry.getKey();
      for (Date serviceDate : entry.getValue()) {

        ServiceIntervalBlock from = index.getIntervalForServiceId(serviceId);
        ServiceIntervalBlock to = from;

        findBlocksInRange(from, to, serviceDate, timeFrom, timeTo, blocks,
            instances, allServiceIds, true);
      }
    }

    return instances;
  }

  private List<BlockInstance> handleBlockIndexWithMultipleServiceIds(
      BlockIndex index, Date timeFrom, Date timeTo,
      List<BlockInstance> instances) {

    List<BlockEntry> blocks = index.getBlocks();

    ServiceIdIntervals serviceIdIntervals = index.getServiceIdIntervals();
    Set<LocalizedServiceId> allServiceIds = serviceIdIntervals.getServiceIds();

    Map<LocalizedServiceId, List<Date>> serviceDatesWithinRange = _calendarService.getServiceDatesWithinRange(
        serviceIdIntervals, timeFrom, timeTo);
    Map<Date, Set<LocalizedServiceId>> byDate = getServiceIdsByDate(serviceDatesWithinRange);

    for (Map.Entry<Date, Set<LocalizedServiceId>> entry : byDate.entrySet()) {

      Date serviceDate = entry.getKey();

      Set<LocalizedServiceId> serviceIds = entry.getValue();

      ServiceIntervalBlock from = null;
      ServiceIntervalBlock to = null;

      for (LocalizedServiceId serviceId : serviceIds) {
        ServiceIntervalBlock block = index.getIntervalForServiceId(serviceId);
        if (from == null || from.compareTo(block) > 0)
          from = block;
        if (to == null || to.compareTo(block) < 0)
          to = block;
      }

      Set<LocalizedServiceId> activeServiceIds = getAdditionalServiceIdsActiveOnDate(
          allServiceIds, serviceIds, serviceDate);

      boolean allServiceIdsAreActive = activeServiceIds.size() == allServiceIds.size();

      findBlocksInRange(from, to, serviceDate, timeFrom, timeTo, blocks,
          instances, activeServiceIds, allServiceIdsAreActive);
    }

    return instances;
  }

  /****
   * Private Methods
   ****/

  private void findBlocksInRange(ServiceIntervalBlock intervalsFrom,
      ServiceIntervalBlock intervalsTo, Date serviceDate, Date timeFrom,
      Date timeTo, List<BlockEntry> blocks, List<BlockInstance> instances,
      Set<LocalizedServiceId> activeServiceIds, boolean allServiceIdsAreActive) {

    int scheduledTimeFrom = (int) ((timeFrom.getTime() - serviceDate.getTime()) / 1000);
    int scheduledTimeTo = (int) ((timeTo.getTime() - serviceDate.getTime()) / 1000);

    int indexFrom = index(Arrays.binarySearch(intervalsTo.getMaxDepartures(),
        scheduledTimeFrom));
    int indexTo = index(Arrays.binarySearch(intervalsFrom.getMinArrivals(),
        scheduledTimeTo));

    for (int in = indexFrom; in < indexTo; in++) {
      BlockEntry block = blocks.get(in);
      BlockInstance instance = new BlockInstance(block, serviceDate.getTime(),
          activeServiceIds, allServiceIdsAreActive);
      instances.add(instance);
    }
  }

  private int index(int index) {
    if (index < 0)
      return -(index + 1);
    return index;
  }

  private Set<LocalizedServiceId> getAdditionalServiceIdsActiveOnDate(
      Set<LocalizedServiceId> allServiceIds,
      Set<LocalizedServiceId> currentServiceIds, Date serviceDate) {

    Set<LocalizedServiceId> allActiveServiceIds = currentServiceIds;

    if (currentServiceIds.size() < allServiceIds.size()) {
      allActiveServiceIds = new HashSet<LocalizedServiceId>(currentServiceIds);
      for (LocalizedServiceId serviceId : allServiceIds) {
        if (allActiveServiceIds.contains(serviceId))
          continue;
        if (_calendarService.isLocalizedServiceIdActiveOnDate(serviceId,
            serviceDate))
          allActiveServiceIds.add(serviceId);
      }
    }
    return allActiveServiceIds;
  }

  private Map<Date, Set<LocalizedServiceId>> getServiceIdsByDate(
      Map<LocalizedServiceId, List<Date>> serviceDatesWithinRange) {
    Map<Date, Set<LocalizedServiceId>> serviceDatesByDate = new FactoryMap<Date, Set<LocalizedServiceId>>(
        new HashSet<LocalizedServiceId>());

    for (Map.Entry<LocalizedServiceId, List<Date>> entry : serviceDatesWithinRange.entrySet()) {
      LocalizedServiceId lsid = entry.getKey();
      for (Date date : entry.getValue())
        serviceDatesByDate.get(date).add(lsid);
    }
    return serviceDatesByDate;
  }

}
