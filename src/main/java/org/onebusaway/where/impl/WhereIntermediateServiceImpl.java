package org.onebusaway.where.impl;

import org.onebusaway.common.graph.Graph;
import org.onebusaway.common.impl.UtilityLibrary;
import org.onebusaway.common.spring.Cacheable;
import org.onebusaway.common.web.common.client.model.PathBean;
import org.onebusaway.common.web.common.client.model.RouteBean;
import org.onebusaway.common.web.common.client.model.StopBean;
import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.CalendarService;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.where.model.StopSelectionTree;
import org.onebusaway.where.model.StopSequence;
import org.onebusaway.where.model.StopSequenceBlock;
import org.onebusaway.where.model.StopTimeInstance;
import org.onebusaway.where.services.NoSuchRouteException;
import org.onebusaway.where.services.StatusService;
import org.onebusaway.where.services.StopSelectionService;
import org.onebusaway.where.services.StopTimePredictionService;
import org.onebusaway.where.services.StopTimeService;
import org.onebusaway.where.services.WhereDao;
import org.onebusaway.where.services.WhereIntermediateService;
import org.onebusaway.where.web.common.client.model.DepartureBean;
import org.onebusaway.where.web.common.client.model.NearbyRoutesBean;
import org.onebusaway.where.web.common.client.model.StopCalendarDayBean;
import org.onebusaway.where.web.common.client.model.StopRouteScheduleBean;
import org.onebusaway.where.web.common.client.model.StopSequenceBlockBean;
import org.onebusaway.where.web.common.client.model.StopTimeBean;
import org.onebusaway.where.web.common.client.rpc.NoSuchRouteServiceException;
import org.onebusaway.where.web.common.client.rpc.NoSuchStopServiceException;

import edu.washington.cs.rse.collections.CollectionsLibrary;
import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.stats.Counter;
import edu.washington.cs.rse.collections.stats.Min;
import edu.washington.cs.rse.collections.tuple.T2;
import edu.washington.cs.rse.collections.tuple.T3;
import edu.washington.cs.rse.text.DateLibrary;
import edu.washington.cs.rse.text.NaturalStringOrder;

import com.vividsolutions.jts.geom.Geometry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
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
class WhereIntermediateServiceImpl implements WhereIntermediateService {

  private static StopTimeBeanComparator _stopTimeComparator = new StopTimeBeanComparator();

  private static StopRouteScheduleBeanComparator _stopRouteScheduleComparator = new StopRouteScheduleBeanComparator();

  private static RouteBeanComparator _routeBeanComparator = new RouteBeanComparator();

  private static StopNameComparator _stopNameComparator = new StopNameComparator();

  @Autowired
  private GtfsDao _gtfsDao;

  @Autowired
  private WhereDao _whereDao;

  @Autowired
  private CalendarService _calendarService;

  @Autowired
  private StopSelectionService _stopSelection;

  @Autowired
  private StopTimeService _stopTimeService;

  @Autowired
  private StopTimePredictionService _stopTimePredictionService;

  @Autowired
  private StatusService _statusService;

  @Cacheable
  public StopBean getStop(String stopId) throws ServiceException {
    return getStopWithNearbyStopRadius(stopId, 300);
  }

  // @Cacheable
  public StopBean getStopWithNearbyStopRadius(String stopId, double nearbyStopSearchDistance) throws ServiceException {

    Stop stop = _gtfsDao.getStopById(stopId);

    if (stop == null)
      throw new NoSuchStopServiceException(stopId);

    StopBean sb = new StopBean();
    ApplicationBeanLibrary.fillStopBean(stop, sb);
    fillNearbyStopsForStopBean(stop, sb, nearbyStopSearchDistance);
    fillRoutesForStopBean(stop, sb);
    return sb;
  }

