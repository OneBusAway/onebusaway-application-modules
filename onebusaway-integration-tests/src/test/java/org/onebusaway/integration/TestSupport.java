package org.onebusaway.integration;

import java.util.Calendar;

public class TestSupport {
  
  public static boolean checkArrivalsForRoute15() {
    int hourOfDay = getHourOfDay();
    return 5 <= hourOfDay && hourOfDay < 24;
  }
  
  public static int getHourOfDay() {
    Calendar c = Calendar.getInstance();
    return c.get(Calendar.HOUR_OF_DAY);
  }
}
