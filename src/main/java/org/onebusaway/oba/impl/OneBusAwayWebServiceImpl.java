package org.onebusaway.oba.impl;

import org.onebusaway.common.impl.UtilityLibrary;
import org.onebusaway.common.model.Order;
import org.onebusaway.common.model.Place;
import org.onebusaway.common.services.CommonDao;
import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.oba.services.OneBusAwayUserSession;
import org.onebusaway.oba.web.common.client.model.LocalSearchResult;
import org.onebusaway.oba.web.common.client.model.LocationBounds;
import org.onebusaway.oba.web.common.client.model.OneBusAwayConstraintsBean;
import org.onebusaway.oba.web.common.client.model.MinTransitTimeResult;
import org.onebusaway.oba.web.common.client.model.TimedPlaceBean;
import org.onebusaway.oba.web.common.client.rpc.OneBusAwayWebService;
import org.onebusaway.tripplanner.model.TripPlannerConstants;
import org.onebusaway.tripplanner.model.TripPlannerConstraints;
import org.onebusaway.tripplanner.model.WalkPlan;
import org.onebusaway.tripplanner.services.NoPathException;
import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.StopWalkPlannerService;
import org.onebusaway.tripplanner.services.TripPlannerBeanFactory;
import org.onebusaway.tripplanner.services.TripPlannerService;
import org.onebusaway.tripplanner.web.common.client.model.TripBean;

import edu.washington.cs.rse.collections.CollectionsLibrary;
import edu.washington.cs.rse.collections.tuple.T2;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import com.vividsolutions.jts.geom.Point;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

class OneBusAwayWebServiceImpl implements OneBusAwayWebService {

  @Autowired
  private TripPlannerConstants _constants;

  @Autowired
  private CommonDao _commonDao;

  @Autowired
  private TripPlannerService _tripPlannerService;

  @Autowired
  private TripPlannerBeanFactory _tripPlannerBeanFactory;

  @Autowired
  private ProjectionService _projectionService;

  @Autowired
  private OneBusAwayUserSession _session;

  @Autowired
  public StopWalkPlannerService _stopWalkPlanner;

  public void setCommonDao(CommonDao commonDao) {
    _commonDao = commonDao;
  }

  public void setTripPlannerService(TripPlannerService tripPlannerService) {
    _tripPlannerService = tripPlannerService;
  }

  public void setProjectionService(ProjectionService projectionService) {
    _projectionService = projectionService;
  }

  public void setOneBusAwayUserSession(OneBusAwayUserSession session) {
    _session = session;
  }

  public void setStopWalkPlannerService(StopWalkPlannerService stopWalkPlanner) {
    _stopWalkPlanner = stopWalkPlanner;
  }

