package org.onebusaway.utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateLibrary {

  public static String getTimeAsIso8601String(Date date) {
    return getTimeAsIso8601String(date, TimeZone.getDefault());
  }

  public static String getTimeAsIso8601String(Date date, TimeZone timeZone) {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    format.setTimeZone(timeZone);
    String timeString = format.format(date);
    return timeString.substring(0, timeString.length() - 2) + ":"
        + timeString.substring(timeString.length() - 2);
  }
}