package org.onebusaway.where.impl;

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.CalendarService;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.where.model.StopTimeInstance;
import org.onebusaway.where.services.StopTimeService;

import edu.washington.cs.rse.text.DateLibrary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
class StopTimeServiceImpl implements StopTimeService {

  private GtfsDao _dao;

  private CalendarService _calendarService;

  @Autowired
  public void setGtfsDao(GtfsDao dao) {
    _dao = dao;
  }

  @Autowired
  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  public List<StopTimeInstance> getStopTimeInstancesInTimeRange(Stop stop, Date from, Date to) {

    Set<String> serviceIds = new HashSet<String>(_dao.getServiceIdsByStop(stop));

    Map<String, List<Date>> serviceDates = _calendarService.getServiceDatesWithinRange(serviceIds, from, to);
    serviceIds = serviceDates.keySet();

    List<StopTime> stopTimes = getStopTimesForStopAndServiceIdsAndTimeRange(stop, serviceIds, from, to);

    List<StopTimeInstance> stis = new ArrayList<StopTimeInstance>();

    for (StopTime stopTime : stopTimes) {
      Trip trip = stopTime.getTrip();
      String serviceId = trip.getServiceId();
      List<Date> dates = serviceDates.get(serviceId);

      for (Date serviceDate : dates) {

        StopTimeInstance sti = new StopTimeInstance(stopTime, serviceDate);

        if (DateLibrary.inRange(from, to, sti.getArrivalTime())
            || DateLibrary.inRange(from, to, sti.getDepartureTime())) {
          stis.add(sti);
        }
      }
    }

    return stis;
  }

  private List<StopTime> getStopTimesForStopAndServiceIdsAndTimeRange(Stop stop, Set<String> serviceIds, Date from,
      Date to) {

    if (to.getTime() - from.getTime() >= 24 * 60 * 60 * 1000)
      return _dao.getStopTimesByStopAndServiceIds(stop, serviceIds);

    Date dayFrom = DateLibrary.getTimeAsDay(from);

    int minPassingTime = (int) ((from.getTime() - dayFrom.getTime()) / 1000);
    int maxPassingTime = (int) ((to.getTime() - dayFrom.getTime()) / 1000);

    return _dao.getStopTimesByStopAndServiceIdsAndTimeRange(stop, serviceIds, minPassingTime, maxPassingTime);
  }
}