  public MinTransitTimeResult getMinTravelTimeToStopsFrom(double lat, double lon, OneBusAwayConstraintsBean constraints)
      throws ServiceException {

    CoordinatePoint p = new CoordinatePoint(lat, lon);

    TripPlannerConstraints tpc = new TripPlannerConstraints();

    if (constraints.hasMinDepartureTimeOfDay() && constraints.hasMaxDepartureTimeOfDay()) {
      Calendar c = Calendar.getInstance();
      while (c.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
        c.add(Calendar.DAY_OF_WEEK, 1);

      setSecondsOfDay(c, constraints.getMinDepartureTimeOfDay());
      tpc.setMinDepartureTime(c.getTimeInMillis());
      Date from = c.getTime();

      setSecondsOfDay(c, constraints.getMaxDepartureTimeOfDay());
      tpc.setMaxDepartureTime(c.getTimeInMillis());
      Date to = c.getTime();

      System.out.println("from=" + from + " to=" + to);

    } else if (constraints.hasMinDepartureTime() && constraints.hasMaxDepartureTime()) {
      tpc.setMinDepartureTime(constraints.getMinDepartureTime());
      tpc.setMaxDepartureTime(constraints.getMaxDepartureTime());
    } else {
      throw new ServiceException("must specify departure time constraints");
    }

    long maxTripDuration = constraints.getMaxTripDuration() * 60 * 1000;
    tpc.setMaxTripDuration(maxTripDuration);

    if (constraints.hasMaxTransfers())
      tpc.setMaxTransferCount(constraints.getMaxTransfers());

    Map<StopProxy, Long> minTravelTimes = _tripPlannerService.getMinTravelTimeToStopsFrom(p, tpc);
    
    String resultId = Long.toString(System.currentTimeMillis());
    _session.setOneBusAwayResults(resultId, constraints, minTravelTimes);
    
    double maxWalkDistance = constraints.getMaxWalkingDistance();
    GridFactory gridFactory = new GridFactory(_projectionService, 5280);
    TimedGridFactory timedGridFactory = new TimedGridFactory(_projectionService, 5280 / 4,
        _constants.getWalkingVelocity());

    long maxTripLength = constraints.getMaxTripDuration() * 60 * 1000;

    for (Map.Entry<StopProxy, Long> entry : minTravelTimes.entrySet()) {
      
      StopProxy stop = entry.getKey();
      long duration = entry.getValue();
      
      double remainingWalkingDistance = (maxTripLength - duration) * _constants.getWalkingVelocity();
      remainingWalkingDistance = Math.min(remainingWalkingDistance, maxWalkDistance);

      gridFactory.addPoint(stop.getStopLocation(), remainingWalkingDistance);
      long remainingWalkingTime = (long) (remainingWalkingDistance / _constants.getWalkingVelocity());
      if (remainingWalkingTime > 0)
        timedGridFactory.addPoint(stop.getStopLocation(), remainingWalkingDistance, duration, remainingWalkingTime);
    }

    MinTransitTimeResult result = new MinTransitTimeResult();
    result.setResultId(resultId);
    
    for (T2<LocationBounds, Long> entry : timedGridFactory.getGridAndTimes()) {
      Long time = entry.getSecond();
      result.getTimeGrid().add(entry.getFirst());
      result.getTimes().add((int) (time / 1000));
    }

    result.setSearchGrid(gridFactory.getGrid());
    return result;
  }

  public List<TimedPlaceBean> getLocalPaths(String resultId, List<LocalSearchResult> localResults)
      throws ServiceException {

    if (!resultId.equals(_session.getResultId()))
      throw new ServiceException("bad");

    OneBusAwayConstraintsBean constraints = _session.getConstraints();
    Map<StopProxy,Long> transitTimes = _session.getTransitTimes();

    long maxTripLength = constraints.getMaxTripDuration() * 60 * 1000;

    Map<String, Place> placesById = getPlacesFromLocalSearchResults(localResults);
    List<Place> toSave = new ArrayList<Place>();

    List<TimedPlaceBean> beans = new ArrayList<TimedPlaceBean>();

    for (LocalSearchResult result : localResults) {

      Place place = placesById.get(result.getId());

      if (place == null) {
        place = new Place();
        place.setId(result.getId());
        place.setLat(result.getLat());
        place.setLon(result.getLon());
        place.setName(result.getName());
        Point location = _projectionService.getLatLonAsPoint(result.getLat(), result.getLon());
        place.setLocation(location);
        toSave.add(place);
      }

      Point location = place.getLocation();

      List<Order<Map.Entry<StopProxy, Long>>> closestStops = new ArrayList<Order<Map.Entry<StopProxy, Long>>>();

      for (Map.Entry<StopProxy, Long> entry : transitTimes.entrySet()) {
        StopProxy stop = entry.getKey();
        long currentTripDuration = entry.getValue();
        double d = UtilityLibrary.distance(location, stop.getStopLocation());
        if (d <= constraints.getMaxWalkingDistance()) {
          double t = currentTripDuration + d / _constants.getWalkingVelocity();
          Order<Map.Entry<StopProxy, Long>> o = Order.create(entry, t);
          closestStops.add(o);
        }
      }

      if (closestStops.isEmpty())
        continue;

      Collections.sort(closestStops);

      double minTime = 0;
      StopProxy minStop = null;
      WalkPlan minWalkPlan = null;

      for (Order<Map.Entry<StopProxy, Long>> o : closestStops) {

        Entry<StopProxy, Long> entry = o.getObject();
        StopProxy stop = entry.getKey();
        long currentTripDuration = entry.getValue();

        // Short circuit if there is no way any of the remaining trips is going
        // to be better than our current winner
        if (minStop != null && o.getValue() > minTime)
          break;

        try {
          WalkPlan plan = _stopWalkPlanner.getWalkPlanForStopProxyToPlace(stop, place);
          double t = currentTripDuration + plan.getDistance() / _constants.getWalkingVelocity();
          if (minStop == null || t < minTime) {
            minTime = t;
            minStop = stop;
            minWalkPlan = plan;
          }
        } catch (NoPathException e) {
        }
      }

      if (minStop != null && minTime <= maxTripLength) {
        TimedPlaceBean bean = new TimedPlaceBean();
        bean.setPlaceId(place.getId());
        bean.setStopId(minStop.getStopId());
        bean.setTime((int) (minTime / 1000));
        beans.add(bean);
      }
    }

    return beans;
  }

  public TripBean getTripPlan(String resultId, TimedPlaceBean destination) throws ServiceException {

    if (!resultId.equals(_session.getResultId()))
      throw new ServiceException("internal error");

    throw new IllegalStateException("no");
    /*
    TripPlan trip = _session.getTripForStopId(destination.getStopId());
    if (trip == null)
      throw new ServiceException("internal error");

    TripBean tripBean = _tripPlannerBeanFactory.getTripAsBean(trip);
    WalkPlan walkPlan = _session.getWalkPlanForPlace(destination.getPlaceId());
    if (walkPlan == null)
      throw new ServiceException("internal error");

    WalkSegmentBean walkSegment = _tripPlannerBeanFactory.getWalkPlanAsBean(trip.getTripEndTime(), walkPlan);
    tripBean.getSegments().add(walkSegment);

    return tripBean;
    */
  }

  private Map<String, Place> getPlacesFromLocalSearchResults(List<LocalSearchResult> localResults) {
    Set<String> ids = new HashSet<String>();
    for (LocalSearchResult result : localResults)
      ids.add(result.getId());
    List<Place> places = _commonDao.getPlacesByIds(ids);
    Map<String, Place> placesById = CollectionsLibrary.mapToValue(places, "id", String.class);
    return placesById;
  }

  private void setSecondsOfDay(Calendar calendar, int seconds) {
    int hours = seconds / (60 * 60);
    seconds -= hours * 60 * 60;
    int minutes = seconds % 60;
    seconds -= seconds * 60;
    calendar.set(Calendar.HOUR_OF_DAY, hours);
    calendar.set(Calendar.MINUTE, minutes);
    calendar.set(Calendar.SECOND, seconds);
    calendar.set(Calendar.MILLISECOND, 0);
  }
}