  public NearbyRoutesBean getNearbyRoutes(String stopId, double nearbyRouteSearchDistance)
      throws NoSuchStopServiceException {

    Stop stop = _gtfsDao.getStopById(stopId);
    if (stop == null)
      throw new NoSuchStopServiceException(stopId);

    Geometry envelope = stop.getLocation().buffer(nearbyRouteSearchDistance).getEnvelope();

    // This call is SLOW
    List<T3<Route, String, Stop>> tuples = _gtfsDao.getRoutesDirectionIdsAndStopsByLocation(envelope);

    Map<T2<Route, String>, Min<Stop>> mins = new FactoryMap<T2<Route, String>, Min<Stop>>(new Min<Stop>());

    for (T3<Route, String, Stop> tuple : tuples) {
      T2<Route, String> key = T2.create(tuple.getFirst(), tuple.getSecond());
      Stop nearbyStop = tuple.get3();
      double d = UtilityLibrary.distance(stop.getLocation(), nearbyStop.getLocation());
      mins.get(key).add(d, nearbyStop);
    }

    NearbyRoutesBean bean = new NearbyRoutesBean();
    Map<Stop, StopBean> stopBeans = new HashMap<Stop, StopBean>();

    for (Map.Entry<T2<Route, String>, Min<Stop>> entry : mins.entrySet()) {
      T2<Route, String> key = entry.getKey();
      Route route = key.getFirst();
      RouteBean routeBean = getRouteAsBean(route);

      Min<Stop> min = entry.getValue();
      Stop minStop = min.getMinElement();
      StopBean stopBean = stopBeans.get(minStop);
      if (stopBean == null) {
        stopBean = new StopBean();
        ApplicationBeanLibrary.fillStopBean(minStop, stopBean);
        stopBeans.put(minStop, stopBean);
      }
      bean.addRouteAndStop(routeBean, stopBean);
    }

    Collections.sort(bean.getRoutes(), _routeBeanComparator);

    for (RouteBean route : bean.getRoutes()) {
      List<StopBean> stops = bean.getNearbyStopsForRoute(route);
      Collections.sort(stops, _stopNameComparator);
    }

    return bean;
  }

  @Transactional
  @Cacheable
  public List<StopRouteScheduleBean> getScheduledArrivalsForStopAndDate(String stopId, Date date)
      throws ServiceException {

    date = DateLibrary.getTimeAsDay(date);

    Stop stop = _gtfsDao.getStopById(stopId);

    if (stop == null)
      throw new NoSuchStopServiceException(stopId);

    List<StopSequenceBlock> blocks = _whereDao.getStopSequenceBlocksByStop(stop);

    Map<Route, List<StopSequenceBlock>> blocksByRoute = CollectionsLibrary.mapToValueList(blocks, "id.route",
        Route.class);

    Set<String> serviceIds = _calendarService.getServiceIdsOnDate(date);
    List<StopTime> stopTimes = _gtfsDao.getStopTimesByStopAndServiceIds(stop, serviceIds);

    Map<Route, StopRouteScheduleBean> routeMapping = new HashMap<Route, StopRouteScheduleBean>();

    for (StopTime stopTime : stopTimes) {
      StopTimeInstance sti = new StopTimeInstance(stopTime, date);
      StopRouteScheduleBean routeBean = getRouteBeanForStopTime(routeMapping, blocksByRoute, stopTime);
      StopTimeBean stBean = new StopTimeBean();
      stBean.setTripId(stopTime.getTrip().getId());
      stBean.setDepartureDate(sti.getDepartureTime());
      stBean.setServiceId(stopTime.getTrip().getServiceId());
      routeBean.getStopTimes().add(stBean);
    }

    for (StopRouteScheduleBean bean : routeMapping.values())
      Collections.sort(bean.getStopTimes(), _stopTimeComparator);

    List<StopRouteScheduleBean> beans = new ArrayList<StopRouteScheduleBean>(routeMapping.values());
    Collections.sort(beans, _stopRouteScheduleComparator);
    return beans;
  }

  @Cacheable
  public List<StopCalendarDayBean> getCalendarForStop(String stopId) throws ServiceException {

    Stop stop = _gtfsDao.getStopById(stopId);

    if (stop == null)
      throw new NoSuchStopServiceException(stopId);

    List<String> serviceIds = _gtfsDao.getServiceIdsByStop(stop);

    SortedMap<Date, Set<String>> serviceIdsByDate = new TreeMap<Date, Set<String>>();
    serviceIdsByDate = FactoryMap.createSorted(serviceIdsByDate, new HashSet<String>());

    for (String serviceId : serviceIds) {
      Set<Date> dates = _calendarService.getDatesForServiceId(serviceId);
      for (Date date : dates) {
        serviceIdsByDate.get(date).add(serviceId);
      }
    }

    Counter<Set<String>> counts = new Counter<Set<String>>();
    for (Set<String> ids : serviceIdsByDate.values())
      counts.increment(ids);
    int total = counts.size();
    Map<Set<String>, Integer> idsToGroup = new HashMap<Set<String>, Integer>();
    for (Set<String> ids : counts.getSortedKeys())
      idsToGroup.put(ids, total--);

    List<StopCalendarDayBean> beans = new ArrayList<StopCalendarDayBean>(serviceIdsByDate.size());
    for (Map.Entry<Date, Set<String>> entry : serviceIdsByDate.entrySet()) {
      StopCalendarDayBean bean = new StopCalendarDayBean();
      bean.setDate(entry.getKey());
      Integer index = idsToGroup.get(entry.getValue());
      bean.setGroup(index);
      beans.add(bean);
    }

    return beans;
  }

