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
import org.onebusaway.federations.annotations.FederatedByAgencyIdMethod;
import org.onebusaway.federations.annotations.FederatedByEntityIdMethod;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureForStopQueryBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
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
import org.onebusaway.transit_data.model.VehicleStatusBean;
import org.onebusaway.transit_data.model.blocks.BlockBean;
import org.onebusaway.transit_data.model.blocks.BlockInstanceBean;
import org.onebusaway.transit_data.model.blocks.ScheduledBlockLocationBean;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.transit_data.model.problems.StopProblemReportBean;
import org.onebusaway.transit_data.model.problems.StopProblemReportQueryBean;
import org.onebusaway.transit_data.model.problems.StopProblemReportSummaryBean;
import org.onebusaway.transit_data.model.problems.TripProblemReportBean;
import org.onebusaway.transit_data.model.problems.TripProblemReportQueryBean;
import org.onebusaway.transit_data.model.problems.TripProblemReportSummaryBean;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordBean;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordQueryBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data.model.tripplanning.ConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.ItinerariesBean;
import org.onebusaway.transit_data.model.tripplanning.TransitShedConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.VertexBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
import org.onebusaway.transit_data.model.trips.TripForVehicleQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForAgencyQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.beans.AgencyBeanService;
import org.onebusaway.transit_data_federation.services.beans.ArrivalsAndDeparturesBeanService;
import org.onebusaway.transit_data_federation.services.beans.BlockBeanService;
import org.onebusaway.transit_data_federation.services.beans.ItinerariesBeanService;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.onebusaway.transit_data_federation.services.beans.RoutesBeanService;
import org.onebusaway.transit_data_federation.services.beans.ServiceAlertsBeanService;
import org.onebusaway.transit_data_federation.services.beans.ShapeBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopScheduleBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopWithArrivalsAndDeparturesBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopsBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripDetailsBeanService;
import org.onebusaway.transit_data_federation.services.beans.VehicleStatusBeanService;
import org.onebusaway.transit_data_federation.services.oba.OneBusAwayService;
import org.onebusaway.transit_data_federation.services.reporting.UserReportingService;
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
  private ArrivalsAndDeparturesBeanService _arrivalsAndDeparturesBeanService;

  @Autowired
  private StopsBeanService _stopsBeanService;

  @Autowired
  private RoutesBeanService _routesBeanService;

  @Autowired
  private TripBeanService _tripBeanService;

  @Autowired
  private TripDetailsBeanService _tripDetailsBeanService;

  @Autowired
  private BlockBeanService _blockBeanService;

  @Autowired
  private ItinerariesBeanService _itinerariesBeanService;

  @Autowired
  private OneBusAwayService _oneBusAwayService;

  @Autowired
  private ShapeBeanService _shapeBeanService;

  @Autowired
  private ServiceAlertsBeanService _serviceAlertsBeanService;

  @Autowired
  private UserReportingService _userReportingService;

  @Autowired
  private VehicleStatusBeanService _vehicleStatusBeanService;

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
      String stopId, ArrivalsAndDeparturesQueryBean query)
      throws ServiceException {
    AgencyAndId id = convertAgencyAndId(stopId);
    return _stopWithArrivalsAndDepaturesBeanService.getArrivalsAndDeparturesByStopId(
        id, query);
  }

  @Override
  public StopsWithArrivalsAndDeparturesBean getStopsWithArrivalsAndDepartures(
      Collection<String> stopIds, ArrivalsAndDeparturesQueryBean query)
      throws ServiceException {
    Set<AgencyAndId> ids = convertAgencyAndIds(stopIds);
    return _stopWithArrivalsAndDepaturesBeanService.getArrivalsAndDeparturesForStopIds(
        ids, query);
  }

  @Override
  public ArrivalAndDepartureBean getArrivalAndDepartureForStop(
      ArrivalAndDepartureForStopQueryBean query) throws ServiceException {

    AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(query.getStopId());
    int stopSequence = query.getStopSequence();
    AgencyAndId tripId = AgencyAndIdLibrary.convertFromString(query.getTripId());
    long serviceDate = query.getServiceDate();
    AgencyAndId vehicleId = AgencyAndIdLibrary.convertFromString(query.getVehicleId());
    long time = query.getTime();

    return _arrivalsAndDeparturesBeanService.getArrivalAndDepartureForStop(
        stopId, stopSequence, tripId, serviceDate, vehicleId, time);
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
  public TripDetailsBean getSingleTripDetails(TripDetailsQueryBean query)
      throws ServiceException {
    return _tripDetailsBeanService.getTripForId(query);
  }

  @Override
  public ListBean<TripDetailsBean> getTripDetails(TripDetailsQueryBean query)
      throws ServiceException {
    return _tripDetailsBeanService.getTripsForId(query);
  }

  @Override
  public ListBean<TripDetailsBean> getTripsForBounds(
      TripsForBoundsQueryBean query) {
    return _tripDetailsBeanService.getTripsForBounds(query);
  }

  @Override
  public ListBean<TripDetailsBean> getTripsForRoute(TripsForRouteQueryBean query) {
    return _tripDetailsBeanService.getTripsForRoute(query);
  }

  @Override
  public ListBean<TripDetailsBean> getTripsForAgency(
      TripsForAgencyQueryBean query) {
    return _tripDetailsBeanService.getTripsForAgency(query);
  }

  @Override
  public BlockBean getBlockForId(String blockId) {
    AgencyAndId id = AgencyAndIdLibrary.convertFromString(blockId);
    return _blockBeanService.getBlockForId(id);
  }

  @Override
  public BlockInstanceBean getBlockInstance(String blockId, long serviceDate) {
    AgencyAndId id = AgencyAndIdLibrary.convertFromString(blockId);
    return _blockBeanService.getBlockInstance(id, serviceDate);
  }

  @Override
  public ScheduledBlockLocationBean getScheduledBlockLocationFromScheduledTime(
      String blockId, long serviceDate, int scheduledTime) {
    AgencyAndId id = AgencyAndIdLibrary.convertFromString(blockId);
    return _blockBeanService.getScheduledBlockLocationFromScheduledTime(id,
        serviceDate, scheduledTime);
  }

  @Override
  public VehicleStatusBean getVehicleForAgency(String vehicleId, long time) {
    AgencyAndId vid = AgencyAndIdLibrary.convertFromString(vehicleId);
    return _vehicleStatusBeanService.getVehicleForId(vid, time);
  }

  @Override
  public ListBean<VehicleStatusBean> getAllVehiclesForAgency(String agencyId,
      long time) {
    return _vehicleStatusBeanService.getAllVehiclesForAgency(agencyId, time);
  }

  @Override
  public VehicleLocationRecordBean getVehicleLocationRecordForVehicleId(
      String vehicleId, long targetTime) {
    AgencyAndId id = convertAgencyAndId(vehicleId);
    return _vehicleStatusBeanService.getVehicleLocationRecordForVehicleId(id,
        targetTime);
  }

  @Override
  public TripDetailsBean getTripDetailsForVehicleAndTime(
      TripForVehicleQueryBean query) {
    AgencyAndId id = convertAgencyAndId(query.getVehicleId());
    return _tripDetailsBeanService.getTripForVehicle(id,
        query.getTime().getTime(), query.getInclusion());
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

  @Override
  public ItinerariesBean getItinerariesBetween(CoordinatePoint from,
      CoordinatePoint to, long time, ConstraintsBean constraints)
      throws ServiceException {
    return _itinerariesBeanService.getItinerariesBetween(from, to, time,
        constraints);
  }

  @Override
  public ListBean<VertexBean> getStreetGraphForRegion(double latFrom,
      double lonFrom, double latTo, double lonTo) throws ServiceException {
    return _itinerariesBeanService.getStreetGraphForRegion(latFrom, lonFrom,
        latTo, lonTo);
  }

  @Override
  public MinTravelTimeToStopsBean getMinTravelTimeToStopsFrom(
      CoordinatePoint location, long time,
      TransitShedConstraintsBean constraints) throws ServiceException {
    return _itinerariesBeanService.getMinTravelTimeToStopsFrom(location, time,
        constraints);
  }

  public List<TimedPlaceBean> getLocalPaths(String agencyId,
      ConstraintsBean constraints,
      MinTravelTimeToStopsBean minTravelTimeToStops,
      List<LocalSearchResult> localResults) throws ServiceException {
    return _oneBusAwayService.getLocalPaths(constraints, minTravelTimeToStops,
        localResults);
  }

  /****
   * 
   ****/

  public ListBean<VehicleLocationRecordBean> getVehicleLocationRecords(
      VehicleLocationRecordQueryBean query) {
    return _vehicleStatusBeanService.getVehicleLocations(query);
  }

  @Override
  public void submitVehicleLocation(VehicleLocationRecordBean record) {
    _vehicleStatusBeanService.submitVehicleLocation(record);
  }

  @FederatedByEntityIdMethod
  public void resetVehicleLocation(String vehicleId) {
    AgencyAndId id = AgencyAndIdLibrary.convertFromString(vehicleId);
    _vehicleStatusBeanService.resetVehicleLocation(id);
  }

  /****
   * Service Alert Methods
   ****/

  @Override
  public SituationBean createServiceAlert(String agencyId,
      SituationBean situation) {
    return _serviceAlertsBeanService.createServiceAlert(agencyId, situation);
  }

  @Override
  public void updateServiceAlert(SituationBean situation) {
    _serviceAlertsBeanService.updateServiceAlert(situation);
  }

  @Override
  public SituationBean getServiceAlertForId(String situationId) {
    AgencyAndId id = AgencyAndIdLibrary.convertFromString(situationId);
    return _serviceAlertsBeanService.getServiceAlertForId(id);
  }

  @Override
  public void removeServiceAlert(String situationId) {
    AgencyAndId id = AgencyAndIdLibrary.convertFromString(situationId);
    _serviceAlertsBeanService.removeServiceAlert(id);
  }

  @Override
  public ListBean<SituationBean> getAllServiceAlertsForAgencyId(String agencyId) {
    List<SituationBean> situations = _serviceAlertsBeanService.getAllSituationsForAgencyId(agencyId);
    return new ListBean<SituationBean>(situations, false);
  }

  @Override
  public void removeAllServiceAlertsForAgencyId(String agencyId) {
    _serviceAlertsBeanService.removeAllSituationsForAgencyId(agencyId);
  }

  @Override
  public ListBean<SituationBean> getServiceAlerts(SituationQueryBean query) {
    List<SituationBean> situations = _serviceAlertsBeanService.getAllSituationsForAgencyId(query.getAgencyId());
    return new ListBean<SituationBean>(situations, false);
  }

  @Override
  public void reportProblemWithStop(StopProblemReportBean problem) {
    _userReportingService.reportProblemWithStop(problem);
  }

  @Override
  public void reportProblemWithTrip(TripProblemReportBean problem) {
    _userReportingService.reportProblemWithTrip(problem);
  }

  @Override
  public ListBean<StopProblemReportSummaryBean> getStopProblemReportSummaries(
      StopProblemReportQueryBean query) {
    return _userReportingService.getStopProblemReportSummaries(query);
  }

  @Override
  public ListBean<TripProblemReportSummaryBean> getTripProblemReportSummaries(
      TripProblemReportQueryBean query) {
    return _userReportingService.getTripProblemReportSummaries(query);
  }

  @Override
  @FederatedByAgencyIdMethod()
  public ListBean<StopProblemReportBean> getStopProblemReports(
      StopProblemReportQueryBean query) {
    return _userReportingService.getStopProblemReports(query);
  }

  @Override
  public ListBean<TripProblemReportBean> getTripProblemReports(
      TripProblemReportQueryBean query) {
    return _userReportingService.getTripProblemReports(query);
  }

  @Override
  public List<StopProblemReportBean> getAllStopProblemReportsForStopId(
      String stopId) {
    return _userReportingService.getAllStopProblemReportsForStopId(convertAgencyAndId(stopId));
  }

  @Override
  public List<TripProblemReportBean> getAllTripProblemReportsForTripId(
      String tripId) {
    return _userReportingService.getAllTripProblemReportsForTripId(convertAgencyAndId(tripId));
  }

  @Override
  public StopProblemReportBean getStopProblemReportForStopIdAndId(
      String stopId, long id) {
    return _userReportingService.getStopProblemReportForId(id);
  }

  @Override
  public TripProblemReportBean getTripProblemReportForTripIdAndId(
      String tripId, long id) {
    return _userReportingService.getTripProblemReportForId(id);
  }

  @Override
  public void deleteStopProblemReportForStopIdAndId(String stopId, long id) {
    _userReportingService.deleteStopProblemReportForId(id);
  }

  @Override
  public void updateTripProblemReport(TripProblemReportBean tripProblemReport) {
    _userReportingService.updateTripProblemReport(tripProblemReport);
  }

  @Override
  public void deleteTripProblemReportForTripIdAndId(String tripId, long id) {
    _userReportingService.deleteTripProblemReportForId(id);
  }

  @Override
  public List<String> getAllTripProblemReportLabels() {
    return _userReportingService.getAllTripProblemReportLabels();
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
