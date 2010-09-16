package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.TripStopTimesBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.model.trips.TripsForAgencyQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripDetailsBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripStopTimesBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockStatusService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TripStatusBeanServiceImpl implements TripDetailsBeanService {

  private TransitGraphDao _transitGraphDao;

  private BlockStatusService _blockStatusService;

  private TripBeanService _tripBeanService;

  private TripStopTimesBeanService _tripStopTimesBeanService;

  private StopBeanService _stopBeanService;

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

  /****
   * {@link TripStatusBeanService} Interface
   ****/

  @Override
  public TripDetailsBean getTripStatusForTripId(AgencyAndId tripId,
      long serviceDate, long time, TripDetailsInclusionBean inclusion) {

    TripEntry tripEntry = _transitGraphDao.getTripEntryForId(tripId);
    if (tripEntry == null)
      return null;

    BlockLocation blockLocation = _blockStatusService.getBlock(
        tripEntry.getBlock().getId(), serviceDate, time);

    return getTripEntryAndBlockLocationAsTripDetails(tripEntry, blockLocation,
        inclusion);
  }

  @Override
  public TripDetailsBean getTripStatusForVehicleAndTime(AgencyAndId vehicleId,
      long time, TripDetailsInclusionBean inclusion) {

    BlockLocation blockLocation = _blockStatusService.getBlockForVehicle(
        vehicleId, time);
    return getBlockLocationAsTripDetails(blockLocation, inclusion);
  }

  @Override
  public ListBean<TripDetailsBean> getActiveTripForBounds(
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

    TripEntry tripEntry = blockLocation.getActiveTrip();

    return getTripEntryAndBlockLocationAsTripDetails(tripEntry, blockLocation,
        inclusion);
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
      stopTimes = _tripStopTimesBeanService.getStopTimesForTrip(tripEntry.getId());
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

  private TripStatusBean getBlockLocationAsStatusBean(
      BlockLocation blockLocation) {

    TripStatusBean bean = new TripStatusBean();
    bean.setStatus("default");

    if (blockLocation != null) {

      BlockInstance blockInstance = blockLocation.getBlockInstance();
      long serviceDate = blockInstance.getServiceDate();

      bean.setServiceDate(serviceDate);

      bean.setLastUpdateTime(blockLocation.getLastUpdateTime());

      CoordinatePoint location = blockLocation.getLocation();
      bean.setLocation(location);
      bean.setScheduleDeviation(blockLocation.getScheduleDeviation());

      TripEntry activeTrip = blockLocation.getActiveTrip();
      bean.setScheduledDistanceAlongTrip(blockLocation.getScheduledDistanceAlongBlock()
          - activeTrip.getDistanceAlongBlock());
      bean.setDistanceAlongTrip(blockLocation.getDistanceAlongBlock()
          - activeTrip.getDistanceAlongBlock());
      bean.setTotalDistanceAlongTrip(activeTrip.getTotalTripDistance());

      StopTimeEntry stop = blockLocation.getClosestStop();
      if (stop != null) {
        StopBean stopBean = _stopBeanService.getStopForId(stop.getStop().getId());
        bean.setClosestStop(stopBean);
        bean.setClosestStopTimeOffset(blockLocation.getClosestStopTimeOffset());
      }

      bean.setPredicted(blockLocation.isPredicted());

      AgencyAndId vid = blockLocation.getVehicleId();
      if (vid != null)
        bean.setVehicleId(ApplicationBeanLibrary.getId(vid));

    } else {
      bean.setPredicted(false);
    }

    return bean;
  }
}
