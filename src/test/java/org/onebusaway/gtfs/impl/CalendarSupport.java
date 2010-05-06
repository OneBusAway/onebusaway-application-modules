package org.onebusaway.gtfs.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class CalendarSupport {

  private static DateFormat _dayFormat = DateFormat.getDateInstance(DateFormat.SHORT);

  private static DateFormat _timeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

  public static final int hourToSec(int hour) {
    return hour * 60 * 60;
  }

  public static final Date day(String spec) {
    try {
      return _dayFormat.parse(spec);
    } catch (ParseException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public static final Date time(String spec) {
    try {
      return _timeFormat.parse(spec);
    } catch (ParseException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
