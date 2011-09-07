package org.onebusaway.transit_data_federation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.blocks.BlockStopTimeArrivalTimeIndexAdapter;
import org.onebusaway.transit_data_federation.impl.blocks.BlockStopTimeDepartureTimeIndexAdapter;
import org.onebusaway.transit_data_federation.impl.blocks.FrequencyEndTimeIndexAdapter;
import org.onebusaway.transit_data_federation.impl.blocks.FrequencyStartTimeIndexAdapter;
import org.onebusaway.transit_data_federation.impl.blocks.GenericBinarySearch;
import org.onebusaway.transit_data_federation.services.ExtendedCalendarService;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyBlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class StopTimeServiceImpl implements StopTimeService {

  private TransitGraphDao _graph;

  private ExtendedCalendarService _calendarService;

  private BlockIndexService _blockIndexService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao graph) {
    _graph = graph;
  }

  @Autowired
  public void setCalendarService(ExtendedCalendarService calendarService) {
    _calendarService = calendarService;
  }

  @Autowired
  public void setBlockIndexService(BlockIndexService blockIndexService) {
    _blockIndexService = blockIndexService;
  }

  @Override
  public List<StopTimeInstance> getStopTimeInstancesInTimeRange(
      AgencyAndId stopId, Date from, Date to) {

    StopEntry stopEntry = _graph.getStopEntryForId(stopId);
    return getStopTimeInstancesInRange(from, to, stopEntry);
  }

  @Override
  public List<StopTimeInstance> getStopTimeInstancesInRange(Date from, Date to,
      StopEntry stopEntry) {

    List<StopTimeInstance> stopTimeInstances = new ArrayList<StopTimeInstance>();

    for (BlockStopTimeIndex index : _blockIndexService.getStopTimeIndicesForStop(stopEntry)) {
      Collection<Date> serviceDates = _calendarService.getServiceDatesWithinRange(
          index.getServiceIds(), index.getServiceInterval(), from, to);
      getStopTimesForStopAndServiceIdsAndTimeRange(index, serviceDates, from,
          to, stopTimeInstances);
    }

    for (FrequencyBlockStopTimeIndex index : _blockIndexService.getFrequencyStopTimeIndicesForStop(stopEntry)) {
      Collection<Date> serviceDates = _calendarService.getServiceDatesWithinRange(
          index.getServiceIds(), index.getServiceInterval(), from, to);
      getFrequenciesForStopAndServiceIdsAndTimeRange(index, serviceDates, from,
          to, stopTimeInstances);
    }

    return stopTimeInstances;
  }

  /****
   * Private Methods
   ****/

  private void getStopTimesForStopAndServiceIdsAndTimeRange(
      BlockStopTimeIndex index, Collection<Date> serviceDates, Date from,
      Date to, List<StopTimeInstance> stopTimeInstances) {

    List<BlockStopTimeEntry> blockStopTimes = index.getStopTimes();

    for (Date serviceDate : serviceDates) {

      int relativeFrom = time(serviceDate, from);
      int relativeTo = time(serviceDate, to);

      int fromIndex = GenericBinarySearch.search(index, blockStopTimes.size(),
          relativeFrom, BlockStopTimeDepartureTimeIndexAdapter.INSTANCE);
      int toIndex = GenericBinarySearch.search(index, blockStopTimes.size(),
          relativeTo, BlockStopTimeArrivalTimeIndexAdapter.INSTANCE);

      for (int in = fromIndex; in < toIndex; in++) {
        BlockStopTimeEntry blockStopTime = blockStopTimes.get(in);
        stopTimeInstances.add(new StopTimeInstance(blockStopTime, serviceDate));
      }
    }

  }

  private void getFrequenciesForStopAndServiceIdsAndTimeRange(
      FrequencyBlockStopTimeIndex index, Collection<Date> serviceDates,
      Date from, Date to, List<StopTimeInstance> stopTimeInstances) {

    for (Date serviceDate : serviceDates) {

      int relativeFrom = time(serviceDate, from);
      int relativeTo = time(serviceDate, to);

      int fromIndex = GenericBinarySearch.search(index, index.size(),
          relativeFrom, FrequencyEndTimeIndexAdapter.INSTANCE);
      int toIndex = GenericBinarySearch.search(index, index.size(), relativeTo,
          FrequencyStartTimeIndexAdapter.INSTANCE);

      List<FrequencyBlockStopTimeEntry> frequencyStopTimes = index.getFrequencyStopTimes();
      for (int in = fromIndex; in < toIndex; in++) {
        FrequencyBlockStopTimeEntry entry = frequencyStopTimes.get(in);
        stopTimeInstances.add(new StopTimeInstance(entry.getStopTime(),
            serviceDate.getTime(), entry.getFrequency()));
      }
    }
  }

  private int time(Date serviceDate, Date targetDate) {
    return (int) ((targetDate.getTime() - serviceDate.getTime()) / 1000);
  }
}
