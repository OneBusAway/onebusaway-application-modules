/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.util;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class OneBusAwayFormats {

  public static DateTimeFormatter DATETIMEPATTERN_JSON_DATE_TIME = ISODateTimeFormat.dateTimeNoMillis();
  
  public static String DATETIMEPATTERN_DATE = "yyyy-MM-dd";

  public static String toCamelCase(String upperCase) {
    if (upperCase == null || upperCase.length() == 0) return upperCase;
    String[] parts = upperCase.split("_");
    StringBuffer camelCase = new StringBuffer();
    for (String part : parts) {
      camelCase.append(part.substring(0,1).toUpperCase());
      camelCase.append(part.substring(1).toLowerCase());
    }
    String result = camelCase.substring(0,1).toLowerCase() + camelCase.substring(1);
    return result;
  }

  public static String toPascalCaseWithSpaces(String upperCase) {
    if (upperCase == null || upperCase.length() == 0) return upperCase;
    String[] parts = upperCase.split("_");
    StringBuffer camelCase = new StringBuffer();
    for (String part : parts) {
      camelCase.append(part.substring(0,1).toUpperCase());
      camelCase.append(part.substring(1).toLowerCase());
      camelCase.append(" ");
    }
    if (camelCase.length() < 2)
      return camelCase.toString();
    String result = camelCase.substring(0, camelCase.length()-1);
    return result;
  }
}
