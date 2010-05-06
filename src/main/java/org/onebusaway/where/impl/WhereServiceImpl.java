package org.onebusaway.where.impl;

import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.common.web.common.client.model.PathBean;
import org.onebusaway.common.web.common.client.model.StopBean;
import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.CalendarService;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.where.model.SelectionName;
import org.onebusaway.where.model.StopSelectionTree;
import org.onebusaway.where.model.StopSequence;
import org.onebusaway.where.model.StopTimeInstance;
import org.onebusaway.where.model.TimepointPredictionSummary;
import org.onebusaway.where.services.StatusService;
import org.onebusaway.where.services.StopTimePredictionService;
import org.onebusaway.where.services.WhereDao;
import org.onebusaway.where.services.WhereIntermediateService;
import org.onebusaway.where.web.actions.GeocoderAccuracyToBounds;
import org.onebusaway.where.web.common.client.model.DepartureBean;
import org.onebusaway.where.web.common.client.model.NameBean;
import org.onebusaway.where.web.common.client.model.NameTreeBean;
import org.onebusaway.where.web.common.client.model.NearbyRoutesBean;
import org.onebusaway.where.web.common.client.model.StopAndTimeBean;
import org.onebusaway.where.web.common.client.model.StopCalendarDayBean;
import org.onebusaway.where.web.common.client.model.StopRouteScheduleBean;
import org.onebusaway.where.web.common.client.model.StopScheduleBean;
import org.onebusaway.where.web.common.client.model.StopSequenceBean;
import org.onebusaway.where.web.common.client.model.StopSequenceBlockBean;
import org.onebusaway.where.web.common.client.model.StopTimeBean;
import org.onebusaway.where.web.common.client.model.StopWithArrivalsBean;
import org.onebusaway.where.web.common.client.model.StopsBean;
import org.onebusaway.where.web.common.client.model.TripStatusBean;
import org.onebusaway.where.web.common.client.rpc.InvalidSelectionServiceException;
import org.onebusaway.where.web.common.client.rpc.NoSuchRouteServiceException;
import org.onebusaway.where.web.common.client.rpc.NoSuchTripServiceException;
import org.onebusaway.where.web.common.client.rpc.WhereService;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
class WhereServiceImpl implements WhereService {

  private static final long serialVersionUID = 1L;

  @Autowired
  private GtfsDao _gtfsDao;

  @Autowired
  private WhereDao _whereDao;

  @Autowired
  private CalendarService _calendarService;

  @Autowired
  private ProjectionService _projection;

  @Autowired
  private WhereIntermediateService _whereIntermediate;

  @Autowired
  private StatusService _statusService;

  @Autowired
  private StopTimePredictionService _stopTimePredictionService;

  /***************************************************************************
   * {@link WhereService} Interface
   **************************************************************************/

  public StopsBean getStopsByLocationAndAccuracy(double lat, double lon, int accuracy) {
    int r = GeocoderAccuracyToBounds.getBoundsInFeetByAccuracy(accuracy);
    Point p = _projection.getLatLonAsPoint(lat, lon);
    Geometry boundary = p.buffer(r).getEnvelope();
    return getStopsByGeometry(boundary);
  }

  public StopsBean getStopsByBounds(double lat1, double lon1, double lat2, double lon2) throws ServiceException {
    Point p1 = _projection.getLatLonAsPoint(lat1, lon1);
    Point p2 = _projection.getLatLonAsPoint(lat2, lon2);
    Geometry g = p1.union(p2);
    g = g.getEnvelope();
    return getStopsByGeometry(g);
  }

  public StopBean getStop(String stopId) throws ServiceException {
    return _whereIntermediate.getStop(stopId);
  }

  public StopBean getStop(String stopId, double nearbyStopSearchDistance) throws ServiceException {
    return _whereIntermediate.getStopWithNearbyStopRadius(stopId, nearbyStopSearchDistance);
  }

  public NearbyRoutesBean getNearbyRoutes(String stopId, double nearbyRouteSearchDistance) throws ServiceException {
    return _whereIntermediate.getNearbyRoutes(stopId, nearbyRouteSearchDistance);
  }

