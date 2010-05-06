package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data_federation.impl.time.StopTimeSearchOperations;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeOp;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndex;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
class StopTimeServiceImpl implements StopTimeService {

  private CalendarService _calendarService;

  private TripPlannerGraph _graph;

  @Autowired
  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  @Autowired
  public void setTripPlannerGraph(TripPlannerGraph graph) {
    _graph = graph;
  }

  public List<StopTimeInstanceProxy> getStopTimeInstancesInTimeRange(
      AgencyAndId stopId, Date from, Date to) {

    StopEntry stopEntry = _graph.getStopEntryForId(stopId);
    StopTimeIndex index = stopEntry.getStopTimes();

    Set<AgencyAndId> serviceIds = index.getServiceIds();

    Map<AgencyAndId, List<Date>> serviceDates = _calendarService.getServiceDatesWithinRange(
        serviceIds, from, to);
    serviceIds = serviceDates.keySet();

    return getStopTimesForStopAndServiceIdsAndTimeRange(index, serviceDates,
        from, to);
  }

  private List<StopTimeInstanceProxy> getStopTimesForStopAndServiceIdsAndTimeRange(
      StopTimeIndex index, Map<AgencyAndId, List<Date>> serviceDates,
      Date from, Date to) {

    List<StopTimeInstanceProxy> results = new ArrayList<StopTimeInstanceProxy>();

    for (Map.Entry<AgencyAndId, List<Date>> entry : serviceDates.entrySet()) {

      AgencyAndId serviceId = entry.getKey();

      List<StopTimeEntry> byArrival = index.getStopTimesForServiceIdSortedByArrival(serviceId);
      List<StopTimeEntry> byDeparture = index.getStopTimesForServiceIdSortedByDeparture(serviceId);

      if (byArrival.isEmpty() && byDeparture.isEmpty())
        continue;

      for (Date serviceDate : entry.getValue()) {

        int relativeFrom = time(serviceDate, from);
        int relativeTo = time(serviceDate, to);

        Set<StopTimeEntry> hits = new HashSet<StopTimeEntry>();

        search(byArrival, StopTimeOp.ARRIVAL, relativeFrom, relativeTo, hits);
        search(byDeparture, StopTimeOp.DEPARTURE, relativeFrom, relativeTo, hits);

        for (StopTimeEntry stopTime : hits)
          results.add(new StopTimeInstanceProxy(stopTime, serviceDate));
      }
    }

    return results;
  }

  private void search(List<StopTimeEntry> stopTimes, StopTimeOp stopTimeOp,
      int relativeFrom, int relativeTo, Set<StopTimeEntry> hits) {
    int fromIndex = StopTimeSearchOperations.searchForStopTime(stopTimes, relativeFrom, stopTimeOp);
    int toIndex = StopTimeSearchOperations.searchForStopTime(stopTimes, relativeTo, stopTimeOp);
    for (int i = fromIndex; i < toIndex; i++)
      hits.add(stopTimes.get(i));
  }

  private int time(Date serviceDate, Date targetDate) {
    return (int) ((targetDate.getTime() - serviceDate.getTime()) / 1000);
  }
}
