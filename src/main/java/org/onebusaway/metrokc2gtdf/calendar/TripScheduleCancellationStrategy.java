package org.onebusaway.metrokc2gtdf.calendar;

import java.util.Date;

public interface TripScheduleCancellationStrategy {
  public boolean isCancellation(String exceptionCode, Date date);
}
