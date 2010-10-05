package org.onebusaway.transit_data_federation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.onebusaway.transit_data_federation.services.tripplanner.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class StopTimeServiceImpl implements StopTimeService {

  private static Logger _log = LoggerFactory.getLogger(StopTimeServiceImpl.class);

  private TransitGraphDao _graph;

  private ExtendedCalendarService _calendarService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao graph) {
    _graph = graph;
  }

  @Autowired
  public void setCalendarService(ExtendedCalendarService calendarService) {
    _calendarService = calendarService;
  }

  @Override
  public List<StopTimeInstanceProxy> getStopTimeInstancesInTimeRange(
      AgencyAndId stopId, Date from, Date to) {

    StopEntry stopEntry = _graph.getStopEntryForId(stopId);
    return getStopTimeInstancesInRange(from, to, stopEntry);
  }

  @Override
  public List<StopTimeInstanceProxy> getStopTimeInstancesInRange(Date from,
      Date to, StopEntry stopEntry) {
    List<StopTimeInstanceProxy> stopTimeInstances = new ArrayList<StopTimeInstanceProxy>();

    for (BlockStopTimeIndex index : stopEntry.getStopTimeIndices()) {
      Collection<Date> serviceDates = _calendarService.getServiceDatesWithinRange(
          index.getServiceIds(), index.getServiceInterval(), from, to);
      getStopTimesForStopAndServiceIdsAndTimeRange(index, serviceDates, from,
          to, stopTimeInstances);
    }

    return stopTimeInstances;
  }

  @Override
  public List<StopTimeInstanceProxy> getPreviousStopTimeArrival(
      StopEntry stopEntry, long targetTime) {
    _log.warn("todo: implement me");
    return Collections.emptyList();
  }
  

  @Override
  public List<StopTimeInstanceProxy> getNextStopTimeDeparture(
      StopEntry stopEntry, long currentTime) {
    _log.warn("todo: implement me");
    return Collections.emptyList();
  }

  /****
   * Private Methods
   ****/

  private void getStopTimesForStopAndServiceIdsAndTimeRange(
      BlockStopTimeIndex index, Collection<Date> serviceDates, Date from,
      Date to, List<StopTimeInstanceProxy> stopTimeInstances) {

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
        stopTimeInstances.add(new StopTimeInstanceProxy(blockStopTime,
            serviceDate));
      }
    }

  }

  private int time(Date serviceDate, Date targetDate) {
    return (int) ((targetDate.getTime() - serviceDate.getTime()) / 1000);
  }

  
  /*

  public StopTimeIndexResult getNextStopTime(StopTimeIndex stopIndex,
      StopTimeIndexContext context, long targetTime) {

    Map<LocalizedServiceId, List<Date>> _serviceDates = context.getNextServiceDates(
        stopIndex.getServiceIdIntervals(), targetTime);

    Min<StopTimeInstanceProxy> m = new Min<StopTimeInstanceProxy>();

    for (Map.Entry<LocalizedServiceId, List<Date>> entry : _serviceDates.entrySet()) {

      LocalizedServiceId serviceId = entry.getKey();
      List<StopTimeEntry> stopTimes = stopIndex.getStopTimesForServiceIdSortedByDeparture(serviceId);

      if (stopTimes.isEmpty())
        continue;

      for (Date serviceDate : entry.getValue()) {

        int index = searchNext(stopTimes, 0, stopTimes.size(), serviceDate,
            targetTime);

        if (index < 0 || index >= stopTimes.size())
          continue;

        while (index > 0
            && _op.getValue(stopTimes.get(index)) == _op.getValue(stopTimes.get(index - 1)))
          index--;

        double previousTime = -1;

        while (0 <= index && index < stopTimes.size()) {
          StopTimeInstanceProxy sti = new StopTimeInstanceProxy(
              stopTimes.get(index), serviceDate);
          double stiTime = _op.getValue(sti);
          if (previousTime == -1)
            previousTime = stiTime;
          if (previousTime != stiTime)
            break;
          double delta = stiTime - targetTime;
          m.add(delta, sti);
          index++;
        }
      }
    }

    return new StopTimeIndexResult(m.getMinElements(), null);
  }

  public StopTimeIndexResult getPreviousStopTime(StopTimeIndex stopIndex,
      StopTimeIndexContext context, long targetTime) {

    Map<LocalizedServiceId, List<Date>> serviceDates = context.getPreviousServiceDates(
        stopIndex.getServiceIdIntervals(), targetTime);

    Min<StopTimeInstanceProxy> m = new Min<StopTimeInstanceProxy>();

    for (Map.Entry<LocalizedServiceId, List<Date>> entry : serviceDates.entrySet()) {

      LocalizedServiceId serviceId = entry.getKey();
      List<StopTimeEntry> stopTimes = stopIndex.getStopTimesForServiceIdSortedByArrival(serviceId);

      if (stopTimes.isEmpty())
        continue;

      for (Date serviceDate : entry.getValue()) {

        int index = searchPrevious(stopTimes, 0, stopTimes.size(), serviceDate,
            targetTime);

        if (index == 0 || index > stopTimes.size())
          continue;

        while (index < stopTimes.size()
            && _op.getValue(stopTimes.get(index - 1)) == _op.getValue(stopTimes.get(index)))
          index++;

        double previousTime = -1;

        while (0 < index && index <= stopTimes.size()) {
          StopTimeInstanceProxy sti = new StopTimeInstanceProxy(
              stopTimes.get(index - 1), serviceDate);
          double stiTime = _op.getValue(sti);
          if (previousTime == -1)
            previousTime = stiTime;
          if (stiTime != previousTime)
            break;
          double delta = targetTime - stiTime;
          m.add(delta, sti);
          index--;
        }
      }
    }

    return new StopTimeIndexResult(m.getMinElements(), null);
  }

  private int searchNext(List<StopTimeEntry> stopTimes, int indexFrom,
      int indexTo, Date serviceDate, long targetTime) {

    if (indexTo == indexFrom)
      return indexFrom;

    int index = (indexFrom + indexTo) / 2;

    StopTimeEntry stopTime = stopTimes.get(index);
    long time = (long) (serviceDate.getTime() + _op.getValue(stopTime) * 1000);

    if (time == targetTime)
      return index;

    if (targetTime < time)
      return searchNext(stopTimes, indexFrom, index, serviceDate, targetTime);
    else
      return searchNext(stopTimes, index + 1, indexTo, serviceDate, targetTime);
  }

  private int searchPrevious(List<StopTimeEntry> stopTimes, int indexFrom,
      int indexTo, Date serviceDate, long targetTime) {

    if (indexTo == indexFrom)
      return indexFrom;

    int index = (indexFrom + indexTo + 1) / 2;

    StopTimeEntry stopTime = stopTimes.get(index - 1);
    long time = (long) (serviceDate.getTime() + _op.getValue(stopTime) * 1000);

    if (time == targetTime)
      return index;

    if (targetTime < time)
      return searchPrevious(stopTimes, indexFrom, index - 1, serviceDate,
          targetTime);
    else
      return searchPrevious(stopTimes, index, indexTo, serviceDate, targetTime);
  }
   */
}
