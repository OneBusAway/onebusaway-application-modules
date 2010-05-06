package org.onebusaway.where.impl;

import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.text.DateLibrary;

import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.gtdf.model.Trip;
import org.onebusaway.gtdf.services.CalendarService;
import org.onebusaway.gtdf.services.GtdfDao;
import org.onebusaway.where.model.ServiceDate;
import org.onebusaway.where.model.StopTimeInstance;
import org.onebusaway.where.services.StopTimeService;
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

  private GtdfDao _dao;

  private CalendarService _calendarService;

  @Autowired
  public void setGtdfDao(GtdfDao dao) {
    _dao = dao;
  }

  @Autowired
  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  public List<StopTimeInstance> getStopTimeInstancesInTimeRange(Stop stop,
      Date from, Date to) {

    Set<ServiceDate> serviceDates = _calendarService.getServiceDatesWithinRange(
        from, to);

    Map<String, Set<ServiceDate>> serviceDatesByServiceId = new FactoryMap<String, Set<ServiceDate>>(
        new HashSet<ServiceDate>());

    for (ServiceDate serviceDate : serviceDates)
      serviceDatesByServiceId.get(serviceDate.getServiceId()).add(serviceDate);

    Set<String> serviceIds = serviceDatesByServiceId.keySet();

    List<StopTime> stopTimes = getStopTimesForStopAndServiceIdsAndTimeRange(
        stop, serviceIds, from, to);

    List<StopTimeInstance> stis = new ArrayList<StopTimeInstance>();

    for (StopTime stopTime : stopTimes) {
      Trip trip = stopTime.getTrip();
      String serviceId = trip.getServiceId();
      Set<ServiceDate> dates = serviceDatesByServiceId.get(serviceId);

      for (ServiceDate serviceDate : dates) {

        StopTimeInstance sti = new StopTimeInstance(stopTime,
            serviceDate.getServiceDate());

        if (DateLibrary.inRange(from, to, sti.getArrivalTime())
            || DateLibrary.inRange(from, to, sti.getDepartureTime())) {
          stis.add(sti);
        }
      }
    }

    return stis;
  }

  private List<StopTime> getStopTimesForStopAndServiceIdsAndTimeRange(
      Stop stop, Set<String> serviceIds, Date from, Date to) {

    if (to.getTime() - from.getTime() >= 24 * 60 * 60 * 1000)
      return _dao.getStopTimesByStopAndServiceIds(stop, serviceIds);

    Date dayFrom = DateLibrary.getTimeAsDay(from);

    int minPassingTime = (int) ((from.getTime() - dayFrom.getTime()) / 1000);
    int maxPassingTime = (int) ((to.getTime() - dayFrom.getTime()) / 1000);

    return _dao.getStopTimesByStopAndServiceIdsAndTimeRange(stop, serviceIds,
        minPassingTime, maxPassingTime);
  }
}
