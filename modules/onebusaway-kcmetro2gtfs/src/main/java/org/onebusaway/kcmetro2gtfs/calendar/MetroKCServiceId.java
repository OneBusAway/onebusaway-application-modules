/**
 * 
 */
package org.onebusaway.kcmetro2gtfs.calendar;

import org.onebusaway.kcmetro2gtfs.model.MetroKCChangeDate;
import org.onebusaway.kcmetro2gtfs.model.MetroKCTrip;

import edu.washington.cs.rse.collections.CollectionsLibrary;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class MetroKCServiceId {

  private final List<MetroKCChangeDate> _changeDates;
  private final String _scheduleType;
  private final String _exceptionCode;
  private final Set<String> _changeDateIds = new HashSet<String>();

  public MetroKCServiceId(List<MetroKCChangeDate> changeDates, MetroKCTrip trip) {
    this(changeDates, trip.getScheduleType(), trip.getExceptionCode());
  }

  public MetroKCServiceId(List<MetroKCChangeDate> changeDates,
      String scheduleType, String exceptionCode) {
    _changeDates = changeDates;
    _scheduleType = scheduleType;
    _exceptionCode = exceptionCode == null ? "" : exceptionCode;
    for( MetroKCChangeDate changeDate : changeDates)
      _changeDateIds.add(changeDate.getId());
  }

  public List<MetroKCChangeDate> getChangeDates() {
    return _changeDates;
  }
  
  public Set<String> getChangeDateIds() {
    return _changeDateIds;
  }

  public String getScheduleType() {
    return _scheduleType;
  }

  public String getExceptionCode() {
    return _exceptionCode;
  }

  public String getServiceId() {

    String scheduleType = _scheduleType;
    if (scheduleType.equals(CalendarManager.SCHEDULE_TYPE_WEEKDAY))
      scheduleType = "WEEK";
    else if (scheduleType.equals(CalendarManager.SCHEDULE_TYPE_SATURDAY))
      scheduleType = "SAT";
    else if (scheduleType.equals(CalendarManager.SCHEDULE_TYPE_SUNDAY))
      scheduleType = "SUN";

    String exceptionCode = _exceptionCode.trim();
    if (exceptionCode.length() > 0)
      exceptionCode = "-" + exceptionCode;

    String changeDateCombinedId = CollectionsLibrary.getObjectsAsSortedString(
        _changeDateIds, "-");

    return changeDateCombinedId + "-" + scheduleType + exceptionCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof MetroKCServiceId))
      return false;
    MetroKCServiceId key = (MetroKCServiceId) obj;
    return _changeDates.equals(key._changeDates)
        && _scheduleType.equals(key._scheduleType)
        && _exceptionCode.equals(key._exceptionCode);
  }

  @Override
  public int hashCode() {
    return _changeDates.hashCode() + _scheduleType.hashCode()
        + _exceptionCode.hashCode();
  }
}