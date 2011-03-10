/*
 * Copyright 2010 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.utility;

import java.text.SimpleDateFormat;
import java.util.Date;
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
}