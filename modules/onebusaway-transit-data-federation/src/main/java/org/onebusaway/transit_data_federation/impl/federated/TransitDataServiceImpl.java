package org.onebusaway.transit_data_federation.impl.federated;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.RoutesQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopCalendarDayBean;
import org.onebusaway.transit_data.model.StopRouteScheduleBean;
import org.onebusaway.transit_data.model.StopScheduleBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.TripStatusBean;
import org.onebusaway.transit_data.model.TripDetailsBean;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlanBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlannerConstraintsBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.beans.AgencyBeanService;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.onebusaway.transit_data_federation.services.beans.RoutesBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopScheduleBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopWithArrivalsAndDeparturesBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopsBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripPlannerBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripPositionBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripStatusBeanService;
import org.onebusaway.transit_data_federation.services.oba.OneBusAwayService;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
class TransitDataServiceImpl implements TransitDataService {

  @Autowired
  private AgencyBeanService _agencyBeanService;

  @Autowired
  private AgencyService _agencyService;

  @Autowired
  private StopBeanService _stopBeanService;

  @Autowired
  private RouteBeanService _routeBeanService;

  @Autowired
  private StopScheduleBeanService _stopScheduleBeanService;

  @Autowired
  private StopWithArrivalsAndDeparturesBeanService _stopWithArrivalsAndDepaturesBeanService;

  @Autowired
  private TripStatusBeanService _tripStatusBeanService;

  @Autowired
  private StopsBeanService _stopsBeanService;

  @Autowired
  private RoutesBeanService _routesBeanService;

  @Autowired
  private TripPositionBeanService _tripPositionBeanService;

  @Autowired
  private TripPlannerBeanService _tripPlannerBeanService;

  @Autowired
  private OneBusAwayService _oneBusAwayService;

  /****
   * {@link TransitDataService} Interface
   ****/

  @Override
  public Map<String, List<CoordinateBounds>> getAgencyIdsWithCoverageArea()
      throws ServiceException {
    Map<String, CoordinateBounds> agencyIdsAndCoverageAreas = _agencyService.getAgencyIdsAndCoverageAreas();
    Map<String, List<CoordinateBounds>> result = new HashMap<String, List<CoordinateBounds>>();
    for (Map.Entry<String, CoordinateBounds> entry : agencyIdsAndCoverageAreas.entrySet()) {
      String agencyId = entry.getKey();
      CoordinateBounds bounds = entry.getValue();
      List<CoordinateBounds> coverage = Arrays.asList(bounds);
      result.put(agencyId, coverage);
    }
    return result;
  }

  @Override
  public List<AgencyWithCoverageBean> getAgenciesWithCoverage()
      throws ServiceException {

    Map<String, CoordinatePoint> agencyIdsAndCenterPoints = _agencyService.getAgencyIdsAndCenterPoints();

    List<AgencyWithCoverageBean> beans = new ArrayList<AgencyWithCoverageBean>();

    for (Map.Entry<String, CoordinatePoint> entry : agencyIdsAndCenterPoints.entrySet()) {

      String agencyId = entry.getKey();
      CoordinatePoint point = entry.getValue();

      AgencyBean agencyBean = _agencyBeanService.getAgencyForId(agencyId);
      if (agencyBean == null)
        throw new ServiceException();

      AgencyWithCoverageBean bean = new AgencyWithCoverageBean();
      bean.setAgency(agencyBean);
      bean.setLat(point.getLat());
      bean.setLon(point.getLon());

      beans.add(bean);
    }
    return beans;
  }

  @Override
  public StopScheduleBean getScheduleForStop(String stopId, Date date)
      throws ServiceException {

    StopScheduleBean bean = new StopScheduleBean();
    bean.setDate(date);

    AgencyAndId id = convertAgencyAndId(stopId);
    StopBean stopBean = _stopBeanService.getStopForId(id);
    if (stopBean == null)
      return null;
    bean.setStop(stopBean);

    List<StopRouteScheduleBean> routes = _stopScheduleBeanService.getScheduledArrivalsForStopAndDate(
        id, date);
    bean.setRoutes(routes);

    List<StopCalendarDayBean> calendarDays = _stopScheduleBeanService.getCalendarForStop(id);
    bean.setCalendarDays(calendarDays);

    return bean;
  }

