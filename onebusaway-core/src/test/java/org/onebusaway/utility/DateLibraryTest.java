package org.onebusaway.utility;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

public class DateLibraryTest {

  @Test
  public void test() throws ParseException {
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    Date date = format.parse("2010-01-10 11:24:36 PST");
    TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
    String string = DateLibrary.getTimeAsIso8601String(date, tz);
    assertEquals("2010-01-10T11:24:36-08:00", string);
  }
}
