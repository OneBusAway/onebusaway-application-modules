package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.EVehiclePhase;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.TripStopTimesBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.model.trips.TripsForAgencyQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripDetailsBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripStopTimesBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockStatusService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlertsService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TripStatusBeanServiceImpl implements TripDetailsBeanService {

  private TransitGraphDao _transitGraphDao;

  private BlockStatusService _blockStatusService;

  private TripBeanService _tripBeanService;

  private TripStopTimesBeanService _tripStopTimesBeanService;

  private StopBeanService _stopBeanService;

  private ServiceAlertsService _serviceAlertBeanService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setBlockStatusService(BlockStatusService blockStatusService) {
    _blockStatusService = blockStatusService;
  }

  @Autowired
  public void setTripBeanService(TripBeanService tripBeanService) {
    _tripBeanService = tripBeanService;
  }

  @Autowired
  public void setTripStopTimesBeanService(
      TripStopTimesBeanService tripStopTimesBeanService) {
    _tripStopTimesBeanService = tripStopTimesBeanService;
  }

  @Autowired
  public void setStopBeanService(StopBeanService stopBeanService) {
    _stopBeanService = stopBeanService;
  }

  @Autowired
  public void setServiceAlertBeanService(
      ServiceAlertsService serviceAlertBeanService) {
    _serviceAlertBeanService = serviceAlertBeanService;
  }

  /****
   * {@link TripStatusBeanService} Interface
   ****/

  @Override
  public TripDetailsBean getTripForId(TripDetailsQueryBean query) {

    AgencyAndId tripId = AgencyAndIdLibrary.convertFromString(query.getTripId());
    long serviceDate = query.getServiceDate();
    long time = query.getTime();

    TripEntry tripEntry = _transitGraphDao.getTripEntryForId(tripId);
    if (tripEntry == null)
      return null;

    BlockLocation location = _blockStatusService.getBlock(
        tripEntry.getBlock().getId(), serviceDate, time);

    return getTripEntryAndBlockLocationAsTripDetails(tripEntry, location,
        query.getInclusion());
  }

  @Override
  public ListBean<TripDetailsBean> getTripsForId(TripDetailsQueryBean query) {

    AgencyAndId tripId = AgencyAndIdLibrary.convertFromString(query.getTripId());
    long serviceDate = query.getServiceDate();
    long time = query.getTime();

    TripEntry tripEntry = _transitGraphDao.getTripEntryForId(tripId);
    if (tripEntry == null)
      return new ListBean<TripDetailsBean>();

    List<BlockLocation> locations = _blockStatusService.getBlocks(
        tripEntry.getBlock().getId(), serviceDate, time);

    return getBlockLocationsAsTripDetails(locations, query.getInclusion());
  }

  @Override
  public TripDetailsBean getTripForVehicle(AgencyAndId vehicleId, long time,
      TripDetailsInclusionBean inclusion) {

    BlockLocation blockLocation = _blockStatusService.getBlockForVehicle(
        vehicleId, time);
    return getBlockLocationAsTripDetails(blockLocation, inclusion);
  }

  @Override
  public ListBean<TripDetailsBean> getTripsForBounds(
      TripsForBoundsQueryBean query) {
    List<BlockLocation> locations = _blockStatusService.getBlocksForBounds(
        query.getBounds(), query.getTime());
    return getBlockLocationsAsTripDetails(locations, query.getInclusion());
  }

  @Override
  public ListBean<TripDetailsBean> getTripsForRoute(TripsForRouteQueryBean query) {
    AgencyAndId routeId = AgencyAndIdLibrary.convertFromString(query.getRouteId());
    List<BlockLocation> locations = _blockStatusService.getBlocksForRoute(
        routeId, query.getTime());
    return getBlockLocationsAsTripDetails(locations, query.getInclusion());
  }

  @Override
  public ListBean<TripDetailsBean> getTripsForAgency(
      TripsForAgencyQueryBean query) {
    List<BlockLocation> locations = _blockStatusService.getBlocksForAgency(
        query.getAgencyId(), query.getTime());
    return getBlockLocationsAsTripDetails(locations, query.getInclusion());
  }

  @Override
  public TripStatusBean getBlockLocationAsStatusBean(BlockLocation blockLocation) {

    TripStatusBean bean = new TripStatusBean();
    bean.setStatus("default");

    if (blockLocation != null) {

      BlockInstance blockInstance = blockLocation.getBlockInstance();
      long serviceDate = blockInstance.getServiceDate();

      bean.setServiceDate(serviceDate);

      bean.setLastUpdateTime(blockLocation.getLastUpdateTime());

      bean.setLastKnownLocation(blockLocation.getLastKnownLocation());
      bean.setLastKnownOrientation(blockLocation.getLastKnownOrientation());

      bean.setLocation(blockLocation.getLocation());
      bean.setOrientation(blockLocation.getOrientation());

      bean.setLastKnownLocation(blockLocation.getLastKnownLocation());
      if (blockLocation.isLastKnownOrientationSet())
        bean.setLastKnownOrientation(blockLocation.getLastKnownOrientation());

      bean.setScheduleDeviation(blockLocation.getScheduleDeviation());

      BlockTripEntry activeBlockTrip = blockLocation.getActiveTrip();

      if (activeBlockTrip != null) {
        bean.setScheduledDistanceAlongTrip(blockLocation.getScheduledDistanceAlongBlock()
            - activeBlockTrip.getDistanceAlongBlock());
        bean.setDistanceAlongTrip(blockLocation.getDistanceAlongBlock()
            - activeBlockTrip.getDistanceAlongBlock());
        TripEntry activeTrip = activeBlockTrip.getTrip();
        bean.setTotalDistanceAlongTrip(activeTrip.getTotalTripDistance());
      }

      BlockStopTimeEntry closestStop = blockLocation.getClosestStop();
      if (closestStop != null) {
        StopTimeEntry stopTime = closestStop.getStopTime();
        StopBean stopBean = _stopBeanService.getStopForId(stopTime.getStop().getId());
        bean.setClosestStop(stopBean);
        bean.setClosestStopTimeOffset(blockLocation.getClosestStopTimeOffset());
      }

      BlockStopTimeEntry nextStop = blockLocation.getNextStop();
      if (nextStop != null) {
        StopTimeEntry stopTime = nextStop.getStopTime();
        StopBean stopBean = _stopBeanService.getStopForId(stopTime.getStop().getId());
        bean.setNextStop(stopBean);
        bean.setNextStopTimeOffset(blockLocation.getNextStopTimeOffset());
      }

      EVehiclePhase phase = blockLocation.getPhase();
      if (phase != null)
        bean.setPhase(phase.toString().toLowerCase());

      String status = blockLocation.getStatus();
      if (status != null)
        bean.setStatus(status);

      bean.setPredicted(blockLocation.isPredicted());

      AgencyAndId vid = blockLocation.getVehicleId();
      if (vid != null)
        bean.setVehicleId(ApplicationBeanLibrary.getId(vid));

      if (activeBlockTrip != null) {
        TripEntry trip = activeBlockTrip.getTrip();
        AgencyAndId lineId = trip.getRouteCollectionId();
        String lineIdAsString = AgencyAndIdLibrary.convertToString(lineId);
        List<SituationBean> situations = _serviceAlertBeanService.getSituationsForLineId(
            lineIdAsString, trip.getDirectionId());
        if (!situations.isEmpty())
          bean.setSituations(situations);
      }

    } else {
      bean.setPredicted(false);
    }

    return bean;
  }

  /****
   * Private Methods
   ****/

  private ListBean<TripDetailsBean> getBlockLocationsAsTripDetails(
      List<BlockLocation> locations, TripDetailsInclusionBean inclusion) {
    List<TripDetailsBean> tripDetails = new ArrayList<TripDetailsBean>();
    for (BlockLocation location : locations) {
      TripDetailsBean details = getBlockLocationAsTripDetails(location,
          inclusion);
      tripDetails.add(details);
    }
    return new ListBean<TripDetailsBean>(tripDetails, false);
  }

  private TripDetailsBean getBlockLocationAsTripDetails(
      BlockLocation blockLocation, TripDetailsInclusionBean inclusion) {

    if (blockLocation == null)
      return null;

    BlockTripEntry tripEntry = blockLocation.getActiveTrip();

    if (tripEntry == null) {
      System.err.println("no trip?");
      return null;
    }

    return getTripEntryAndBlockLocationAsTripDetails(tripEntry.getTrip(),
        blockLocation, inclusion);
  }

  private TripDetailsBean getTripEntryAndBlockLocationAsTripDetails(
      TripEntry tripEntry, BlockLocation blockLocation,
      TripDetailsInclusionBean inclusion) {

    TripBean trip = null;
    TripStopTimesBean stopTimes = null;
    TripStatusBean status = null;

    boolean missing = false;

    if (inclusion.isIncludeTripBean()) {
      trip = _tripBeanService.getTripForId(tripEntry.getId());
      if (trip == null)
        missing = true;
    }

    if (inclusion.isIncludeTripSchedule()) {
      if (blockLocation != null)
        stopTimes = _tripStopTimesBeanService.getStopTimesForBlockTrip(blockLocation.getActiveTrip());
      else
        stopTimes = _tripStopTimesBeanService.getStopTimesForTrip(tripEntry);
      if (stopTimes == null)
        missing = true;
    }

    if (inclusion.isIncludeTripStatus() && blockLocation != null) {
      status = getBlockLocationAsStatusBean(blockLocation);
      if (status == null)
        missing = true;
    }

    if (missing)
      return null;

    String tripId = AgencyAndIdLibrary.convertToString(tripEntry.getId());
    return new TripDetailsBean(tripId, trip, stopTimes, status);
  }

}
