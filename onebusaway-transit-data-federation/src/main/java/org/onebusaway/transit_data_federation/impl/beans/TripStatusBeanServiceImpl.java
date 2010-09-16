package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.calendar.CalendarService;
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
import org.onebusaway.transit_data_federation.services.beans.BlockStatusBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripStatusBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripStopTimesBeanService;
import org.onebusaway.transit_data_federation.services.realtime.ActiveCalendarService;
import org.onebusaway.transit_data_federation.services.realtime.BlockInstance;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.onebusaway.transit_data_federation.services.realtime.TripLocation;
import org.onebusaway.transit_data_federation.services.realtime.TripLocationService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripInstanceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO: Refactor to use {@link BlockStatusBeanService}
 * 
 * @author bdferris
 */
@Component
public class TripStatusBeanServiceImpl implements TripStatusBeanService {

  private TransitGraphDao _graph;

  private CalendarService _calendarService;

  private ActiveCalendarService _activeCalendarService;

  private BlockLocationService _blockLocationService;

  private TripLocationService _tripPositionService;

  private TripBeanService _tripBeanService;

  private TripStopTimesBeanService _tripStopTimesBeanService;

  private StopBeanService _stopBeanService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao graph) {
    _graph = graph;
  }

  @Autowired
  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  @Autowired
  public void setActive(ActiveCalendarService activeCalendarService) {
    _activeCalendarService = activeCalendarService;
  }

  @Autowired
  public void setBlockLocationService(BlockLocationService blockLocationService) {
    _blockLocationService = blockLocationService;
  }

  @Autowired
  public void setTripPositionService(TripLocationService tripPositionService) {
    _tripPositionService = tripPositionService;
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

  public TripStatusBean getTripStatusForTripId(AgencyAndId tripId,
      long serviceDate, long time) {

    TripEntry tripEntry = _graph.getTripEntryForId(tripId);
    if (tripEntry == null)
      return null;

    serviceDate = getBestServiceDateForTrip(tripId, serviceDate);

    TripInstanceProxy tripInstance = new TripInstanceProxy(tripEntry,
        serviceDate);

    TripLocation tripPosition = _tripPositionService.getPositionForTripInstance(
        tripInstance, time);

    return getTripPositionAsStatusBean(serviceDate, tripPosition);
  }

  @Override
  public TripDetailsBean getTripStatusForVehicleAndTime(AgencyAndId vehicleId,
      long time, TripDetailsInclusionBean inclusion) {

    TripLocation position = _tripPositionService.getPositionForVehicleAndTime(
        vehicleId, time);

    if (position == null)
      return null;

    TripEntry tripEntry = position.getTrip();
    AgencyAndId tripId = tripEntry.getId();

    TripDetailsBean details = new TripDetailsBean();
    details.setTripId(AgencyAndIdLibrary.convertToString(tripId));

    if (inclusion.isIncludeTripBean()) {
      TripBean trip = _tripBeanService.getTripForId(tripId);
      details.setTrip(trip);
    }

    if (inclusion.isIncludeTripSchedule()) {
      TripStopTimesBean stopTimes = _tripStopTimesBeanService.getStopTimesForTrip(tripId);
      details.setSchedule(stopTimes);
    }

    if (inclusion.isIncludeTripStatus()) {
      TripStatusBean status = getTripPositionAsStatusBean(
          position.getServiceDate(), position);
      details.setStatus(status);
    }

    return details;
  }

  @Override
  public ListBean<TripDetailsBean> getActiveTripForBounds(
      TripsForBoundsQueryBean query) {

    CoordinateBounds bounds = query.getBounds();
    long time = query.getTime();

    Map<TripInstanceProxy, TripLocation> tripsAndPositions = _tripPositionService.getScheduledTripsForBounds(
        bounds, time);
    TripDetailsInclusionBean inclusion = query.getInclusion();

    List<TripDetailsBean> results = new ArrayList<TripDetailsBean>();

    for (Map.Entry<TripInstanceProxy, TripLocation> entry : tripsAndPositions.entrySet()) {

      TripInstanceProxy tripInstance = entry.getKey();
      TripLocation tripPosition = entry.getValue();

      TripEntry trip = tripInstance.getTrip();

      TripDetailsBean details = new TripDetailsBean();
      details.setTripId(AgencyAndIdLibrary.convertToString(trip.getId()));

      if (inclusion.isIncludeTripStatus()) {
        TripStatusBean status = getTripPositionAsStatusBean(
            tripPosition.getServiceDate(), tripPosition);
        details.setStatus(status);
      }

      if (inclusion.isIncludeTripBean())
        details.setTrip(_tripBeanService.getTripForId(trip.getId()));

      if (inclusion.isIncludeTripSchedule())
        details.setSchedule(_tripStopTimesBeanService.getStopTimesForTrip(trip.getId()));

      results.add(details);
    }

    return new ListBean<TripDetailsBean>(results, false);
  }

  /**
   * 
   */
  @Override
  public ListBean<TripDetailsBean> getTripsForRoute(TripsForRouteQueryBean query) {

    AgencyAndId routeId = AgencyAndIdLibrary.convertFromString(query.getRouteId());
    long time = query.getTime();
    TripDetailsInclusionBean inclusion = query.getInclusion();

    List<BlockInstance> instances = _activeCalendarService.getActiveBlocksForRouteInTimeRange(
        routeId, time, time);

    return getBlockInstancesAsBeans(instances, inclusion, time);
  }

  @Override
  public ListBean<TripDetailsBean> getTripsForAgency(
      TripsForAgencyQueryBean query) {

    String agencyId = query.getAgencyId();
    long time = query.getTime();
    TripDetailsInclusionBean inclusion = query.getInclusion();

    List<BlockInstance> instances = _activeCalendarService.getActiveBlocksForAgencyInTimeRange(
        agencyId, time, time);

    return getBlockInstancesAsBeans(instances, inclusion, time);
  }

  /****
   * Private Methods
   ****/

  private ListBean<TripDetailsBean> getBlockInstancesAsBeans(
      List<BlockInstance> instances, TripDetailsInclusionBean inclusion,
      long time) {

    List<TripDetailsBean> results = new ArrayList<TripDetailsBean>();

    for (BlockInstance instance : instances) {

      BlockLocation position = _blockLocationService.getPositionForBlockInstance(
          instance, time);

      if (position == null)
        continue;

      TripEntry trip = position.getActiveTrip();
      TripDetailsBean details = new TripDetailsBean();

      details.setTripId(AgencyAndIdLibrary.convertToString(trip.getId()));

      if (inclusion.isIncludeTripStatus()) {
        TripStatusBean status = getBlockLocationAsStatusBean(
            position.getServiceDate(), position);
        details.setStatus(status);
      }

      if (inclusion.isIncludeTripBean())
        details.setTrip(_tripBeanService.getTripForId(trip.getId()));

      if (inclusion.isIncludeTripSchedule())
        details.setSchedule(_tripStopTimesBeanService.getStopTimesForTrip(trip.getId()));

      results.add(details);
    }

    return new ListBean<TripDetailsBean>(results, false);
  }

  private TripStatusBean getTripPositionAsStatusBean(long serviceDate,
      TripLocation tripPosition) {

    TripStatusBean bean = new TripStatusBean();

    bean.setStatus("default");
    bean.setServiceDate(serviceDate);

    if (tripPosition != null) {

      bean.setTime(tripPosition.getLastUpdateTime());

      CoordinatePoint location = tripPosition.getLocation();
      bean.setPosition(location);
      bean.setScheduleDeviation(tripPosition.getScheduleDeviation());
      bean.setPredicted(tripPosition.isPredicted());
      AgencyAndId vid = tripPosition.getVehicleId();
      if (vid != null)
        bean.setVehicleId(ApplicationBeanLibrary.getId(vid));
      StopTimeEntry stop = tripPosition.getClosestStop();
      if (stop != null) {
        StopBean stopBean = _stopBeanService.getStopForId(stop.getStop().getId());
        bean.setClosestStop(stopBean);
        bean.setClosestStopTimeOffset(tripPosition.getClosestStopTimeOffset());
      }
    } else {
      bean.setPredicted(false);
    }

    return bean;
  }

  private TripStatusBean getBlockLocationAsStatusBean(long serviceDate,
      BlockLocation blockLocation) {

    TripStatusBean bean = new TripStatusBean();

    bean.setStatus("default");
    bean.setServiceDate(serviceDate);

    if (blockLocation != null) {

      bean.setTime(blockLocation.getLastUpdateTime());

      CoordinatePoint location = blockLocation.getLocation();
      bean.setPosition(location);
      bean.setScheduleDeviation(blockLocation.getScheduleDeviation());

      TripEntry activeTrip = blockLocation.getActiveTrip();
      bean.setDistanceAlongTrip(blockLocation.getDistanceAlongBlock()
          - activeTrip.getDistanceAlongBlock());

      bean.setPredicted(blockLocation.isPredicted());
      AgencyAndId vid = blockLocation.getVehicleId();
      if (vid != null)
        bean.setVehicleId(ApplicationBeanLibrary.getId(vid));
      StopTimeEntry stop = blockLocation.getClosestStop();
      if (stop != null) {
        StopBean stopBean = _stopBeanService.getStopForId(stop.getStop().getId());
        bean.setClosestStop(stopBean);
        bean.setClosestStopTimeOffset(blockLocation.getClosestStopTimeOffset());
      }
    } else {
      bean.setPredicted(false);
    }

    return bean;
  }

  private long getBestServiceDateForTrip(AgencyAndId tripId, long serviceDate) {

    TimeZone timeZone = _calendarService.getTimeZoneForAgencyId(tripId.getAgencyId());
    Calendar c = Calendar.getInstance();
    c.setTimeZone(timeZone);
    c.setTimeInMillis(serviceDate);
    ServiceDate d = new ServiceDate(c);
    Date adjustedDate = d.getAsDate(timeZone);
    return adjustedDate.getTime();
  }

}
