package org.onebusaway.transit_data_federation.impl.federated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopCalendarDaysBean;
import org.onebusaway.transit_data.model.StopRouteScheduleBean;
import org.onebusaway.transit_data.model.StopScheduleBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.TripBean;
import org.onebusaway.transit_data.model.TripDetailsBean;
import org.onebusaway.transit_data.model.TripStatusBean;
import org.onebusaway.transit_data.model.TripStopTimesBean;
import org.onebusaway.transit_data.model.TripsForBoundsQueryBean;
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
import org.onebusaway.transit_data_federation.services.beans.ShapeBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopScheduleBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopWithArrivalsAndDeparturesBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopsBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripPlannerBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripStatusBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripStopTimesBeanService;
import org.onebusaway.transit_data_federation.services.oba.OneBusAwayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
  private StopsBeanService _stopsBeanService;

  @Autowired
  private RoutesBeanService _routesBeanService;

  @Autowired
  private TripBeanService _tripBeanService;

  @Autowired
  private TripStopTimesBeanService _tripStopTimesBeanService;

  @Autowired
  private TripStatusBeanService _tripStatusBeanService;

  @Autowired
  private TripPlannerBeanService _tripPlannerBeanService;

  @Autowired
  private OneBusAwayService _oneBusAwayService;

  @Autowired
  private ShapeBeanService _shapeBeanService;

  /****
   * {@link TransitDataService} Interface
   ****/

  @Override
  public Map<String, List<CoordinateBounds>> getAgencyIdsWithCoverageArea() {

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

    Map<String, CoordinateBounds> agencyIdsAndCoverageAreas = _agencyService.getAgencyIdsAndCoverageAreas();
    List<AgencyWithCoverageBean> beans = new ArrayList<AgencyWithCoverageBean>();

    for (Map.Entry<String, CoordinateBounds> entry : agencyIdsAndCoverageAreas.entrySet()) {

      String agencyId = entry.getKey();
      CoordinateBounds bounds = entry.getValue();

      AgencyBean agencyBean = _agencyBeanService.getAgencyForId(agencyId);
      if (agencyBean == null)
        throw new ServiceException("agency not found: " + agencyId);

      AgencyWithCoverageBean bean = new AgencyWithCoverageBean();
      bean.setAgency(agencyBean);
      bean.setLat((bounds.getMaxLat() + bounds.getMinLat()) / 2);
      bean.setLon((bounds.getMaxLon() + bounds.getMinLon()) / 2);
      bean.setLatSpan(bounds.getMaxLat() - bounds.getMinLat());
      bean.setLonSpan(bounds.getMaxLon() - bounds.getMinLon());

      beans.add(bean);
    }
    return beans;
  }

  @Override
  public AgencyBean getAgency(String agencyId) throws ServiceException {
    return _agencyBeanService.getAgencyForId(agencyId);
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

    ServiceDate serviceDate = new ServiceDate(date);
    List<StopRouteScheduleBean> routes = _stopScheduleBeanService.getScheduledArrivalsForStopAndDate(
        id, serviceDate);
    bean.setRoutes(routes);

    StopCalendarDaysBean calendarDays = _stopScheduleBeanService.getCalendarForStop(id);
    bean.setCalendarDays(calendarDays);

    return bean;
  }

  @Override
  public StopsBean getStops(SearchQueryBean query) throws ServiceException {
    return _stopsBeanService.getStops(query);
  }

  @Override
  public StopBean getStop(String stopId) throws ServiceException {
    AgencyAndId id = convertAgencyAndId(stopId);
    return _stopBeanService.getStopForId(id);
  }

  @Override
  public ListBean<String> getStopIdsForAgencyId(String agencyId) {
    return _stopsBeanService.getStopsIdsForAgencyId(agencyId);
  }

  @Override
  public StopWithArrivalsAndDeparturesBean getStopWithArrivalsAndDepartures(
      String stopId, Date timeFrom, Date timeTo) throws ServiceException {
    AgencyAndId id = convertAgencyAndId(stopId);
    return _stopWithArrivalsAndDepaturesBeanService.getArrivalsAndDeparturesByStopId(
        id, timeFrom, timeTo);
  }

  @Override
  public StopsWithArrivalsAndDeparturesBean getStopsWithArrivalsAndDepartures(
      Collection<String> stopIds, Date timeFrom, Date timeTo)
      throws ServiceException {
    Set<AgencyAndId> ids = convertAgencyAndIds(stopIds);
    return _stopWithArrivalsAndDepaturesBeanService.getArrivalsAndDeparturesForStopIds(
        ids, timeFrom, timeTo);
  }

  @Override
  public RouteBean getRouteForId(String routeId) throws ServiceException {
    return _routeBeanService.getRouteForId(convertAgencyAndId(routeId));
  }

  @Override
  public ListBean<String> getRouteIdsForAgencyId(String agencyId) {
    return _routesBeanService.getRouteIdsForAgencyId(agencyId);
  }

  @Override
  public StopsForRouteBean getStopsForRoute(String routeId) {
    return _routeBeanService.getStopsForRoute(convertAgencyAndId(routeId));
  }

  @Override
  public TripBean getTrip(String tripId) throws ServiceException {
    return _tripBeanService.getTripForId(convertAgencyAndId(tripId));
  }

  @Override
  public TripDetailsBean getTripDetails(String tripId) throws ServiceException {
    AgencyAndId id = convertAgencyAndId(tripId);
    TripBean trip = _tripBeanService.getTripForId(id);
    if (trip == null)
      return null;
    TripStopTimesBean stopTimes = _tripStopTimesBeanService.getStopTimesForTrip(id);
    return new TripDetailsBean(trip, stopTimes);
  }

  @Override
  public TripDetailsBean getSpecificTripDetails(String tripId, Date serviceDate,
      Date time) throws ServiceException {
    AgencyAndId id = convertAgencyAndId(tripId);
    TripBean trip = _tripBeanService.getTripForId(id);
    if (trip == null)
      return null;
    TripStopTimesBean stopTimes = _tripStopTimesBeanService.getStopTimesForTrip(id);
    TripStatusBean status = _tripStatusBeanService.getTripStatusForTripId(id,
        serviceDate.getTime(), time.getTime());
    return new TripDetailsBean(trip, stopTimes, status);
  }

  @Override
  public ListBean<TripDetailsBean> getTripsForBounds(
      TripsForBoundsQueryBean query) {
    return _tripStatusBeanService.getActiveTripForBounds(query);
  }

  @Override
  public RoutesBean getRoutes(SearchQueryBean query) throws ServiceException {
    return _routesBeanService.getRoutesForQuery(query);
  }

  @Override
  public EncodedPolylineBean getShapeForId(String shapeId) {
    AgencyAndId id = convertAgencyAndId(shapeId);
    return _shapeBeanService.getPolylineForShapeId(id);
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

  public List<TimedPlaceBean> getLocalPaths(String agencyId,
      OneBusAwayConstraintsBean constraints,
      MinTravelTimeToStopsBean minTravelTimeToStops,
      List<LocalSearchResult> localResults) throws ServiceException {
    return _oneBusAwayService.getLocalPaths(constraints, minTravelTimeToStops,
        localResults);
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

}
