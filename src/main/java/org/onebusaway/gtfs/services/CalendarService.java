package org.onebusaway.gtfs.services;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CalendarService {

  /**
   * Determine service dates for the specified service ids that have departures
   * within the specified time interval. Each service id has a time interval
   * over its first and last departure on a given day.
   * 
   * @param serviceIds
   * @param from
   * @param to
   * @return
   */
  public Map<String, List<Date>> getServiceDateDeparturesWithinRange(Set<String> serviceIds, Date from, Date to);
  
  public Map<String, List<Date>> getServiceDateArrivalsWithinRange(Set<String> serviceIds, Date from, Date to);
  
  public Map<String, List<Date>> getServiceDatesWithinRange(Set<String> serviceIds, Date from, Date to);

  public Set<String> getServiceIdsOnDate(Date date);

  public Set<Date> getDatesForServiceId(String serviceId);

  public Map<String, List<Date>> getNextDepartureServiceDates(Set<String> serviceIds, long targetTime);

  public Map<String, List<Date>> getPreviousArrivalServiceDates(Set<String> serviceIds, long targetTime);
}
