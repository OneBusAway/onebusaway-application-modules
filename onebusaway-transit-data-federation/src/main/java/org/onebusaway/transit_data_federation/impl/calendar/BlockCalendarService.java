package org.onebusaway.transit_data_federation.impl.calendar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockCalendarService {

  private TransitGraphDao _graphDao;

  private CalendarService _calendarService;

  @Autowired
  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao graphDao) {
    _graphDao = graphDao;
  }

  public List<Date> getServiceDatesWithinRangeForBlockId(AgencyAndId blockId,
      Date from, Date to) {

    List<TripEntry> trips = _graphDao.getTripsForBlockId(blockId);
    if (trips == null || trips.isEmpty())
      return Collections.emptyList();

    Set<LocalizedServiceId> serviceIds = new HashSet<LocalizedServiceId>();

    ServiceInterval in = null;

    for (TripEntry trip : trips) {
      List<StopTimeEntry> stopTimes = trip.getStopTimes();
      if (stopTimes.isEmpty())
        continue;
      in = extend(in, stopTimes.get(0));
      in = extend(in, stopTimes.get(stopTimes.size() - 1));

      AgencyAndId tripId = trip.getId();
      AgencyAndId serviceId = trip.getServiceId();
      LocalizedServiceId localizedServiceId = _calendarService.getLocalizedServiceIdForAgencyAndServiceId(
          tripId.getAgencyId(), serviceId);
      serviceIds.add(localizedServiceId);
    }

    Set<Date> allDates = new TreeSet<Date>();
    for (LocalizedServiceId serviceId : serviceIds) {
      List<Date> dates = _calendarService.getServiceDatesWithinRange(serviceId,
          in, from, to);
      allDates.addAll(dates);
    }

    return new ArrayList<Date>(allDates);
  }

  /****
   * Private Methods
   ****/

  private ServiceInterval extend(ServiceInterval interval,
      StopTimeEntry stopTime) {
    if (interval == null)
      return new ServiceInterval(stopTime.getArrivalTime(),
          stopTime.getDepartureTime());
    return interval.extend(stopTime.getArrivalTime(),
        stopTime.getDepartureTime());
  }
}