  @Override
  public StopsBean getStopsByBounds(double lat1, double lon1, double lat2,
      double lon2, int maxCount) throws ServiceException {
    return _stopsBeanService.getStopsByBounds(lat1, lon1, lat2, lon2, maxCount);
  }

  @Override
  public StopsBean getStopsByBoundsAndQuery(double lat1, double lon1,
      double lat2, double lon2, String query, int maxCount)
      throws ServiceException {

    return _stopsBeanService.getStopsByBoundsAndQuery(lat1, lon1, lat2, lon2,
        query, maxCount);
  }

  @Override
  public StopBean getStop(String stopId) throws ServiceException {
    AgencyAndId id = convertAgencyAndId(stopId);
    return _stopBeanService.getStopForId(id);
  }

  @Override
  public StopWithArrivalsAndDeparturesBean getStopWithArrivalsAndDepartures(
      String stopId, Date date) throws ServiceException {
    AgencyAndId id = convertAgencyAndId(stopId);
    return _stopWithArrivalsAndDepaturesBeanService.getArrivalsAndDeparturesByStopId(
        id, new Date());
  }

  @Override
  public StopsWithArrivalsAndDeparturesBean getStopsWithArrivalsAndDepartures(
      Collection<String> stopIds, Date time) throws ServiceException {
    Set<AgencyAndId> ids = convertAgencyAndIds(stopIds);
    return _stopWithArrivalsAndDepaturesBeanService.getArrivalsAndDeparturesForStopIds(
        ids, time);
  }

  @Override
  public RouteBean getRouteForId(String routeId) throws ServiceException {
    return _routeBeanService.getRouteForId(convertAgencyAndId(routeId));
  }

  @Override
  public StopsForRouteBean getStopsForRoute(String routeId) {
    return _routeBeanService.getStopsForRoute(convertAgencyAndId(routeId));
  }

  @Override
  public ListBean<TripStatusBean> getTripsForBounds(
      CoordinateBounds bounds, long time) {
    return _tripPositionBeanService.getActiveTripForBounds(bounds, time);
  }

  @Override
  public RoutesBean getRoutes(RoutesQueryBean query) throws ServiceException {
    return _routesBeanService.getRoutesForQuery(query);
  }

  @Override
  public TripDetailsBean getTripDetails(String tripId) throws ServiceException {
    return _tripStatusBeanService.getTripStatus(convertAgencyAndId(tripId));
  }

  public List<TripPlanBean> getTripsBetween(double latFrom, double lonFrom,
      double latTo, double lonTo, TripPlannerConstraintsBean constraints)
      throws ServiceException {
    return _tripPlannerBeanService.getTripsBetween(latFrom, lonFrom, latTo,
        lonTo, constraints);
  }

  public MinTravelTimeToStopsBean getMinTravelTimeToStopsFrom(double lat,
      double lon, OneBusAwayConstraintsBean constraints)
      throws ServiceException {

    return _tripPlannerBeanService.getMinTravelTimeToStopsFrom(lat, lon,
        constraints);
  }

  /****
   * Private Methods
   ****/

  private AgencyAndId convertAgencyAndId(String id) {
    return AgencyAndIdLibrary.convertFromString(id);
  }

  private Set<AgencyAndId> convertAgencyAndIds(Iterable<String> ids) {
    Set<AgencyAndId> converted = new HashSet<AgencyAndId>();
    for (String id : ids)
      converted.add(convertAgencyAndId(id));
    return converted;
  }

  public List<TimedPlaceBean> getLocalPaths(String agencyId,
      OneBusAwayConstraintsBean constraints,
      MinTravelTimeToStopsBean minTravelTimeToStops,
      List<LocalSearchResult> localResults) {
    return _oneBusAwayService.getLocalPaths(constraints, minTravelTimeToStops,
        localResults);
  }

}
