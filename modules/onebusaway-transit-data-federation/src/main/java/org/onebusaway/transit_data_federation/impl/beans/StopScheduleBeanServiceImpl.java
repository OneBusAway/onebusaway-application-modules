package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopCalendarDayBean;
import org.onebusaway.transit_data.model.StopRouteScheduleBean;
import org.onebusaway.transit_data.model.StopTimeInstanceBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopScheduleBeanService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndex;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;
import org.onebusaway.utility.text.NaturalStringOrder;

import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.stats.Counter;
import edu.washington.cs.rse.text.DateLibrary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
class StopScheduleBeanServiceImpl implements StopScheduleBeanService {

  private static StopTimeBeanComparator _stopTimeComparator = new StopTimeBeanComparator();

  private static StopRouteScheduleBeanComparator _stopRouteScheduleComparator = new StopRouteScheduleBeanComparator();

  private TripPlannerGraph _graph;

  private CalendarService _calendarService;

  private RouteBeanService _routeBeanService;

  @Autowired
  public void setTripPlannerGraph(TripPlannerGraph graph) {
    _graph = graph;
  }

  @Autowired
  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  @Autowired
  public void setRouteBeanService(RouteBeanService routeBeanService) {
    _routeBeanService = routeBeanService;
  }

  @Cacheable
  public List<StopCalendarDayBean> getCalendarForStop(AgencyAndId stopId) {

    StopEntry stopEntry = _graph.getStopEntryForId(stopId);
    StopTimeIndex index = stopEntry.getStopTimes();

    Set<AgencyAndId> serviceIds = index.getServiceIds();

    SortedMap<Date, Set<AgencyAndId>> serviceIdsByDate = getServiceIdsByDate(serviceIds);

    Counter<Set<AgencyAndId>> counts = new Counter<Set<AgencyAndId>>();
    for (Set<AgencyAndId> ids : serviceIdsByDate.values())
      counts.increment(ids);
    int total = counts.size();
    Map<Set<AgencyAndId>, Integer> idsToGroup = new HashMap<Set<AgencyAndId>, Integer>();
    for (Set<AgencyAndId> ids : counts.getSortedKeys())
      idsToGroup.put(ids, total--);

    List<StopCalendarDayBean> beans = new ArrayList<StopCalendarDayBean>(
        serviceIdsByDate.size());
    for (Map.Entry<Date, Set<AgencyAndId>> entry : serviceIdsByDate.entrySet()) {
      StopCalendarDayBean bean = new StopCalendarDayBean();
      bean.setDate(entry.getKey());
      Integer indexId = idsToGroup.get(entry.getValue());
      bean.setGroup(indexId);
      beans.add(bean);
    }

    return beans;
  }

  @Transactional
  @Cacheable
  public List<StopRouteScheduleBean> getScheduledArrivalsForStopAndDate(
      AgencyAndId stopId, Date date) {

    date = DateLibrary.getTimeAsDay(date);

    StopEntry stopEntry = _graph.getStopEntryForId(stopId);
    StopTimeIndex index = stopEntry.getStopTimes();

    Map<AgencyAndId, List<StopTimeEntry>> stopTimesByRouteCollectionId = new FactoryMap<AgencyAndId, List<StopTimeEntry>>(
        new ArrayList<StopTimeEntry>());

    Set<AgencyAndId> serviceIds = _calendarService.getServiceIdsOnDate(date);
    serviceIds.retainAll(index.getServiceIds());

    for (AgencyAndId serviceId : serviceIds) {
      List<StopTimeEntry> stopTimes = index.getStopTimesForServiceIdSortedByDeparture(serviceId);
      for (StopTimeEntry stopTime : stopTimes) {
        TripEntry trip = stopTime.getTrip();
        AgencyAndId routeCollectionId = trip.getRouteCollectionId();
        stopTimesByRouteCollectionId.get(routeCollectionId).add(stopTime);
      }
    }

    List<StopRouteScheduleBean> beans = new ArrayList<StopRouteScheduleBean>();

    for (Map.Entry<AgencyAndId, List<StopTimeEntry>> entry : stopTimesByRouteCollectionId.entrySet()) {

      AgencyAndId routeCollectionId = entry.getKey();
      List<StopTimeEntry> stopTimesForRoute = entry.getValue();

      StopRouteScheduleBean routeScheduleBean = new StopRouteScheduleBean();
      beans.add(routeScheduleBean);

      RouteBean route = _routeBeanService.getRouteForId(routeCollectionId);
      routeScheduleBean.setRoute(route);

      for (StopTimeEntry stopTime : stopTimesForRoute) {

        StopTimeInstanceProxy sti = new StopTimeInstanceProxy(stopTime, date);

        StopTimeInstanceBean stiBean = new StopTimeInstanceBean();
        AgencyAndId tripId = stopTime.getTrip().getId();
        stiBean.setTripId(AgencyAndIdLibrary.convertToString(tripId));
        stiBean.setDepartureDate(new Date(sti.getDepartureTime()));

        routeScheduleBean.getStopTimes().add(stiBean);

      }

      Collections.sort(routeScheduleBean.getStopTimes(), _stopTimeComparator);
    }

    Collections.sort(beans, _stopRouteScheduleComparator);

    return beans;
  }

  /****
   * Private Methods
   ****/

  private SortedMap<Date, Set<AgencyAndId>> getServiceIdsByDate(
      Set<AgencyAndId> serviceIds) {

    SortedMap<Date, Set<AgencyAndId>> serviceIdsByDate = new TreeMap<Date, Set<AgencyAndId>>();
    serviceIdsByDate = FactoryMap.createSorted(serviceIdsByDate,
        new HashSet<AgencyAndId>());

    for (AgencyAndId serviceId : serviceIds) {
      Set<Date> dates = _calendarService.getServiceDatesForServiceId(serviceId);
      for (Date date : dates) {
        serviceIdsByDate.get(date).add(serviceId);
      }
    }
    return serviceIdsByDate;
  }

  private static class StopTimeBeanComparator implements
      Comparator<StopTimeInstanceBean> {

    public int compare(StopTimeInstanceBean o1, StopTimeInstanceBean o2) {
      Date t1 = o1.getDepartureDate();
      Date t2 = o2.getDepartureDate();
      return t1.compareTo(t2);
    }
  }

  private static class StopRouteScheduleBeanComparator implements
      Comparator<StopRouteScheduleBean> {

    public int compare(StopRouteScheduleBean o1, StopRouteScheduleBean o2) {
      String a = o1.getRoute().getShortName();
      String b = o2.getRoute().getShortName();
      return NaturalStringOrder.compareNatural(a, b);
    }
  }
}