  @Cacheable
  public StopSelectionTree getStopSelectionTreeForRoute(String route) throws ServiceException {
    try {
      return _stopSelection.getStopsByRoute(route);
    } catch (NoSuchRouteException e) {
      throw new NoSuchRouteServiceException();
    }
  }

  @Transactional
  @Cacheable
  public List<StopSequenceBlockBean> getStopSequenceBlocksByRoute(String routeName) throws ServiceException {

    Route route = _gtfsDao.getRouteByShortName(routeName);

    if (route == null)
      throw new NoSuchRouteServiceException();

    List<StopSequenceBlock> blocks = _whereDao.getStopSequenceBlocksByRoute(route);
    List<StopSequenceBlockBean> blockBeans = new ArrayList<StopSequenceBlockBean>(blocks.size());

    for (StopSequenceBlock block : blocks) {

      StopSequenceBlockBean bean = new StopSequenceBlockBean();

      bean.setId(block.getId().getId());
      bean.setDescription(block.getDescription());
      bean.setRoute(getRouteAsBean(route));
      bean.setStartLat(block.getStartLat());
      bean.setStartLon(block.getStartLon());
      bean.setEndLat(block.getEndLat());
      bean.setEndLon(block.getEndLon());

      Set<Stop> stops = new HashSet<Stop>();
      Set<String> shapeIds = new HashSet<String>();

      for (StopSequence sequence : block.getStopSequences()) {
        stops.addAll(sequence.getStops());
        if (sequence.getShapeId() != null)
          shapeIds.add(sequence.getShapeId());
      }

      List<StopBean> stopBeans = getStopBeansFromStops(stops);
      bean.setStops(stopBeans);

      List<PathBean> paths = getPathBeansFromShapeIds(shapeIds);
      bean.setPaths(paths);

      blockBeans.add(bean);
    }

    return blockBeans;
  }

  @Transactional
  // @Cacheable
  public List<DepartureBean> getDeparturesForStop(String stopId) throws ServiceException {

    Stop stop = _gtfsDao.getStopById(stopId);

    if (stop == null)
      throw new NoSuchStopServiceException(stopId);

    Calendar c = Calendar.getInstance();
    c.add(Calendar.MINUTE, -30);
    Date from = c.getTime();
    c.add(Calendar.MINUTE, 65);
    Date to = c.getTime();

    List<StopTimeInstance> stis = _stopTimeService.getStopTimeInstancesInTimeRange(stop, from, to);
    _stopTimePredictionService.getPredictions(stis);

    List<DepartureBean> beans = new ArrayList<DepartureBean>();
    for (StopTimeInstance sti : stis)
      beans.add(getPredictedArrivalTimeAsBean(stop, sti));

    return beans;
  }

