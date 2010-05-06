package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.model.calendar.ServiceIdIntervals;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.TripDetailsBean;
import org.onebusaway.transit_data.model.TripStatusBean;
import org.onebusaway.transit_data.model.TripsForBoundsQueryBean;
import org.onebusaway.transit_data_federation.impl.time.StopTimeSearchOperations;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeOp;
import org.onebusaway.transit_data_federation.model.TripPosition;
import org.onebusaway.transit_data_federation.model.predictions.ScheduleDeviation;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.TripPositionService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripStatusBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripStopTimesBeanService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndex;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripInstanceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.washington.cs.rse.geospatial.latlon.CoordinateRectangle;

@Component
public class TripStatusBeanServiceImpl implements TripStatusBeanService {

  private TransitGraphDao _graph;

  private CalendarService _calendarService;

  private TripPositionService _tripPositionService;

  private TripBeanService _tripBeanService;

  private TripStopTimesBeanService _tripStopTimesBeanService;

  private static final long TIME_WINDOW = 30 * 60 * 1000;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao graph) {
    _graph = graph;
  }

  @Autowired
  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  @Autowired
  public void setTripPositionService(TripPositionService tripPositionService) {
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

  public TripStatusBean getTripStatusForTripId(AgencyAndId tripId,
      long serviceDate, long time) {

    TripEntry tripEntry = _graph.getTripEntryForId(tripId);
    if (tripEntry == null)
      return null;

    serviceDate = getBestServiceDateForTrip(tripId, serviceDate);

    TripInstanceProxy tripInstance = new TripInstanceProxy(tripEntry,
        serviceDate);

    TripPosition tripPosition = _tripPositionService.getPositionForTripInstance(
        tripInstance, time);

    return getTrip(tripInstance, tripPosition);
  }

  @Override
  public ListBean<TripDetailsBean> getActiveTripForBounds(
      TripsForBoundsQueryBean query) {

    CoordinateBounds bounds = query.getBounds();
    long time = query.getTime();

    CoordinateRectangle r = new CoordinateRectangle(bounds.getMinLat(),
        bounds.getMinLon(), bounds.getMaxLat(), bounds.getMaxLon());
    List<StopEntry> stops = _graph.getStopsByLocation(r);

    ServiceIdIntervals intervals = new ServiceIdIntervals();
    for (StopEntry stop : stops) {
      StopTimeIndex index = stop.getStopTimes();
      intervals.addIntervals(index.getServiceIdIntervals());
    }

    long timeFrom = time - TIME_WINDOW;
    long timeTo = time + TIME_WINDOW;

    Map<LocalizedServiceId, List<Date>> serviceIdsAndDates = _calendarService.getServiceDatesWithinRange(
        intervals, new Date(timeFrom), new Date(timeTo));

    Set<TripInstanceProxy> tripInstances = new HashSet<TripInstanceProxy>();

    for (StopEntry stop : stops) {
      StopTimeIndex index = stop.getStopTimes();
      List<StopTimeInstanceProxy> stopTimeInstances = StopTimeSearchOperations.getStopTimeInstancesInRange(
          index, timeFrom, timeTo, StopTimeOp.DEPARTURE, serviceIdsAndDates);
      for (StopTimeInstanceProxy stopTimeInstance : stopTimeInstances) {
        TripEntry trip = stopTimeInstance.getTrip();
        long serviceDate = stopTimeInstance.getServiceDate();
        tripInstances.add(new TripInstanceProxy(trip, serviceDate));
      }
    }

    List<TripDetailsBean> results = new ArrayList<TripDetailsBean>();

    for (TripInstanceProxy tripInstance : tripInstances) {

      TripPosition tripPosition = _tripPositionService.getPositionForTripInstance(
          tripInstance, time);

      if (tripPosition == null)
        continue;

      CoordinatePoint location = tripPosition.getPosition();

      if (location != null
          && bounds.contains(location.getLat(), location.getLon())) {

        TripEntry trip = tripInstance.getTrip();

        TripDetailsBean details = new TripDetailsBean();
        details.setTripId(AgencyAndIdLibrary.convertToString(trip.getId()));

        TripStatusBean status = getTrip(tripInstance, tripPosition);
        details.setStatus(status);

        if (query.isIncludeTripBeans())
          details.setTrip(_tripBeanService.getTripForId(trip.getId()));

        if (query.isIncludeTripSchedules())
          details.setSchedule(_tripStopTimesBeanService.getStopTimesForTrip(trip.getId()));

        results.add(details);
      }
    }

    return new ListBean<TripDetailsBean>(results, false);
  }

  /****
   * Private Methods
   ****/

  private TripStatusBean getTrip(TripInstanceProxy tripInstance,
      TripPosition tripPosition) {

    TripStatusBean bean = new TripStatusBean();

    bean.setStatus("default");
    bean.setServiceDate(tripInstance.getServiceDate());

    if (tripPosition != null) {
      CoordinatePoint location = tripPosition.getPosition();
      ScheduleDeviation sd = tripPosition.getScheduleDeviation();
      bean.setPosition(location);
      bean.setScheduleDeviation(sd.getScheduleDeviation());
      bean.setPredicted(sd.isPredicted());
      AgencyAndId vid = sd.getVehicleId();
      if( vid != null)
        bean.setVehicleId(ApplicationBeanLibrary.getId(vid));
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
