package org.onebusaway.metrokc2gtdf.calendar;

import java.util.Date;

public interface TripScheduleReplacementStrategy {

  public boolean hasReplacement(CalendarKey key, Date date);

  public CalendarKey getReplacement(CalendarKey key, Date date);
}
