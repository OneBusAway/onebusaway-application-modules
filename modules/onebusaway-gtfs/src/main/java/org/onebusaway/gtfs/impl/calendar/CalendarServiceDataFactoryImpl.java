package org.onebusaway.gtfs.impl.calendar;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarServiceData;
import org.onebusaway.gtfs.services.calendar.CalendarServiceDataFactory;
import org.onebusaway.gtfs.services.calendar.ServiceIdCalendarServiceData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CalendarServiceDataFactoryImpl implements
    CalendarServiceDataFactory {

  private final Logger _log = LoggerFactory.getLogger(CalendarServiceDataFactoryImpl.class);

  private GtfsRelationalDao _dao;

  public void setGtfsDao(GtfsRelationalDao dao) {
    _dao = dao;
  }
  
  public CalendarServiceData createServiceCalendarData() {

    CalendarServiceDataImpl data = new CalendarServiceDataImpl();

    Collection<ServiceCalendar> calendars = _dao.getAllCalendars();
    Collection<ServiceCalendarDate> calendarDates = _dao.getAllCalendarDates();

    Map<AgencyAndId, ServiceCalendar> calendarsByServiceId = getCalendarsByServiceId(calendars);
    Map<AgencyAndId, List<ServiceCalendarDate>> calendarDatesByServiceId = getCalendarDatesByServiceId(calendarDates);

    Set<AgencyAndId> serviceIds = new HashSet<AgencyAndId>();
    serviceIds.addAll(calendarsByServiceId.keySet());
    serviceIds.addAll(calendarDatesByServiceId.keySet());

    int index = 0;

    for (AgencyAndId serviceId : serviceIds) {

      index++;

      _log.info("serviceId=" + serviceId + " (" + index + "/"
          + serviceIds.size() + ")");

      // These operations take a while, especially if the specified service id
      // has lots of StopTimes
      int[] arrivals = _dao.getArrivalTimeIntervalForServiceId(serviceId);
      int[] departures = _dao.getDepartureTimeIntervalForServiceId(serviceId);

      // If there are no arrivals or depatures for a specified service id, we
      // skip id
      if (arrivals == null || departures == null)
        continue;

      Set<Date> activeDates = new HashSet<Date>();
      ServiceCalendar c = calendarsByServiceId.get(serviceId);

      if (c != null)
        addDatesFromCalendar(c, activeDates);
      if (calendarDatesByServiceId.containsKey(serviceId)) {
        for (ServiceCalendarDate cd : calendarDatesByServiceId.get(serviceId)) {
          addAndRemoveDatesFromCalendarDate(cd, activeDates);
        }
      }

      List<Date> dates = new ArrayList<Date>(activeDates);

      ServiceIdCalendarServiceData serviceIdData = new ServiceIdCalendarServiceData(
          dates, arrivals[0], arrivals[1], departures[0], departures[1]);
      data.putDataForServiceId(serviceId, serviceIdData);
    }

    return data;
  }

  private Map<AgencyAndId, ServiceCalendar> getCalendarsByServiceId(
      Collection<ServiceCalendar> calendars) {
    Map<AgencyAndId, ServiceCalendar> calendarsByServiceId = new HashMap<AgencyAndId, ServiceCalendar>();
    for (ServiceCalendar c : calendars)
      calendarsByServiceId.put(c.getServiceId(), c);
    return calendarsByServiceId;
  }

  private Map<AgencyAndId, List<ServiceCalendarDate>> getCalendarDatesByServiceId(
      Collection<ServiceCalendarDate> calendarDates) {
    Map<AgencyAndId, List<ServiceCalendarDate>> calendarDatesByServiceId = new HashMap<AgencyAndId, List<ServiceCalendarDate>>();

    for (ServiceCalendarDate calendarDate : calendarDates) {
      List<ServiceCalendarDate> cds = calendarDatesByServiceId.get(calendarDate.getServiceId());
      if( cds == null) {
        cds = new ArrayList<ServiceCalendarDate>();
        calendarDatesByServiceId.put(calendarDate.getServiceId(), cds);
      }
      cds.add(calendarDate);
    }
    return calendarDatesByServiceId;
  }

  private void addDatesFromCalendar(ServiceCalendar calendar, Set<Date> dates) {

    java.util.Calendar c = java.util.Calendar.getInstance();
    c.setTime(calendar.getStartDate());
    while (true) {
      Date date = c.getTime();
      if (date.after(calendar.getEndDate()))
        break;

      int day = c.get(java.util.Calendar.DAY_OF_WEEK);
      boolean active = false;

      switch (day) {
        case java.util.Calendar.MONDAY:
          active = calendar.getMonday() == 1;
          break;
        case java.util.Calendar.TUESDAY:
          active = calendar.getTuesday() == 1;
          break;
        case java.util.Calendar.WEDNESDAY:
          active = calendar.getWednesday() == 1;
          break;
        case java.util.Calendar.THURSDAY:
          active = calendar.getThursday() == 1;
          break;
        case java.util.Calendar.FRIDAY:
          active = calendar.getFriday() == 1;
          break;
        case java.util.Calendar.SATURDAY:
          active = calendar.getSaturday() == 1;
          break;
        case java.util.Calendar.SUNDAY:
          active = calendar.getSunday() == 1;
          break;
      }

      if (active)
        dates.add(date);

      c.add(java.util.Calendar.DAY_OF_YEAR, 1);
    }
  }

  private void addAndRemoveDatesFromCalendarDate(
      ServiceCalendarDate calendarDate, Set<Date> dates) {

    Date date = new Date(calendarDate.getDate().getTime());

    switch (calendarDate.getExceptionType()) {
      case ServiceCalendarDate.EXCEPTION_TYPE_ADD:
        dates.add(date);
        break;
      case ServiceCalendarDate.EXCEPTION_TYPE_REMOVE:
        dates.remove(date);
        break;
      default:
        _log.warn("unknown CalendarDate exception type: "
            + calendarDate.getExceptionType());
        break;
    }
  }
}
