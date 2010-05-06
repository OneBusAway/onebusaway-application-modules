/*
 * Copyright 2008 Brian Ferris
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
package edu.washington.cs.rse.transit.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.struts2.util.StrutsTypeConverter;

import com.opensymphony.xwork2.conversion.TypeConversionException;

/**
 * 
 */
public class MetroKCTimestampConverter extends StrutsTypeConverter {

  private static SimpleDateFormat _format = new SimpleDateFormat(
      "dd-MMM-yyyy HH:mm:ss");

  private static Date _startEpoch;

  private static Date _endEpoch;

  @SuppressWarnings("unchecked")
  public Object convertFromString(Map context, String[] values, Class toClass) {

    if (values != null && values.length > 0 && values[0] != null
        && values[0].length() > 0) {
      try {

        if (_startEpoch == null)
          _startEpoch = _format.parse("31-JAN-1970 00:00:00");

        if (_endEpoch == null)
          _endEpoch = _format.parse("31-JAN-2020 00:00:00");

        Date d = _format.parse(values[0]);
        if (d.getTime() < _startEpoch.getTime())
          return _startEpoch;
        if (d.getTime() > _endEpoch.getTime())
          return _endEpoch;
        return d;
      } catch (ParseException e) {
        e.printStackTrace();
        throw new TypeConversionException(e);
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public String convertToString(Map context, Object o) {
    if (o instanceof Date)
      return _format.format((Date) o);
    return "";
  }
}
