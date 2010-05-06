package org.onebusaway.gtfs.services.calendar;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.StopTime;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * While the set of {@link ServiceCalendar} and {@link ServiceCalendarDate}
 * entities for a given GTFS feed compactly represent the dates of service for a
 * particular service id, they are not particularly amenable to quick
 * calculations.
 * 
 * The {@link CalendarService} abstracts common date operations into a service
 * interface. The service is typically backed by an efficient data structure
 * that has scanned all the {@link ServiceCalendar} and
 * {@link ServiceCalendarDate} entities into an appropriate representation.
 * 
 * Many of the methods in {@link CalendarService} refer to "service dates". A
 * service date is a particular date when a particular service id is active. The
 * service date is always specified as 12:00 am (aka the start of the day) such
 * that adding the number of seconds from {@link StopTime#getArrivalTime()} or
 * {@link StopTime#getDepartureTime()} will specify the actual time of arrival
 * or departure on that date. Note that service dates are time-zone specific as
 * result.
 * 
 * Note that time interval methods can potentially return multiple service dates
 * for the same service id. Consider, for example, a service id that has stop
 * times that start a 1 am and continue through until 2 am the next day. Thus a
 * query for 1-2 am would return two service dates: one for the previous day's
 * service and one for the current day's service.
 * 
 * @author bdferris
 * 
 */
public interface CalendarService {

  /**
   * @return the set of all service ids used in the data set
   */
  public Set<AgencyAndId> getServiceIds();

  /**
   * @param serviceId the target service id
   * @return the set of all service dates for which the specified service id is
   *         active
   */
  public Set<Date> getServiceDatesForServiceId(AgencyAndId serviceId);

  /**
   * Determine the set of service ids that are active on the specified date. The
   * date parameter need not be a service date (aka specified at midnight).
   * 
   * @param date the target date
   * @return the set of service ids that are active on the specified date
   */
  public Set<AgencyAndId> getServiceIdsOnDate(Date date);

  /**
   * Determine service dates for the specified service ids that have ANY
   * {@link StopTime} with a departure within the specified time interval. Each
   * service id has a time interval over its first and last departure on a given
   * day.
   * 
   * @param serviceIds the set of service ids to consider
   * @param from time interval min
   * @param to time interval max
   * @return the list of active service dates, keyed by service id
   */
  public Map<AgencyAndId, List<Date>> getServiceDateDeparturesWithinRange(
      Set<AgencyAndId> serviceIds, Date from, Date to);

  /**
   * Determine service dates for the specified service ids that have ANY
   * {@link StopTime} with an arrival within the specified time interval. Each
   * service id has a time interval over its first and last arrival on a given
   * day.
   * 
   * @param serviceIds the set of service ids to consider
   * @param from time interval min
   * @param to time interval max
   * @return the list of active service dates, keyed by service id
   */
  public Map<AgencyAndId, List<Date>> getServiceDateArrivalsWithinRange(
      Set<AgencyAndId> serviceIds, Date from, Date to);

  /**
   * Determine service dates for the specified service ids that have ANY
   * {@link StopTime} with an arrival OR departure within the specified time
   * interval. Each service id has a time interval over its first and last
   * arrival on a given day.
   * 
   * @param serviceIds the set of service ids to consider
   * @param from time interval min
   * @param to time interval max
   * @return the list of active service dates, keyed by service id
   */
  public Map<AgencyAndId, List<Date>> getServiceDatesWithinRange(
      Set<AgencyAndId> serviceIds, Date from, Date to);

  /**
   * Determine service ids and service dates for any service id that have ANY
   * {@link StopTime} with an arrival OR departure within the specified time
   * interval. Each service id has a time interval over its first and last
   * arrival on a given day.
   * 
   * @param from time interval min
   * @param to time interval max
   * @return the list of active service dates, keyed by service id
   */
  public Map<AgencyAndId, List<Date>> getServiceDatesWithinRange(Date from,
      Date to);

  /**
   * For each service id, computes the list of service dates whose full service
   * interval (min to max arrival/departure time) overlaps the specified target
   * time plus the next service date whose full service interval comes
   * immediately after (but does overlap) the target time.
   * 
   * This method is useful for finding the next scheduled {@link StopTime} after
   * the target time at a particular stop . By calling this method with the set
   * of service ids for the specified stop, we return the set of service dates
   * that potentially overlap plus the next service date, guaranteeing that at
   * least one service date instantiates a StopTime that occurs after the target
   * time.
   * 
   * This method should be fast, as it will be called frequently in a trip
   * planner graph traversal.
   * 
   * @param serviceIds
   * @param targetTime
   * @return
   */
  public Map<AgencyAndId, List<Date>> getNextDepartureServiceDates(
      Set<AgencyAndId> serviceIds, long targetTime);

  /**
   * For each service id, computes the list of service dates whose full service
   * interval (min to max arrival/departure time) overlaps the specified target
   * time plus the previous service date whose full service interval comes
   * immediately before (but does overlap) the target time.
   * 
   * This method is useful for finding the previous scheduled {@link StopTime}
   * before the target time at a particular stop . By calling this method with
   * the set of service ids for the specified stop, we return the set of service
   * dates that potentially overlap plus the previous service date, guaranteeing
   * that at least one service date instantiates a StopTime that occurs before
   * the target time.
   * 
   * This method should be fast, as it will be called frequently in a trip
   * planner graph traversal.
   * 
   * @param serviceIds
   * @param targetTime
   * @return
   */
  public Map<AgencyAndId, List<Date>> getPreviousArrivalServiceDates(
      Set<AgencyAndId> serviceIds, long targetTime);
}