  private void fillNearbyStopsForStopBean(Stop stop, StopBean sb, double maxDistance) {

    Geometry envelope = stop.getLocation().buffer(maxDistance).getEnvelope();
    List<Stop> stops = _gtfsDao.getStopsByLocation(envelope);

    Map<Stop, List<T2<Route, String>>> routesAndDirectionIdForStops = _gtfsDao.getRoutesAndDirectionIdsForStops(stops);
    Map<T2<Route, String>, Min<Stop>> mins = new FactoryMap<T2<Route, String>, Min<Stop>>(new Min<Stop>());

    for (Map.Entry<Stop, List<T2<Route, String>>> entry : routesAndDirectionIdForStops.entrySet()) {

      Stop nearbyStop = entry.getKey();

      if (nearbyStop.equals(stop))
        continue;

      double d = UtilityLibrary.distance(stop.getLocation(), nearbyStop.getLocation());
      for (T2<Route, String> tuple : entry.getValue())
        mins.get(tuple).add(d, nearbyStop);
    }

    Map<Stop, Set<Route>> goodStopsWithRoutes = new FactoryMap<Stop, Set<Route>>(new HashSet<Route>());

    for (Map.Entry<T2<Route, String>, Min<Stop>> entry : mins.entrySet()) {

      T2<Route, String> routeAndDirectionId = entry.getKey();
      Min<Stop> min = entry.getValue();

      Stop minStop = min.getMinElement();
      goodStopsWithRoutes.get(minStop).add(routeAndDirectionId.getFirst());
    }

    for (Map.Entry<Stop, Set<Route>> entry : goodStopsWithRoutes.entrySet()) {

      Stop nearby = entry.getKey();
      Set<Route> routes = entry.getValue();

      StopBean nearbyBean = new StopBean();
      ApplicationBeanLibrary.fillStopBean(nearby, nearbyBean);

      List<RouteBean> routeBeans = new ArrayList<RouteBean>(routes.size());
      for (Route route : routes)
        routeBeans.add(getRouteAsBean(route));
      Collections.sort(routeBeans, _routeBeanComparator);
      nearbyBean.setRoutes(routeBeans);

      sb.addNearbyStop(nearbyBean);
    }

    Collections.sort(sb.getNearbyStops(), new StopDistanceComparator(sb));
  }

  private void fillRoutesForStopBean(Stop stop, StopBean sb) {
    List<Route> routes = _gtfsDao.getRoutesByStopId(stop.getId());
    List<RouteBean> routeBeans = new ArrayList<RouteBean>(routes.size());
    for (Route route : routes)
      routeBeans.add(getRouteAsBean(route));
    Collections.sort(routeBeans, _routeBeanComparator);
    sb.setRoutes(routeBeans);
  }

  private RouteBean getRouteAsBean(Route route) {
    RouteBean bean = new RouteBean();
    bean.setId(route.getId());
    bean.setNumber(route.getShortName());
    return bean;
  }

  private List<StopBean> getStopBeansFromStops(Set<Stop> stops) {
    List<StopBean> stopBeans = new ArrayList<StopBean>(stops.size());
    for (Stop stop : stops) {
      StopBean stopBean = new StopBean();
      ApplicationBeanLibrary.fillStopBean(stop, stopBean);
      stopBeans.add(stopBean);
    }
    return stopBeans;
  }

  private List<PathBean> getPathBeansFromShapeIds(Set<String> shapeIds) {

    List<PathBean> paths = new ArrayList<PathBean>();

    Graph<Location> graph = new Graph<Location>();

    for (String shapeId : shapeIds) {

      List<ShapePoint> points = _gtfsDao.getShapePointsByShapeId(shapeId);

      Location prev = null;
      for (ShapePoint point : points) {
        Location loc = new Location(point);
        if (prev != null) {
          if (!prev.equals(loc))
            graph.addEdge(prev, loc);
        }
        prev = loc;
      }
    }

    for (Location node : graph.getNodes()) {

      if (!isSegmetStart(graph, node))
        continue;

      Set<Location> outbound = graph.getOutboundNodes(node);

      for (Location next : outbound) {
        List<Location> locations = new ArrayList<Location>();
        locations.add(node);
        while (next != null) {
          locations.add(next);
          if (!isSegmetStart(graph, next)) {
            Set<Location> nextOutbound = graph.getOutboundNodes(next);
            if (nextOutbound.size() > 1)
              throw new IllegalStateException();
            if (nextOutbound.size() == 1)
              next = nextOutbound.iterator().next();
            else
              next = null;
          } else {
            next = null;
          }
        }

        paths.add(getLocationsAsPathBean(locations));
      }

    }

    return paths;
  }

  private boolean isSegmetStart(Graph<Location> graph, Location node) {
    Set<Location> outbound = graph.getOutboundNodes(node);
    if (outbound.size() == 0)
      return false;
    Set<Location> inbound = graph.getInboundNodes(node);
    if (outbound.size() == 1 && inbound.size() == 1)
      return false;
    return true;
  }

