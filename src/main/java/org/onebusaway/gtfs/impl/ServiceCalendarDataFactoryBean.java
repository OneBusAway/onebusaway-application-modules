package org.onebusaway.gtfs.impl;

import org.onebusaway.gtfs.model.CalendarDate;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.services.GtfsDao;

import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.stats.IntegerInterval;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class ServiceCalendarDataFactoryBean implements FactoryBean {

  private static Logger _log = Logger.getLogger(ServiceCalendarDataFactoryBean.class.getName());

  @Autowired
  private GtfsDao _dao;

  private ServiceCalendarData _data;

  public Class<?> getObjectType() {
    return ServiceCalendarData.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public Object getObject() throws Exception {
    if (_data == null) {
      _data = new ServiceCalendarData();
      populateCalendarServiceData(_data);
    }
    return _data;
  }

  private void populateCalendarServiceData(ServiceCalendarData data) {

    List<ServiceCalendar> calendars = _dao.getAllCalendars();
    List<CalendarDate> calendarDates = _dao.getAllCalendarDates();

    Map<String, ServiceCalendar> calendarsByServiceId = getCalendarsByServiceId(calendars);
    Map<String, List<CalendarDate>> calendarDatesByServiceId = getCalendarDatesByServiceId(calendarDates);

    Set<String> serviceIds = new HashSet<String>();
    serviceIds.addAll(calendarsByServiceId.keySet());
    serviceIds.addAll(calendarDatesByServiceId.keySet());

    int index = 0;
    
    for (String serviceId : serviceIds) {
    
      index++;
      
      System.out.println("  serviceId=" + serviceId + " (" + index + "/" + serviceIds.size() + ")");

      // These operations take a while, especially if the specified service id has lots of StopTimes
      IntegerInterval arrivals = _dao.getArrivalTimeIntervalForServiceId(serviceId);
      IntegerInterval departures = _dao.getDepartureTimeIntervalForServiceId(serviceId);

      Set<Date> activeDates = new HashSet<Date>();
      ServiceCalendar c = calendarsByServiceId.get(serviceId);

      if (c != null)
        addDatesFromCalendar(c, activeDates);
      if (calendarDatesByServiceId.containsKey(serviceId)) {
        for (CalendarDate cd : calendarDatesByServiceId.get(serviceId)) {
          addAndRemoveDatesFromCalendarDate(cd, activeDates);
        }
      }
      
      List<Date> dates = new ArrayList<Date>(activeDates);
      
      ServiceIdCalendarData serviceIdData = new ServiceIdCalendarData(dates,arrivals.getMin(),arrivals.getMax(),departures.getMin(),departures.getMax());
      data.putDataForServiceId(serviceId, serviceIdData);
    }
  }

  private Map<String, ServiceCalendar> getCalendarsByServiceId(List<ServiceCalendar> calendars) {
    Map<String, ServiceCalendar> calendarsByServiceId = new HashMap<String, ServiceCalendar>();
    for (ServiceCalendar c : calendars)
      calendarsByServiceId.put(c.getServiceId(), c);
    return calendarsByServiceId;
  }

  private Map<String, List<CalendarDate>> getCalendarDatesByServiceId(List<CalendarDate> calendarDates) {
    Map<String, List<CalendarDate>> calendarDatesByServiceId = new FactoryMap<String, List<CalendarDate>>(
        new ArrayList<CalendarDate>());
    for (CalendarDate calendarDate : calendarDates)
      calendarDatesByServiceId.get(calendarDate.getServiceId()).add(calendarDate);
    return calendarDatesByServiceId;
  }

  private void addDatesFromCalendar(ServiceCalendar calendar, Set<Date> dates) {

    java.util.Calendar c = java.util.Calendar.getInstance();
    c.setTime(calendar.getStartDate());
    while (true) {
      Date date = c.getTime();
      if (!date.before(calendar.getEndDate()))
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

  private void addAndRemoveDatesFromCalendarDate(CalendarDate calendarDate, Set<Date> dates) {

    Date date = new Date(calendarDate.getDate().getTime());

    switch (calendarDate.getExceptionType()) {
      case CalendarDate.EXCEPTION_TYPE_ADD:
        dates.add(date);
        break;
      case CalendarDate.EXCEPTION_TYPE_REMOVE:
        dates.remove(date);
        break;
      default:
        _log.warning("unknown CalendarDate exception type: " + calendarDate.getExceptionType());
        break;
    }
  }
}
