package org.onebusaway.gtdf.impl;

import edu.washington.cs.rse.text.DateLibrary;

import org.onebusaway.gtdf.services.CalendarService;
import org.onebusaway.where.model.ServiceDate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
public class CalendarServiceImpl implements CalendarService {

  private CalendarServiceData _data;

  public void setCalendarServiceData(CalendarServiceData data) {
    _data = data;
  }

  public Set<Date> getDatesForServiceId(String serviceId) {
    return _data.getDatesForServiceId(serviceId);
  }

  public Set<String> getServiceIdsOnDate(Date date) {

    date = DateLibrary.getTimeAsDay(date);

    Set<String> ids = new HashSet<String>();
    Set<String> potential = _data.getServiceIdsForDate(date);
    if (potential != null)
      ids.addAll(potential);
    return ids;
  }

  public Set<ServiceDate> getServiceDatesWithinRange(Date from, Date to) {

    Date fromDay = DateLibrary.getTimeAsDay(from);
    Date toDay = DateLibrary.getTimeAsDay(to);

    java.util.Calendar c = java.util.Calendar.getInstance();
    c.setTime(fromDay);

    Set<ServiceDate> hits = new HashSet<ServiceDate>();

    while (true) {
      Date d = c.getTime();
      if (d.after(toDay))
        break;

      Set<ServiceDate> intervals = _data.getServiceDatesForDate(d);
      if (intervals != null) {
        for (ServiceDate interval : intervals) {
          if (interval.hasOverlap(from, to))
            hits.add(interval);
        }
      }

      c.add(java.util.Calendar.DAY_OF_YEAR, 1);
    }

    return hits;
  }
}
