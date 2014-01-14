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
package org.onebusaway.webapp.gwt.common.control;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.constants.DateTimeConstants;

public class FlexibleDateParser {

  private static LocaleInfo _info = LocaleInfo.getCurrentLocale();

  private static DateTimeConstants _constants = _info.getDateTimeConstants();

  public int getMintuesSinceMidnight(String timeString)
      throws DateParseException {

    String value = timeString.toLowerCase().trim();

    if (value.length() == 0)
      throw new DateParseException();

    value = value.replaceAll("[ :.]+", " ");
    value = value.replaceAll("(\\d)([^0-9 ])", "$1 $2");
    value = value.replaceAll("([^0-9 ])(\\d)", "$1 $2");
    String[] tokens = value.split(" ");

    if (tokens.length == 0)
      throw new DateParseException();

    try {

      String hoursToken = tokens[0];

      // Check for 24 hour time
      if (tokens.length == 1 && hoursToken.length() == 3
          || hoursToken.length() == 4) {
        if (hoursToken.length() == 3)
          hoursToken = "0" + hoursToken;
        String hourPart = hoursToken.substring(0, 2);
        String minutePart = hoursToken.substring(2);
        return Integer.parseInt(hourPart) * 60 + Integer.parseInt(minutePart);
      }

      int hours = Integer.parseInt(tokens[0]);

      if (tokens.length == 1) {
        return hours * 60;
      }

      int minutes = 0;

      int nextIndex = 1;

      if (tokens[nextIndex].matches("^\\d+$")) {
        minutes = Integer.parseInt(tokens[nextIndex]);
        nextIndex++;
      }

      if (nextIndex >= tokens.length)
        return hours * 60 + minutes;

      StringBuilder b = new StringBuilder();
      for (int i = nextIndex; i < tokens.length; i++)
        b.append(tokens[i]);

      String r = b.toString();

      String[] ampms = _constants.ampms();

      Map<String, Boolean> names = new HashMap<String, Boolean>();
      names.put("am", Boolean.FALSE);
      names.put("pm", Boolean.TRUE);
      names.put(cleanAmPmString(ampms[0]), Boolean.FALSE);
      names.put(cleanAmPmString(ampms[1]), Boolean.TRUE);

      String bestMatch = null;
      int bestEditDistance = -1;

      if (names.containsKey(r)) {
        bestMatch = r;
        bestEditDistance = 0;
      } else {
        for (String name : names.keySet()) {
          int d = StringEditDistance.getEditDistance(name, r);
          if (bestEditDistance == -1 || bestEditDistance > d) {
            bestMatch = name;
            bestEditDistance = d;
          }
        }
      }

      boolean isPm = names.get(bestMatch);

      if (isPm)
        hours += 12;
      else if (hours == 12)
        hours = 0;

      return hours * 60 + minutes;

    } catch (NumberFormatException ex) {
      throw new DateParseException();
    }
  }

  private String cleanAmPmString(String ampm) {
    return ampm.toLowerCase().replaceAll("\\.", "");
  }

  public static class DateParseException extends Exception {

    private static final long serialVersionUID = 1L;

  }

}
