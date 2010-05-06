package org.onebusaway.gtfs.impl.calendar;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.gtfs.services.calendar.CalendarServiceData;
import org.onebusaway.gtfs.services.calendar.CalendarServiceDataFactory;
import org.onebusaway.gtfs.services.calendar.ServiceIdCalendarServiceData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CalendarServiceImpl implements CalendarService {

  private CalendarServiceDataFactory _factory;

  private volatile CalendarServiceData _data;

  public void setServiceCalendarDataFactory(CalendarServiceDataFactory factory) {
    _factory = factory;
  }

  public void setServiceCalendarData(CalendarServiceData data) {
    _data = data;
  }

  /****
   * {@link CalendarService} Interface
   ****/

  @Override
  public Set<AgencyAndId> getServiceIds() {
    CalendarServiceData allData = getData();
    return allData.getServiceIds();
  }

  @Override
  public Set<Date> getServiceDatesForServiceId(AgencyAndId serviceId) {
    Set<Date> dates = new HashSet<Date>();
    CalendarServiceData allData = getData();
    ServiceIdCalendarServiceData data = allData.getDataForServiceId(serviceId);
    if (data != null)
      dates.addAll(data.getServiceDates());
    return dates;
  }

  @Override
  public Set<AgencyAndId> getServiceIdsOnDate(Date date) {
    CalendarServiceData allData = getData();
    return allData.getServiceIdsForDate(date);
  }

  @Override
  public Map<AgencyAndId, List<Date>> getServiceDateDeparturesWithinRange(
      Set<AgencyAndId> serviceIds, Date from, Date to) {
    return getServiceDates(serviceIds, ServiceIdCalendarDataOp.DEPARTURE_OP,
        from, to, false);
  }

  @Override
  public Map<AgencyAndId, List<Date>> getServiceDateArrivalsWithinRange(
      Set<AgencyAndId> serviceIds, Date from, Date to) {
    return getServiceDates(serviceIds, ServiceIdCalendarDataOp.ARRIVAL_OP, to,
        from, false);
  }

  @Override
  public Map<AgencyAndId, List<Date>> getServiceDatesWithinRange(Date from,
      Date to) {

    Set<AgencyAndId> ids = new HashSet<AgencyAndId>();

    Calendar c = Calendar.getInstance();
    c.setTime(from);
    while (c.getTime().compareTo(to) < 0) {
      ids.addAll(getServiceIdsOnDate(c.getTime()));
      c.add(Calendar.DAY_OF_YEAR, 1);
    }
    ids.addAll(getServiceIdsOnDate(to));

    return getServiceDatesWithinRange(ids, from, to);
  }

  @Override
  public Map<AgencyAndId, List<Date>> getServiceDatesWithinRange(
      Set<AgencyAndId> serviceIds, Date from, Date to) {
    return getServiceDates(serviceIds, ServiceIdCalendarDataOp.BOTH_OP, from,
        to, false);
  }

  @Override
  public Map<AgencyAndId, List<Date>> getNextDepartureServiceDates(
      Set<AgencyAndId> serviceIds, long targetTime) {
    Date target = new Date(targetTime);
    return getServiceDates(serviceIds, ServiceIdCalendarDataOp.DEPARTURE_OP,
        target, target, true);
  }

  @Override
  public Map<AgencyAndId, List<Date>> getPreviousArrivalServiceDates(
      Set<AgencyAndId> serviceIds, long targetTime) {
    Date target = new Date(targetTime);
    return getServiceDates(serviceIds, ServiceIdCalendarDataOp.ARRIVAL_OP,
        target, target, true);
  }

  /****
   * Private Methods
   ****/

  private CalendarServiceData getData() {
    if (_data == null) {
      synchronized (this) {
        if (_data == null) {
          _data = _factory.createServiceCalendarData();
        }
      }
    }
    return _data;
  }

  private Map<AgencyAndId, List<Date>> getServiceDates(
      Set<AgencyAndId> serviceIds, ServiceIdCalendarDataOp op, Date from,
      Date to, boolean includeNextDate) {

    CalendarServiceData allData = getData();

    Map<AgencyAndId, List<Date>> results = new HashMap<AgencyAndId, List<Date>>();

    for (AgencyAndId serviceId : serviceIds) {

      ServiceIdCalendarServiceData data = allData.getDataForServiceId(serviceId);

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

      if (!resultsForServiceId.isEmpty())
        results.put(serviceId, resultsForServiceId);
    }

    return results;
  }

  private int search(List<Date> serviceDates, ServiceIdCalendarDataOp op,
      int indexFrom, int indexTo, Date key) {

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