  private PathBean getLocationsAsPathBean(List<Location> locations) {
    double[] lat = new double[locations.size()];
    double[] lon = new double[locations.size()];
    int index = 0;
    for (Location location : locations) {
      lat[index] = location.getLat();
      lon[index] = location.getLon();
      index++;
    }
    return new PathBean(lat, lon);
  }

  private StopRouteScheduleBean getRouteBeanForStopTime(Map<Route, StopRouteScheduleBean> routeMapping,
      Map<Route, List<StopSequenceBlock>> blocksByRoute, StopTime stopTime) {

    Trip trip = stopTime.getTrip();
    Route route = trip.getRoute();
    StopRouteScheduleBean routeBean = routeMapping.get(route);
    if (routeBean == null) {
      routeBean = new StopRouteScheduleBean();
      routeBean.setName(route.getShortName());
      List<StopSequenceBlock> blocksForRoute = blocksByRoute.get(route);
      if (blocksForRoute == null || blocksForRoute.isEmpty())
        routeBean.setDescription(route.getDesc());
      else
        routeBean.setDescription(blocksForRoute.get(0).getDescription());
      routeMapping.put(route, routeBean);
    }
    return routeBean;
  }

  private DepartureBean getPredictedArrivalTimeAsBean(Stop stop, StopTimeInstance sti) {

    DepartureBean pab = new DepartureBean();

    pab.setScheduledTime(sti.getDepartureTime().getTime());

    if (sti.hasPrediction())
      pab.setPredictedTime(sti.getDepartureTime().getTime() + sti.getPredictionOffset());

    StopTime stopTime = sti.getStopTime();
    Trip trip = stopTime.getTrip();
    Route route = trip.getRoute();

    String routeName = ApplicationBeanLibrary.getBestName(trip.getRouteShortName(), route.getShortName());
    pab.setRoute(routeName);

    String destination = ApplicationBeanLibrary.getBestName(stopTime.getStopHeadsign(), trip.getTripHeadsign(),
        route.getLongName());
    pab.setDestination(destination);

    String status = _statusService.getRouteStatus(route);
    pab.setStatus(status);

    pab.setTripId(trip.getId());
    pab.setStopId(stop.getId());

    return pab;
  }

  private static class StopTimeBeanComparator implements Comparator<StopTimeBean> {

    public int compare(StopTimeBean o1, StopTimeBean o2) {
      Date t1 = o1.getDepartureDate();
      Date t2 = o2.getDepartureDate();
      return t1.compareTo(t2);
    }
  }

  private static class StopRouteScheduleBeanComparator implements Comparator<StopRouteScheduleBean> {

    public int compare(StopRouteScheduleBean o1, StopRouteScheduleBean o2) {
      return NaturalStringOrder.compareNatural(o1.getName(), o2.getName());
    }
  }

  private static class RouteBeanComparator implements Comparator<RouteBean> {
    public int compare(RouteBean o1, RouteBean o2) {
      return NaturalStringOrder.compareNatural(o1.getNumber(), o2.getNumber());
    }
  }

  private static class Location {

    private final double _lat;

    private final double _lon;

    public Location(ShapePoint point) {
      _lat = point.getLat();
      _lon = point.getLon();
    }

    public double getLat() {
      return _lat;
    }

    public double getLon() {
      return _lon;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof Location))
        return false;
      Location loc = (Location) obj;
      return _lat == loc._lat && _lon == loc._lon;
    }

    @Override
    public int hashCode() {
      return new Double(_lat).hashCode() + new Double(_lon).hashCode();
    }

    @Override
    public String toString() {
      return "Loc(lat=" + _lat + " lon=" + _lon + ")";
    }
  }

  private static class StopDistanceComparator implements Comparator<StopBean> {

    private StopBean _target;

    public StopDistanceComparator(StopBean target) {
      _target = target;
    }

    public int compare(StopBean o1, StopBean o2) {
      double d1 = getDistance(_target, o1);
      double d2 = getDistance(_target, o2);
      return d1 == d2 ? 0 : (d1 < d2 ? -1 : 1);
    }

    private double getDistance(StopBean o1, StopBean o2) {
      double dx = o1.getX() - o2.getX();
      double dy = o1.getY() - o2.getY();
      return Math.sqrt(dx * dx + dy * dy);
    }

  }

  private static class StopNameComparator implements Comparator<StopBean> {

    public int compare(StopBean o1, StopBean o2) {
      return o1.getName().compareTo(o2.getName());
    }

  }
}
