package org.onebusaway.where.impl;

import edu.washington.cs.rse.collections.CollectionsLibrary;
import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.combinations.Combinations;
import edu.washington.cs.rse.collections.stats.Counter;
import edu.washington.cs.rse.collections.tuple.Pair;
import edu.washington.cs.rse.text.DateLibrary;
import edu.washington.cs.rse.text.NaturalStringOrder;

import com.vividsolutions.jts.geom.Geometry;

import org.onebusaway.common.spring.Cacheable;
import org.onebusaway.gtdf.model.Route;
import org.onebusaway.gtdf.model.ShapePoint;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.gtdf.model.Trip;
import org.onebusaway.gtdf.services.CalendarService;
import org.onebusaway.gtdf.services.GtdfDao;
import org.onebusaway.where.model.StopSelectionTree;
import org.onebusaway.where.model.StopSequence;
import org.onebusaway.where.model.StopSequenceBlock;
import org.onebusaway.where.model.StopTimeInstance;
import org.onebusaway.where.services.NoSuchRouteException;
import org.onebusaway.where.services.StopSelectionService;
import org.onebusaway.where.services.StopTimePredictionService;
import org.onebusaway.where.services.StopTimeService;
import org.onebusaway.where.services.WhereDao;
import org.onebusaway.where.services.WhereIntermediateService;
import org.onebusaway.where.web.common.client.model.DepartureBean;
import org.onebusaway.where.web.common.client.model.PathBean;
import org.onebusaway.where.web.common.client.model.RouteBean;
import org.onebusaway.where.web.common.client.model.StopBean;
import org.onebusaway.where.web.common.client.model.StopCalendarDayBean;
import org.onebusaway.where.web.common.client.model.StopRouteScheduleBean;
import org.onebusaway.where.web.common.client.model.StopSequenceBlockBean;
import org.onebusaway.where.web.common.client.model.StopTimeBean;
import org.onebusaway.where.web.common.client.rpc.NoSuchRouteServiceException;
import org.onebusaway.where.web.common.client.rpc.NoSuchStopServiceException;
import org.onebusaway.where.web.common.client.rpc.ServiceException;
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

  @Autowired
  private GtdfDao _gtdfDao;

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

  @Cacheable
  public StopBean getStop(String stopId) throws ServiceException {

    Stop stop = _gtdfDao.getStopById(stopId);

    if (stop == null)
      throw new NoSuchStopServiceException();

    StopBean sb = new StopBean();
    ApplicationBeanLibrary.fillStopBean(stop, sb);
    fillNearbyStopsForStopBean(stop, sb);
    fillRoutesForStopBean(stop, sb);
    return sb;
  }

  @Transactional
  @Cacheable
  public List<StopRouteScheduleBean> getScheduledArrivalsForStopAndDate(
      String stopId, Date date) throws ServiceException {

    date = DateLibrary.getTimeAsDay(date);

    Stop stop = _gtdfDao.getStopById(stopId);

    if (stop == null)
      throw new NoSuchStopServiceException();

    List<StopSequenceBlock> blocks = _whereDao.getStopSequenceBlocksByStop(stop);

    Map<Route, List<StopSequenceBlock>> blocksByRoute = CollectionsLibrary.mapToValueList(
        blocks, "id.route", Route.class);

    Set<String> serviceIds = _calendarService.getServiceIdsOnDate(date);
    List<StopTime> stopTimes = _gtdfDao.getStopTimesByStopAndServiceIds(stop,
        serviceIds);

    Map<Route, StopRouteScheduleBean> routeMapping = new HashMap<Route, StopRouteScheduleBean>();

    for (StopTime stopTime : stopTimes) {
      StopTimeInstance sti = new StopTimeInstance(stopTime, date);
      StopRouteScheduleBean routeBean = getRouteBeanForStopTime(routeMapping,
          blocksByRoute, stopTime);
      StopTimeBean stBean = new StopTimeBean();
      stBean.setDepartureTime(sti.getDepartureTime());
      stBean.setServiceId(stopTime.getTrip().getServiceId());
      routeBean.getStopTimes().add(stBean);
    }

    for (StopRouteScheduleBean bean : routeMapping.values())
      Collections.sort(bean.getStopTimes(), _stopTimeComparator);

    List<StopRouteScheduleBean> beans = new ArrayList<StopRouteScheduleBean>(
        routeMapping.values());
    Collections.sort(beans, _stopRouteScheduleComparator);
    return beans;
  }

  @Cacheable
  public List<StopCalendarDayBean> getCalendarForStop(String stopId)
      throws ServiceException {

    Stop stop = _gtdfDao.getStopById(stopId);

    if (stop == null)
      throw new NoSuchStopServiceException();

    List<String> serviceIds = _gtdfDao.getServiceIdsByStop(stop);

    SortedMap<Date, Set<String>> serviceIdsByDate = new TreeMap<Date, Set<String>>();
    serviceIdsByDate = FactoryMap.createSorted(serviceIdsByDate,
        new HashSet<String>());

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

    List<StopCalendarDayBean> beans = new ArrayList<StopCalendarDayBean>(
        serviceIdsByDate.size());
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
  public StopSelectionTree getStopSelectionTreeForRoute(String route)
      throws ServiceException {
    try {
      return _stopSelection.getStopsByRoute(route);
    } catch (NoSuchRouteException e) {
      throw new NoSuchRouteServiceException();
    }
  }

  @Transactional
  @Cacheable
  public List<StopSequenceBlockBean> getServicePatternBlocksByRoute(
      String routeName) throws ServiceException {

    Route route = _gtdfDao.getRouteByShortName(routeName);
    if (route == null)
      throw new NoSuchRouteServiceException();

    List<StopSequenceBlock> blocks = _whereDao.getStopSequenceBlocksByRoute(route);
    List<StopSequenceBlockBean> blockBeans = new ArrayList<StopSequenceBlockBean>(
        blocks.size());

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
  public List<DepartureBean> getDeparturesForStop(String stopId)
      throws ServiceException {

    Stop stop = _gtdfDao.getStopById(stopId);

    if (stop == null)
      throw new NoSuchStopServiceException();

    Calendar c = Calendar.getInstance();
    c.add(Calendar.MINUTE, -30);
    Date from = c.getTime();
    c.add(Calendar.MINUTE, 65);
    Date to = c.getTime();

    List<StopTimeInstance> stis = _stopTimeService.getStopTimeInstancesInTimeRange(
        stop, from, to);
    _stopTimePredictionService.getPredictions(stis);

    List<DepartureBean> beans = new ArrayList<DepartureBean>();
    for (StopTimeInstance sti : stis)
      beans.add(getPredictedArrivalTimeAsBean(sti));

    return beans;
  }

  private void fillNearbyStopsForStopBean(Stop stop, StopBean sb) {
    Geometry envelope = stop.getLocation().buffer(300).getEnvelope();
    for (Stop nearby : _gtdfDao.getStopsByLocation(envelope)) {
      if (!nearby.equals(stop)) {
        StopBean nearbyBean = new StopBean();
        ApplicationBeanLibrary.fillStopBean(nearby, nearbyBean);
        sb.addNearbyStop(nearbyBean);
      }
    }
  }

  private void fillRoutesForStopBean(Stop stop, StopBean sb) {
    List<Route> routes = _gtdfDao.getRoutesByStopId(stop.getId());
    List<RouteBean> routeBeans = new ArrayList<RouteBean>(routes.size());
    for (Route route : routes)
      routeBeans.add(getRouteAsBean(route));
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
    Set<Pair<ShapePoint>> segments = new HashSet<Pair<ShapePoint>>();

    for (String shapeId : shapeIds) {

      List<ShapePoint> points = _gtdfDao.getShapePointsByShapeId(shapeId);
      List<ShapePoint> continuousPoints = new ArrayList<ShapePoint>();

      for (Pair<ShapePoint> segment : Combinations.getSequentialPairs(points)) {
        if (segments.add(segment)) {
          if (continuousPoints.isEmpty())
            continuousPoints.add(segment.getFirst());
          continuousPoints.add(segment.getSecond());
        } else {
          flushPoints(paths, continuousPoints);
        }
      }

      flushPoints(paths, continuousPoints);
    }
    return paths;
  }

  private void flushPoints(List<PathBean> paths,
      List<ShapePoint> continuousPoints) {
    if (!continuousPoints.isEmpty()) {
      double[] lat = new double[continuousPoints.size()];
      double[] lon = new double[continuousPoints.size()];
      for (int i = 0; i < continuousPoints.size(); i++) {
        ShapePoint point = continuousPoints.get(i);
        lat[i] = point.getLat();
        lon[i] = point.getLon();
      }
      PathBean path = new PathBean(lat, lon);
      paths.add(path);
      continuousPoints.clear();
    }
  }

  private StopRouteScheduleBean getRouteBeanForStopTime(
      Map<Route, StopRouteScheduleBean> routeMapping,
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

  private DepartureBean getPredictedArrivalTimeAsBean(StopTimeInstance sti) {

    DepartureBean pab = new DepartureBean();

    pab.setScheduledTime(sti.getDepartureTime().getTime());

    if (sti.hasPrediction())
      pab.setPredictedTime(sti.getDepartureTime().getTime()
          + sti.getPredictionOffset());

    StopTime stopTime = sti.getStopTime();
    Trip trip = stopTime.getTrip();
    Route route = trip.getRoute();

    String routeName = getBestName(trip.getRouteShortName(),
        route.getShortName());
    pab.setRoute(routeName);

    String destination = getBestName(stopTime.getStopHeadsign(),
        trip.getTripHeadsign(), route.getLongName());
    pab.setDestination(destination);

    pab.setTripId(trip.getId());
    return pab;
  }

  private String getBestName(String... names) {
    for (String name : names) {
      name = name == null ? "" : name.trim();
      if (name.length() > 0)
        return name;
    }
    return "";
  }

  private static class StopTimeBeanComparator implements
      Comparator<StopTimeBean> {

    public int compare(StopTimeBean o1, StopTimeBean o2) {
      Date t1 = o1.getDepartureTime();
      Date t2 = o2.getDepartureTime();
      return t1.compareTo(t2);
    }
  }

  private static class StopRouteScheduleBeanComparator implements
      Comparator<StopRouteScheduleBean> {

    public int compare(StopRouteScheduleBean o1, StopRouteScheduleBean o2) {
      return NaturalStringOrder.compareNatural(o1.getName(), o2.getName());
    }
  }
}
