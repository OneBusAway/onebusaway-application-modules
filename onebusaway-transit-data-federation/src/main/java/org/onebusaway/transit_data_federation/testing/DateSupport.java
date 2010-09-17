package org.onebusaway.transit_data_federation.testing;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateSupport {

  private static final DateFormat _format = new SimpleDateFormat(
      "yyyy-MM-dd HH:mm");

  private static final TimeZone _timeZone = TimeZone.getTimeZone("America/Los_Angeles");

  static {
    _format.setTimeZone(_timeZone);
  }

  public static TimeZone getTimeZone() {
    return _timeZone;
  }

  public static Date date(String source) {
    try {
      return _format.parse(source);
    } catch (ParseException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public static long time(String source) {
    return date(source).getTime();
  }

  public static String format(Date dateA) {
    return _format.format(dateA);
  }

  public static final int hourToSec(double hour) {
    return (int) (hour * 60 * 60);
  }

  public static final int hourAndMinutesToSec(int hour, int minutes) {
    return ((hour * 60) + minutes) * 60;
  }
}
