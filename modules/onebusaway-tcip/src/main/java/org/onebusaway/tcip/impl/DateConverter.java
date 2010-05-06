/**
 * 
 */
package org.onebusaway.tcip.impl;

import com.thoughtworks.xstream.converters.SingleValueConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverter implements SingleValueConverter {

  private static DateFormat _ISO8601Local = new SimpleDateFormat(
      "yyyy-MM-dd");

  public Object fromString(String str) {
    try {
      return _ISO8601Local.parse(str);
    } catch (ParseException e) {
      throw new IllegalStateException(e);
    }
  }

  public String toString(Object obj) {
    Date date = (Date) obj;
    // format in (almost) ISO8601 format
    return _ISO8601Local.format(date);
  }

  @SuppressWarnings("unchecked")
  public boolean canConvert(Class type) {
    return Date.class.equals(type);
  }
}