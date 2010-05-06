package org.onebusaway.metrokc2gtdf.calendar;

import edu.washington.cs.rse.collections.FactoryMap;

import org.onebusaway.csv.CsvEntityWriter;
import org.onebusaway.gtdf.model.CalendarDate;
import org.onebusaway.metrokc2gtdf.TranslationContext;
import org.onebusaway.metrokc2gtdf.model.MetroKCChangeDate;
import org.onebusaway.metrokc2gtdf.model.MetroKCTrip;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

  private Set<CalendarKey> _serviceKeys = new HashSet<CalendarKey>();

  private List<TripScheduleCancellationStrategy> _cancellations = new ArrayList<TripScheduleCancellationStrategy>();

  private List<TripScheduleReplacementStrategy> _replacements = new ArrayList<TripScheduleReplacementStrategy>();

  public void setCancellationStrategy(TripScheduleCancellationStrategy strategy) {
    _cancellations.add(strategy);
  }

  public void setReplacementStrategy(TripScheduleReplacementStrategy strategy) {
    _replacements.add(strategy);
  }

  public String getServiceIdForTrip(MetroKCChangeDate changeDate,
      MetroKCTrip trip) {
    CalendarKey key = new CalendarKey(changeDate, trip);
    _serviceKeys.add(key);
    return key.getAsServiceId();
  }

  public void writeCalendars(TranslationContext context) {

    CsvEntityWriter writer = context.getWriter();

    Map<CalendarKey, SortedMap<Date, CalendarDate>> serviceMods = new FactoryMap<CalendarKey, SortedMap<Date, CalendarDate>>(
        new TreeMap<Date, CalendarDate>());

    for (CalendarKey key : _serviceKeys) {

      MetroKCChangeDate changeDate = key.getChangeDate();
      String scheduleType = key.getScheduleType();

      org.onebusaway.gtdf.model.ServiceCalendar c = new org.onebusaway.gtdf.model.ServiceCalendar();

      String serviceId = key.getAsServiceId();
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
        throw new IllegalStateException("unknown schedule type: "
            + scheduleType);
      }

      writer.handleEntity(c);

      Calendar cal = Calendar.getInstance();
      cal.setTime(changeDate.getStartDate());

      while (cal.getTime().before(changeDate.getEndDate())) {

        Date day = cal.getTime();
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        // Only consider days that we are already in service
        if (dow < dayFrom || dayTo < dow)
          continue;

        if (hasCancellation(key, day)) {

          CalendarDate cd = new CalendarDate();
          cd.setServiceId(serviceId);
          cd.setDate(day);
          cd.setExceptionType(CalendarDate.EXCEPTION_TYPE_REMOVE);

          CalendarDate existing = serviceMods.get(key).put(day, cd);
          if (existing != null && compareTo(existing, cd) != 0)
            throw new IllegalStateException("CalendarDate collision: day="
                + day + " existing=" + existing + " replacement=" + cd);
        } else {
          for (TripScheduleReplacementStrategy strategy : _replacements) {
            if (strategy.hasReplacement(key, day)) {
              CalendarKey newKey = strategy.getReplacement(key, day);

              CalendarDate cd1 = new CalendarDate();
              cd1.setServiceId(key.getAsServiceId());
              cd1.setDate(day);
              cd1.setExceptionType(CalendarDate.EXCEPTION_TYPE_REMOVE);

              CalendarDate existing = serviceMods.get(key).put(day, cd1);
              if (existing != null && compareTo(existing, cd1) != 0)
                throw new IllegalStateException("CalendarDate collision: day="
                    + day + " existing=" + existing + " replacement=" + cd1);

              CalendarDate cd2 = new CalendarDate();
              cd2.setServiceId(newKey.getAsServiceId());
              cd2.setDate(day);
              cd2.setExceptionType(CalendarDate.EXCEPTION_TYPE_ADD);

              existing = serviceMods.get(newKey).put(day, cd2);
              if (existing != null && compareTo(existing, cd2) != 0)
                throw new IllegalStateException("CalendarDate collision: day="
                    + day + " existing=" + existing + " replacement=" + cd2);

              break;
            }
          }
        }
      }
    }

    // Write the service modifications
    for (SortedMap<Date, CalendarDate> m : serviceMods.values()) {
      for (CalendarDate cd : m.values())
        writer.handleEntity(cd);
    }
  }

  private boolean hasCancellation(CalendarKey key, Date day) {
    for (TripScheduleCancellationStrategy strategy : _cancellations) {
      if (strategy.isCancellation(key.getExceptionCode(), day))
        return true;
    }
    return false;
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
