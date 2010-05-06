package org.onebusaway.metrokc2gtfs.calendar;

import java.util.Date;
import java.util.Set;

public interface TripScheduleModificationStrategy {

  public Set<Date> getCancellations(MetroKCServiceId key, Set<Date> dates);

  public Set<Date> getAdditions(MetroKCServiceId key, Set<Date> dates);

  public Set<ServiceIdModificationImpl> getModifications(MetroKCServiceId key, Set<Date> dates);
}
