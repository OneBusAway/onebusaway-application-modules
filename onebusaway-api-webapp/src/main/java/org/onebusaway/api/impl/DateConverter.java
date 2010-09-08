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
package org.onebusaway.api.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.struts2.util.StrutsTypeConverter;

import com.opensymphony.xwork2.conversion.TypeConversionException;

/**
 * 
 */
public class DateConverter extends StrutsTypeConverter {

  private static SimpleDateFormat _format = new SimpleDateFormat("yyyy-MM-dd");

  @SuppressWarnings("rawtypes")
  public Object convertFromString(Map context, String[] values, Class toClass) {

    if (values != null && values.length > 0 && values[0] != null
        && values[0].length() > 0) {

      String value = values[0];

      if (value.matches("^(\\d+)$"))
        return new Date(Long.parseLong(value));

      try {
        return _format.parse(value);
      } catch (ParseException e) {
        e.printStackTrace();
        throw new TypeConversionException(e);
      }
    }
    return null;
  }

  @SuppressWarnings("rawtypes")
  public String convertToString(Map context, Object o) {
    if (o instanceof Date)
      return _format.format((Date) o);
    return "";
  }
}
