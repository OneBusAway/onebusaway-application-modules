package org.onebusaway.gtfs.impl;

import org.onebusaway.gtfs.services.CalendarService;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class CalendarServiceImpl implements CalendarService {

  private ServiceCalendarData _data;

  public void setServiceCalendarData(ServiceCalendarData data) {
    _data = data;
  }

  public Set<Date> getDatesForServiceId(String serviceId) {
    Set<Date> dates = new HashSet<Date>();
    ServiceIdCalendarData data = _data.getDataForServiceId(serviceId);
    if (data != null)
      dates.addAll(data.getServiceDates());
    return dates;
  }

  public Set<String> getServiceIdsOnDate(Date date) {
    return _data.getServiceIdsForDate(date);
  }

  public Map<String, List<Date>> getServiceDateDeparturesWithinRange(Set<String> serviceIds, Date from, Date to) {
    return getServiceDates(serviceIds, ServiceIdCalendarDataOp.DEPARTURE_OP, from, to, false);
  }

  public Map<String, List<Date>> getServiceDateArrivalsWithinRange(Set<String> serviceIds, Date from, Date to) {
    return getServiceDates(serviceIds, ServiceIdCalendarDataOp.ARRIVAL_OP, to, from, false);
  }

  public Map<String, List<Date>> getServiceDatesWithinRange(Set<String> serviceIds, Date from, Date to) {
    return getServiceDates(serviceIds, ServiceIdCalendarDataOp.BOTH_OP, from, to, false);
  }

  public Map<String, List<Date>> getNextDepartureServiceDates(Set<String> serviceIds, long targetTime) {
    Date target = new Date(targetTime);
    return getServiceDates(serviceIds, ServiceIdCalendarDataOp.DEPARTURE_OP, target, target, true);
  }

  public Map<String, List<Date>> getPreviousArrivalServiceDates(Set<String> serviceIds, long targetTime) {
    Date target = new Date(targetTime);
    return getServiceDates(serviceIds, ServiceIdCalendarDataOp.ARRIVAL_OP, target, target, true);
  }

  private Map<String, List<Date>> getServiceDates(Set<String> serviceIds, ServiceIdCalendarDataOp op, Date from, Date to,
      boolean includeNextDate) {

    Map<String, List<Date>> results = new HashMap<String, List<Date>>();

    for (String serviceId : serviceIds) {

      ServiceIdCalendarData data = _data.getDataForServiceId(serviceId);

      if (data == null)
        continue;

      List<Date> serviceDates = data.getServiceDates();
      List<Date> resultsForServiceId = new ArrayList<Date>();

      Date target = op.shiftTime(data, from);
      int index = search(serviceDates, op, 0, serviceDates.size(), target);

      if (index == serviceDates.size())
        index--;

      while (0 <= index) {
        Date serviceDate = op.getServiceDate(serviceDates, index);
        int rc = op.compareInterval(data, serviceDate, from, to);

        if (rc == 0 || (includeNextDate && rc == 1))
          resultsForServiceId.add(serviceDate);
        else if (rc < 0)
          break;
        index--;
      }

      results.put(serviceId, resultsForServiceId);
    }

    return results;
  }

  private int search(List<Date> serviceDates, ServiceIdCalendarDataOp op, int indexFrom, int indexTo, Date key) {

    if (indexTo == indexFrom)
      return indexFrom;

    int index = (indexFrom + indexTo) / 2;

    Date serviceDate = op.getServiceDate(serviceDates, index);

    int rc = op.compare(key, serviceDate);

    if (rc == 0)
      return index;

    if (rc < 0)
      return search(serviceDates, op, indexFrom, index, key);
    else
      return search(serviceDates, op, index + 1, indexTo, key);
  }

}
