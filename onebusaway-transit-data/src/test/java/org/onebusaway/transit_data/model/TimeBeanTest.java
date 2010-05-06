package org.onebusaway.transit_data.model;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.onebusaway.transit_data.model.TimeBean;

public class TimeBeanTest {

  /**
   * TODO: This test will likely fail if not run in the Pacific timezone. Could
   * fix that at some point
   */
  @Test
  public void test() throws ParseException {
    
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    Date date = format.parse("2010-01-10 11:24:36 PST");

    TimeBean bean = new TimeBean(date);
    assertEquals(date.getTime(), bean.getTime());
    assertEquals("2010-01-10T11:24:36-08:00", bean.getReadableTime());
  }
}
