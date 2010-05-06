package org.onebusaway.tripplanner.services;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface StopTimeIndexContext {

  public Map<String, List<Date>> getNextServiceDates(Set<String> keySet, long targetTime);

  public Map<String, List<Date>> getPreviousServiceDates(Set<String> keySet, long targetTime);
}
