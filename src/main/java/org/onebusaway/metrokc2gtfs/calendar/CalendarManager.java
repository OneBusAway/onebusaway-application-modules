package org.onebusaway.metrokc2gtfs.calendar;

import org.onebusaway.csv.CsvEntityWriter;
import org.onebusaway.gtfs.model.CalendarDate;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.metrokc2gtfs.TranslationContext;
import org.onebusaway.metrokc2gtfs.model.MetroKCChangeDate;
import org.onebusaway.metrokc2gtfs.model.MetroKCTrip;
import org.onebusaway.metrokc2gtfs.model.ServiceId;
import org.onebusaway.metrokc2gtfs.model.ServiceIdModification;

import edu.washington.cs.rse.collections.FactoryMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class CalendarManager {

  public static final String SCHEDULE_TYPE_WEEKDAY = "WEEKDAY";

  public static final String SCHEDULE_TYPE_SATURDAY = "SATURDAY";

  public static final String SCHEDULE_TYPE_SUNDAY = "SUNDAY";

  private List<TripScheduleModificationStrategy> _modifications = new ArrayList<TripScheduleModificationStrategy>();

  private List<ServiceCalendar> _calendars = new ArrayList<ServiceCalendar>();

  private Map<MetroKCServiceId, Set<ServiceIdModification>> _serviceIds = new HashMap<MetroKCServiceId, Set<ServiceIdModification>>();

  Map<String, SortedMap<Date, CalendarDate>> _serviceMods = new FactoryMap<String, SortedMap<Date, CalendarDate>>(
      new TreeMap<Date, CalendarDate>());
  
  public void addModificationStrategy(TripScheduleModificationStrategy strategy) {
    _modifications.add(strategy);
  }

  public void setModificationStrategy(TripScheduleModificationStrategy strategy) {
    _modifications.add(strategy);
  }

  public ServiceId getServiceIdsForTrip(MetroKCChangeDate changeDate, MetroKCTrip trip, Route route) {

    MetroKCServiceId serviceId = new MetroKCServiceId(changeDate, trip);

    Set<ServiceIdModification> mods = _serviceIds.get(serviceId);

    if (mods == null) {
      mods = computeServiceIdsForTrip(serviceId);
      _serviceIds.put(serviceId, mods);
    }

    return new ServiceId(serviceId.getServiceId(), mods);
  }

  private Set<ServiceIdModification> computeServiceIdsForTrip(MetroKCServiceId key) {

    Set<ServiceIdModification> mods = new HashSet<ServiceIdModification>();
    mods.add(new ServiceIdModificationImpl());

    MetroKCChangeDate changeDate = key.getChangeDate();
    String scheduleType = key.getScheduleType();

    ServiceCalendar c = new ServiceCalendar();

    String serviceId = key.getServiceId();
    c.setServiceId(serviceId);
    c.setStartDate(changeDate.getStartDate());
    c.setEndDate(changeDate.getEndDate());

    int dayFrom = 0;
    int dayTo = 0;

    if (scheduleType.equals(SCHEDULE_TYPE_WEEKDAY)) {
      c.setMonday(1);
      c.setTuesday(1);
      c.setWednesday(1);
      c.setThursday(1);
      c.setFriday(1);
      dayFrom = Calendar.MONDAY;
      dayTo = Calendar.FRIDAY;
    } else if (scheduleType.equals(SCHEDULE_TYPE_SATURDAY)) {
      c.setSaturday(1);
      dayFrom = dayTo = Calendar.SATURDAY;
    } else if (scheduleType.equals(SCHEDULE_TYPE_SUNDAY)) {
      c.setSunday(1);
      dayFrom = dayTo = Calendar.SUNDAY;
    } else {
      throw new IllegalStateException("unknown schedule type: " + scheduleType);
    }

    _calendars.add(c);

    Calendar cal = Calendar.getInstance();
    cal.setTime(changeDate.getStartDate());

    Set<Date> dates = new HashSet<Date>();

    while (cal.getTime().before(changeDate.getEndDate())) {

      Date day = cal.getTime();
      int dow = cal.get(Calendar.DAY_OF_WEEK);
      cal.add(Calendar.DAY_OF_YEAR, 1);

      // Only consider days that we are already in service
      if (dow < dayFrom || dayTo < dow)
        continue;

      dates.add(day);
    }

    for (TripScheduleModificationStrategy strategy : _modifications) {
      for (Date toAdd : strategy.getAdditions(key, dates)) {
        CalendarDate cd = new CalendarDate();
        cd.setServiceId(serviceId);
        cd.setDate(toAdd);
        cd.setExceptionType(CalendarDate.EXCEPTION_TYPE_ADD);
        addServiceMod(cd);
      }
      for (Date toRemove : strategy.getCancellations(key, dates)) {
        CalendarDate cd = new CalendarDate();
        cd.setServiceId(serviceId);
        cd.setDate(toRemove);
        cd.setExceptionType(CalendarDate.EXCEPTION_TYPE_REMOVE);
        addServiceMod(cd);
      }

      for (ServiceIdModificationImpl mod : strategy.getModifications(key, dates)) {
        mods.add(mod);
        String moddedServiceId = mod.getServiceId(key.getServiceId());
        for (Date toAdd : mod.getDates()) {
          CalendarDate cd = new CalendarDate();
          cd.setServiceId(moddedServiceId);
          cd.setDate(toAdd);
          cd.setExceptionType(CalendarDate.EXCEPTION_TYPE_ADD);
          addServiceMod(cd);
        }
      }
    }

    return mods;
  }

  public void writeCalendars(TranslationContext context) {

    CsvEntityWriter writer = context.getWriter();

    for (ServiceCalendar c : _calendars)
      writer.handleEntity(c);

    for (SortedMap<Date, CalendarDate> m : _serviceMods.values()) {
      for (CalendarDate cd : m.values())
        writer.handleEntity(cd);
    }
  }

  private void addServiceMod(CalendarDate cd) {
    CalendarDate existing = _serviceMods.get(cd.getServiceId()).put(cd.getDate(), cd);
    System.out.println("ADD: serviceId=" + cd.getServiceId() + " ex=" + cd.getExceptionType() + " date=" + cd.getDate() + " " + cd.getDate().getTime());
    if (existing != null && compareTo(existing, cd) != 0)
      throw new IllegalStateException("CalendarDate collision: day=" + cd.getDate() + " existing=" + existing
          + " replacement=" + cd);
  }

  private int compareTo(CalendarDate o1, CalendarDate o2) {

    int rc = 0;

    if ((rc = o1.getServiceId().compareTo(o2.getServiceId())) != 0)
      return rc;

    if ((rc = o1.getDate().compareTo(o2.getDate())) != 0)
      return rc;

    return o1.getExceptionType() == o2.getExceptionType() ? 0
        : (o1.getExceptionType() < o2.getExceptionType() ? -1 : 1);
  }
}
