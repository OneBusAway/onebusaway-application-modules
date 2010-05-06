/**
 * 
 */
package org.onebusaway.metrokc2gtfs.calendar;

import org.onebusaway.metrokc2gtfs.model.MetroKCChangeDate;
import org.onebusaway.metrokc2gtfs.model.MetroKCTrip;

class MetroKCServiceId {

  private final MetroKCChangeDate _changeDate;
  private final String _scheduleType;
  private final String _exceptionCode;

  public MetroKCServiceId(MetroKCChangeDate changeDate, MetroKCTrip trip) {
    this(changeDate, trip.getScheduleType(), trip.getExceptionCode());
  }

  public MetroKCServiceId(MetroKCChangeDate changeDate, String scheduleType, String exceptionCode) {
    _changeDate = changeDate;
    _scheduleType = scheduleType;
    _exceptionCode = exceptionCode == null ? "" : exceptionCode;
  }

  public MetroKCChangeDate getChangeDate() {
    return _changeDate;
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

    return _changeDate.getId() + "-" + scheduleType + exceptionCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof MetroKCServiceId))
      return false;
    MetroKCServiceId key = (MetroKCServiceId) obj;
    return _changeDate.equals(key._changeDate) && _scheduleType.equals(key._scheduleType)
        && _exceptionCode.equals(key._exceptionCode);
  }

  @Override
  public int hashCode() {
    return _changeDate.hashCode() + _scheduleType.hashCode() + _exceptionCode.hashCode();
  }
}