/**
 * 
 */
package org.onebusaway.tcip.impl;

import com.thoughtworks.xstream.converters.SingleValueConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeConverter implements SingleValueConverter {

  private static DateFormat _ISO8601Local = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ssZ");

  public Object fromString(String str) {
    try {
      str = str.substring(0, 22) + str.substring(23);
      return _ISO8601Local.parse(str);
    } catch (ParseException e) {
      throw new IllegalStateException(e);
    }
  }

  public String toString(Object obj) {
    Date date = (Date) obj;
    // format in (almost) ISO8601 format
    String dateStr = _ISO8601Local.format(date);

    // remap the timezone from 0000 to 00:00 (starts at char 22)
    return dateStr.substring(0, 22) + ":" + dateStr.substring(22);
  }

  @SuppressWarnings("unchecked")
  public boolean canConvert(Class type) {
    return Date.class.equals(type);
  }
}