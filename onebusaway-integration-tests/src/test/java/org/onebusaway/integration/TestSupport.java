package org.onebusaway.integration;

import java.util.Calendar;

public class TestSupport {
  public static int getHourOfDay() {
    Calendar c = Calendar.getInstance();
    return c.get(Calendar.HOUR_OF_DAY);
  }
}