  public StopWithArrivalsBean getArrivalsByStopId(String stopId) throws ServiceException {

    StopBean stopBean = _whereIntermediate.getStop(stopId);
    List<DepartureBean> departures = _whereIntermediate.getDeparturesForStop(stopId);
    long now = System.currentTimeMillis();
    long from = now - 5 * 60 * 1000;
    long to = now + 35 * 60 * 1000;
    List<DepartureBean> filtered = new ArrayList<DepartureBean>();
    for (DepartureBean bean : departures) {
      long scheduledTime = bean.getScheduledTime();
      if (from <= scheduledTime && scheduledTime <= to) {
        filtered.add(bean);
      } else if (bean.hasPredictedTime()) {
        long predictedTime = bean.getPredictedTime();
        if (from <= predictedTime && predictedTime <= to)
          filtered.add(bean);
      }
    }
    StopWithArrivalsBean arrivalsBean = new StopWithArrivalsBean(stopBean, filtered);

    return arrivalsBean;
  }

  public NameTreeBean getStopByRoute(String route, List<Integer> selection) throws ServiceException {
    NameTreeBean bean = new NameTreeBean();
    StopSelectionTree tree = _whereIntermediate.getStopSelectionTreeForRoute(route);
    visitTree(tree, bean, selection, 0);
    return bean;
  }

  public List<StopSequenceBlockBean> getStopSequenceBlocksByRoute(String route) throws ServiceException {
    return _whereIntermediate.getStopSequenceBlocksByRoute(route);
  }

  @Transactional
  public List<StopSequenceBean> getStopSequencesByRoute(String route) throws NoSuchRouteServiceException {

    Route r = _gtfsDao.getRouteByShortName(route);
    if (r == null)
      throw new NoSuchRouteServiceException();

    List<StopSequenceBean> beans = new ArrayList<StopSequenceBean>();

    for (StopSequence sequence : _whereDao.getStopSequencesByRoute(r)) {

      StopSequenceBean bean = new StopSequenceBean();

      bean.setId(sequence.getId());
      bean.setDirectionId(sequence.getDirectionId());
      bean.setTripCount(sequence.getTripCount());

      String shapeId = sequence.getShapeId();
      if (shapeId != null) {
        List<ShapePoint> points = _gtfsDao.getShapePointsByShapeId(shapeId);
        PathBean path = ApplicationBeanLibrary.getShapePointsAsPathBean(points);
        bean.setPath(path);
      }

      List<StopBean> stops = new ArrayList<StopBean>();
      for (Stop stop : sequence.getStops())
        stops.add(ApplicationBeanLibrary.getStopAsBean(stop));
      bean.setStops(stops);

      beans.add(bean);
    }

    return beans;
  }

  public StopScheduleBean getScheduleForStop(String stopId, Date date) throws ServiceException {

    StopScheduleBean bean = new StopScheduleBean();
    bean.setDate(date);

    StopBean stopBean = _whereIntermediate.getStop(stopId);
    bean.setStop(stopBean);

    List<StopRouteScheduleBean> routes = _whereIntermediate.getScheduledArrivalsForStopAndDate(stopId, date);
    bean.setRoutes(routes);

    List<StopCalendarDayBean> calendarDays = _whereIntermediate.getCalendarForStop(stopId);
    bean.setCalendarDays(calendarDays);

    return bean;
  }

