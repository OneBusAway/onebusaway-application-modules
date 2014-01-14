/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.utility;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Contains a number of convenience methods for {@link Date} manipulation
 * 
 * @author bdferris
 */
public class DateLibrary {

  /**
   * @param date a target Date object
   * @return an ISO 8601 string representation of a Date
   */
  public static String getTimeAsIso8601String(Date date) {
    return getTimeAsIso8601String(date, TimeZone.getDefault());
  }

  /**
   * @param date a target Date object
   * @param timeZone the target timezone for the ISO 8601 representation
   * @return an ISO 8601 string representation of a Date
   */
  public static String getTimeAsIso8601String(Date date, TimeZone timeZone) {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    format.setTimeZone(timeZone);
    String timeString = format.format(date);
    return timeString.substring(0, timeString.length() - 2) + ":"
        + timeString.substring(timeString.length() - 2);
  }

  /**
   * 
   * @param value an ISO 8601 string representation fo a Date
   * @return a parsed Date object
   * @throws ParseException if there is an error parsing the string
   */
  public static Date getIso8601StringAsTime(String value) throws ParseException {
    return getIso8601StringAsTime(value, TimeZone.getDefault());
  }

  /**
   * 
   * @param value an ISO 8601 string representation fo a Date
   * @param timeZone the target timezone for the ISO 8601 representation
   * @return a parsed Date object
   * @throws ParseException if there is an error parsing the string
   */
  public static Date getIso8601StringAsTime(String value, TimeZone timeZone)
      throws ParseException {

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    format.setTimeZone(timeZone);

    int n = value.length();

    if (n > 6) {
      char c1 = value.charAt(n - 6);
      char c2 = value.charAt(n - 3);
      if ((c1 == '-' || c1 == '+') && c2 == ':')
        value = value.substring(0, n - 3) + value.substring(n - 2);
    }

    return format.parse(value);
  }

  public static Date getTimeAsDay(Date t) {
    return getTimeAsDay(t.getTime());
  }

  public static Date getTimeAsDay(long t) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(t);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTime();
  }

  public static boolean hasAmPmClock(Locale locale) {
    DateFormat stdFormat = DateFormat.getTimeInstance(DateFormat.SHORT,
        Locale.US);
    DateFormat localeFormat = DateFormat.getTimeInstance(DateFormat.LONG,
        locale);
    String midnight = "";
    try {
      midnight = localeFormat.format(stdFormat.parse("12:00 AM"));
    } catch (ParseException ignore) {
    }
    return midnight.contains("12");
  }
}