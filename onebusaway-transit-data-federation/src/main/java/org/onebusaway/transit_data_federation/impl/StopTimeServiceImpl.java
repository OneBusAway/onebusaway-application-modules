package org.onebusaway.transit_data_federation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.blocks.BlockStopTimeArrivalTimeValueAdapter;
import org.onebusaway.transit_data_federation.impl.blocks.BlockStopTimeDepartureTimeValueAdapter;
import org.onebusaway.transit_data_federation.impl.time.GenericBinarySearch;
import org.onebusaway.transit_data_federation.services.ExtendedCalendarService;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndexService;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class StopTimeServiceImpl implements StopTimeService {

  private TransitGraphDao _graph;

  private ExtendedCalendarService _calendarService;

  private BlockStopTimeIndexService _blockStopTimeIndexService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao graph) {
    _graph = graph;
  }

  @Autowired
  public void setCalendarService(ExtendedCalendarService calendarService) {
    _calendarService = calendarService;
  }

  @Autowired
  public void setBlockStopTimeIndexService(
      BlockStopTimeIndexService blockStopTimeIndexService) {
    _blockStopTimeIndexService = blockStopTimeIndexService;
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

    for (BlockStopTimeIndex index : _blockStopTimeIndexService.getStopTimeIndicesForStop(stopEntry)) {
      Collection<Date> serviceDates = _calendarService.getServiceDatesWithinRange(
          index.getServiceIds(), index.getServiceInterval(), from, to);
      getStopTimesForStopAndServiceIdsAndTimeRange(index, serviceDates, from,
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

      int fromIndex = GenericBinarySearch.search(blockStopTimes, relativeFrom,
          BlockStopTimeDepartureTimeValueAdapter.INSTANCE);
      int toIndex = GenericBinarySearch.search(blockStopTimes, relativeTo,
          BlockStopTimeArrivalTimeValueAdapter.INSTANCE);

      for (int in = fromIndex; in < toIndex; in++) {
        BlockStopTimeEntry blockStopTime = blockStopTimes.get(in);
        stopTimeInstances.add(new StopTimeInstance(blockStopTime, serviceDate));
      }
    }

  }

  private int time(Date serviceDate, Date targetDate) {
    return (int) ((targetDate.getTime() - serviceDate.getTime()) / 1000);
  }
}