  @Transactional
  public TripStatusBean getTripStatus(String tripId) throws ServiceException {

    Trip trip = _gtfsDao.getTripById(tripId);
    if (trip == null)
      throw new NoSuchTripServiceException(tripId);

    TripStatusBean bean = new TripStatusBean();
    bean.setTripId(tripId);

    Route route = trip.getRoute();
    bean.setRouteId(route.getId());
    bean.setRouteName(route.getShortName());

    String status = _statusService.getRouteStatus(route);
    bean.setStatus(status);

    String destination = ApplicationBeanLibrary.getBestName(trip.getTripHeadsign(), route.getLongName());
    bean.setDestination(destination);

    TimepointPredictionSummary summary = _stopTimePredictionService.getPredictionSummary(tripId);
    bean.setNumberOfPredictions(summary.getNumberOfPredictions());
    bean.setGoalDeviation(summary.getGoalDeviation());

    Set<Date> dates = _calendarService.getDatesForServiceId(trip.getServiceId());

    if (!dates.isEmpty()) {
      Date date = dates.iterator().next();

      List<StopTime> stopTimes = _gtfsDao.getStopTimesByTrip(trip);

      for (StopTime stopTime : stopTimes) {
        StopTimeInstance sti = new StopTimeInstance(stopTime, date);
        StopTimeBean stBean = new StopTimeBean();
        stBean.setArrivalTime(stopTime.getArrivalTime());
        stBean.setArrivalDate(sti.getArrivalTime());
        stBean.setDepartureTime(stopTime.getDepartureTime());
        stBean.setDepartureDate(sti.getDepartureTime());
        stBean.setTripId(trip.getId());
        stBean.setServiceId(trip.getServiceId());

        Stop stop = stopTime.getStop();
        StopBean stopBean = ApplicationBeanLibrary.getStopAsBean(stop);

        StopAndTimeBean satBean = new StopAndTimeBean();
        satBean.setStopTime(stBean);
        satBean.setStop(stopBean);
        bean.addStopAndTimeBean(satBean);
      }
    }

    Trip previousTrip = _gtfsDao.getPreviousTripInBlock(trip);
    if (previousTrip != null) {
      Route previousRoute = previousTrip.getRoute();
      bean.setPreviousTripId(previousTrip.getId());
      bean.setPreviousRouteName(ApplicationBeanLibrary.getBestName(previousTrip.getRouteShortName(),
          previousRoute.getShortName()));
    }

    Trip nextTrip = _gtfsDao.getNextTripInBlock(trip);
    if (nextTrip != null) {
      Route nextRoute = nextTrip.getRoute();
      bean.setNextTripId(nextTrip.getId());
      bean.setNextRouteName(ApplicationBeanLibrary.getBestName(nextTrip.getRouteShortName(), nextRoute.getShortName()));
    }

    return bean;
  }

  /***************************************************************************
   * 
   **************************************************************************/

  private StopsBean getStopsByGeometry(Geometry g) {

    List<Stop> stops = _gtfsDao.getStopsByLocation(g, 75);

    List<StopBean> stopBeans = new ArrayList<StopBean>();

    for (Stop stop : stops) {
      StopBean sb = ApplicationBeanLibrary.getStopAsBean(stop);
      stopBeans.add(sb);
    }

    StopsBean stopsBean = new StopsBean();
    stopsBean.setStopBeans(stopBeans);
    return stopsBean;
  }

  /***************************************************************************
   * Tree Methods
   **************************************************************************/

  private void visitTree(StopSelectionTree tree, NameTreeBean bean, List<Integer> selection, int index)
      throws InvalidSelectionServiceException {

    // If we have a stop, we have no choice but to return
    if (tree.hasStop()) {
      bean.setStop(ApplicationBeanLibrary.getStopAsBean(tree.getStop()));
      return;
    }

    Set<SelectionName> names = tree.getNames();

    // If we've only got one name, short circuit
    if (names.size() == 1) {

      SelectionName next = names.iterator().next();
      bean.addSelected(ApplicationBeanLibrary.getNameAsBean(next));

      StopSelectionTree subtree = tree.getSubTree(next);
      visitTree(subtree, bean, selection, index);

      return;
    }

    if (index >= selection.size()) {

      for (SelectionName name : names) {
        NameBean n = ApplicationBeanLibrary.getNameAsBean(name);
        Stop stop = getStop(tree.getSubTree(name));
        if (stop != null) {
          bean.addNameWithStop(n, ApplicationBeanLibrary.getStopAsBean(stop));
        } else {
          bean.addName(n);
        }
      }

      List<Stop> stops = tree.getAllStops();

      for (Stop stop : stops)
        bean.addStop(ApplicationBeanLibrary.getStopAsBean(stop));

      return;

    } else {

      int i = 0;
      int selectionIndex = selection.get(index);

      for (SelectionName name : names) {
        if (selectionIndex == i) {
          bean.addSelected(ApplicationBeanLibrary.getNameAsBean(name));
          tree = tree.getSubTree(name);
          visitTree(tree, bean, selection, index + 1);
          return;
        }
        i++;
      }
    }

    // If we made it here...
    throw new InvalidSelectionServiceException();
  }

  private Stop getStop(StopSelectionTree tree) {

    if (tree.hasStop())
      return tree.getStop();

    if (tree.getNames().size() == 1) {
      SelectionName next = tree.getNames().iterator().next();
      return getStop(tree.getSubTree(next));
    }

    return null;
  }
}
