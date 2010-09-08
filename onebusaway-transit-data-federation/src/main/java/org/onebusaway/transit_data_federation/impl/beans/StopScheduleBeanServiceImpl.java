package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import org.onebusaway.collections.Counter;
import org.onebusaway.collections.FactoryMap;
import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopCalendarDayBean;
import org.onebusaway.transit_data.model.StopCalendarDaysBean;
import org.onebusaway.transit_data.model.StopRouteDirectionScheduleBean;
import org.onebusaway.transit_data.model.StopRouteScheduleBean;
import org.onebusaway.transit_data.model.StopTimeInstanceBean;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopScheduleBeanService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndex;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.utility.text.NaturalStringOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class StopScheduleBeanServiceImpl implements StopScheduleBeanService {

  private static StopTimeBeanComparator _stopTimeComparator = new StopTimeBeanComparator();

  private static DirectionComparator _directionComparator = new DirectionComparator();

  private static StopRouteScheduleBeanComparator _stopRouteScheduleComparator = new StopRouteScheduleBeanComparator();

  private AgencyService _agencyService;

  private TransitGraphDao _graph;

  private CalendarService _calendarService;

  private RouteBeanService _routeBeanService;

  private NarrativeService _narrativeService;

  @Autowired
  public void setAgencyService(AgencyService agencyService) {
    _agencyService = agencyService;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao graph) {
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

  @Autowired
  public void setNarrativeService(NarrativeService narrativeService) {
    _narrativeService = narrativeService;
  }

  @Cacheable
  public StopCalendarDaysBean getCalendarForStop(AgencyAndId stopId) {

    TimeZone timeZone = _agencyService.getTimeZoneForAgencyId(stopId.getAgencyId());

    StopEntry stopEntry = _graph.getStopEntryForId(stopId);
    StopTimeIndex index = stopEntry.getStopTimes();

    Set<LocalizedServiceId> serviceIds = index.getServiceIds();

    SortedMap<ServiceDate, Set<AgencyAndId>> serviceIdsByDate = getServiceIdsByDate(serviceIds);

    Counter<Set<AgencyAndId>> counts = new Counter<Set<AgencyAndId>>();
    for (Set<AgencyAndId> ids : serviceIdsByDate.values())
      counts.increment(ids);

    int total = counts.size();
    Map<Set<AgencyAndId>, Integer> idsToGroup = new HashMap<Set<AgencyAndId>, Integer>();
    for (Set<AgencyAndId> ids : counts.getSortedKeys())
      idsToGroup.put(ids, total--);

    List<StopCalendarDayBean> beans = new ArrayList<StopCalendarDayBean>(
        serviceIdsByDate.size());
    for (Map.Entry<ServiceDate, Set<AgencyAndId>> entry : serviceIdsByDate.entrySet()) {
      StopCalendarDayBean bean = new StopCalendarDayBean();
      ServiceDate serviceDate = entry.getKey();
      Date date = serviceDate.getAsDate(timeZone);
      bean.setDate(date);
      Integer indexId = idsToGroup.get(entry.getValue());
      bean.setGroup(indexId);
      beans.add(bean);
    }

    return new StopCalendarDaysBean(timeZone.getID(), beans);
  }

  @Transactional
  @Cacheable
  public List<StopRouteScheduleBean> getScheduledArrivalsForStopAndDate(
      AgencyAndId stopId, ServiceDate date) {

    StopEntry stopEntry = _graph.getStopEntryForId(stopId);
    StopTimeIndex index = stopEntry.getStopTimes();

    Map<AgencyAndId, List<StopTimeInstanceProxy>> stopTimesByRouteCollectionId = new FactoryMap<AgencyAndId, List<StopTimeInstanceProxy>>(
        new ArrayList<StopTimeInstanceProxy>());

    Set<AgencyAndId> serviceIds = _calendarService.getServiceIdsOnDate(date);
    Set<LocalizedServiceId> localizedServiceIds = new HashSet<LocalizedServiceId>();
    for (LocalizedServiceId serviceId : index.getServiceIds()) {
      if (serviceIds.contains(serviceId.getId()))
        localizedServiceIds.add(serviceId);
    }

    for (LocalizedServiceId serviceId : localizedServiceIds) {

      Date serviceDate = date.getAsDate(serviceId.getTimeZone());

      List<StopTimeEntry> stopTimes = index.getStopTimesForServiceIdSortedByDeparture(serviceId);

      for (StopTimeEntry stopTime : stopTimes) {

        TripEntry trip = stopTime.getTrip();
        AgencyAndId routeCollectionId = trip.getRouteCollectionId();

        StopTimeInstanceProxy sti = new StopTimeInstanceProxy(stopTime,
            serviceDate);

        stopTimesByRouteCollectionId.get(routeCollectionId).add(sti);
      }
    }

    List<StopRouteScheduleBean> beans = new ArrayList<StopRouteScheduleBean>();

    for (Map.Entry<AgencyAndId, List<StopTimeInstanceProxy>> entry : stopTimesByRouteCollectionId.entrySet()) {

      AgencyAndId routeCollectionId = entry.getKey();
      List<StopTimeInstanceProxy> stopTimesForRoute = entry.getValue();

      StopRouteScheduleBean routeScheduleBean = new StopRouteScheduleBean();
      beans.add(routeScheduleBean);

      RouteBean route = _routeBeanService.getRouteForId(routeCollectionId);
      routeScheduleBean.setRoute(route);

      Map<String, StopTimeByDirectionEntry> stopTimesByDirection = new FactoryMap<String, StopTimeByDirectionEntry>(
          new StopTimeByDirectionEntry());

      for (StopTimeInstanceProxy sti : stopTimesForRoute) {

        TripEntry trip = sti.getTrip();

        AgencyAndId tripId = trip.getId();
        AgencyAndId serviceId = trip.getServiceId();

        StopTimeInstanceBean stiBean = new StopTimeInstanceBean();
        stiBean.setTripId(AgencyAndIdLibrary.convertToString(tripId));
        stiBean.setArrivalTime(sti.getArrivalTime());
        stiBean.setDepartureTime(sti.getDepartureTime());
        stiBean.setServiceId(AgencyAndIdLibrary.convertToString(serviceId));

        TripNarrative narrative = _narrativeService.getTripForId(tripId);

        String directionId = narrative.getDirectionId();
        if (directionId == null)
          directionId = "0";

        StopTimeByDirectionEntry stopTimesForDirection = stopTimesByDirection.get(directionId);
        stopTimesForDirection.addEntry(stiBean, narrative.getTripHeadsign());
      }

      for (StopTimeByDirectionEntry stopTimesForDirection : stopTimesByDirection.values()) {

        StopRouteDirectionScheduleBean directionBean = new StopRouteDirectionScheduleBean();
        directionBean.getStopTimes().addAll(
            stopTimesForDirection.getStopTimes());
        String headsign = stopTimesForDirection.getBestHeadsign();
        directionBean.setTripHeadsign(headsign);

        Collections.sort(directionBean.getStopTimes(), _stopTimeComparator);

        routeScheduleBean.getDirections().add(directionBean);
      }

      Collections.sort(routeScheduleBean.getDirections(), _directionComparator);
    }

    Collections.sort(beans, _stopRouteScheduleComparator);

    return beans;
  }

  /****
   * Private Methods
   ****/

  private SortedMap<ServiceDate, Set<AgencyAndId>> getServiceIdsByDate(
      Set<LocalizedServiceId> serviceIds) {

    SortedMap<ServiceDate, Set<AgencyAndId>> serviceIdsByDate = new TreeMap<ServiceDate, Set<AgencyAndId>>();
    serviceIdsByDate = FactoryMap.createSorted(serviceIdsByDate,
        new HashSet<AgencyAndId>());

    for (LocalizedServiceId serviceId : serviceIds) {
      AgencyAndId id = serviceId.getId();
      Set<ServiceDate> dates = _calendarService.getServiceDatesForServiceId(id);
      for (ServiceDate date : dates) {
        serviceIdsByDate.get(date).add(id);
      }
    }
    return serviceIdsByDate;
  }

  private static class StopTimeBeanComparator implements
      Comparator<StopTimeInstanceBean> {

    public int compare(StopTimeInstanceBean o1, StopTimeInstanceBean o2) {
      long t1 = o1.getDepartureTime();
      long t2 = o2.getDepartureTime();
      return new Long(t1).compareTo(new Long(t2));
    }
  }

  private static class StopRouteScheduleBeanComparator implements
      Comparator<StopRouteScheduleBean> {

    public int compare(StopRouteScheduleBean o1, StopRouteScheduleBean o2) {
      String a = getNameForRoute(o1.getRoute());
      String b = getNameForRoute(o2.getRoute());
      return NaturalStringOrder.compareNatural(a, b);
    }

    private static String getNameForRoute(RouteBean route) {
      String name = route.getShortName();
      if (name == null)
        name = route.getLongName();
      if (name == null)
        name = route.getId();
      return name;
    }
  }

  private static class DirectionComparator implements
      Comparator<StopRouteDirectionScheduleBean> {
    @Override
    public int compare(StopRouteDirectionScheduleBean o1,
        StopRouteDirectionScheduleBean o2) {
      String tripA = o1.getTripHeadsign();
      String tripB = o2.getTripHeadsign();
      return tripA.compareTo(tripB);
    }
  }

  public static class StopTimeByDirectionEntry {

    private List<StopTimeInstanceBean> _stopTimes = new ArrayList<StopTimeInstanceBean>();

    private Counter<String> _headsigns = new Counter<String>();

    public Collection<? extends StopTimeInstanceBean> getStopTimes() {
      return _stopTimes;
    }

    public void addEntry(StopTimeInstanceBean sti, String headsign) {
      _stopTimes.add(sti);
      _headsigns.increment(headsign);
    }

    public String getBestHeadsign() {
      return _headsigns.getMax();
    }
  }
}
