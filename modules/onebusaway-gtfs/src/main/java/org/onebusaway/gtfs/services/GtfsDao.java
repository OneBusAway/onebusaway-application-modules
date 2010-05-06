package org.onebusaway.gtfs.services;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FareAttribute;
import org.onebusaway.gtfs.model.FareRule;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Transfer;
import org.onebusaway.gtfs.model.Trip;

import java.util.Collection;

/**
 * Basic methods for accessing GTFS entities in bulk or by id.
 * 
 * @author bdferris
 */
public interface GtfsDao {

  /****
   * Agency Methods
   ****/

  public Collection<Agency> getAllAgencies();

  public Agency getAgencyForId(String id);

  /****
   * {@link ServiceCalendar} Methods
   ****/

  public Collection<ServiceCalendar> getAllCalendars();

  public ServiceCalendar getCalendarForId(int id);

  /****
   * {@link ServiceCalendarDate} Methods
   ****/

  public Collection<ServiceCalendarDate> getAllCalendarDates();

  public ServiceCalendarDate getCalendarDateForId(int id);
  
  /****
   * {@link FareAttribute} Methods
   ****/
  
  public Collection<FareAttribute> getAllFareAttributes();
  
  public FareAttribute getFareAttributeForId(AgencyAndId id);
  
  /****
   * {@link FareRule} Methods
   ***/
  
  public Collection<FareRule> getAllFareRules();
  
  public FareRule getFareRuleForId(int id);
  
  /****
   * {@link Frequency} Methods
   ****/
  
  public Collection<Frequency> getAllFrequencies();
  
  public Frequency getFrequencyForId(int id);

  /****
   * {@link Route} Methods
   ****/

  public Collection<Route> getAllRoutes();

  public Route getRouteForId(AgencyAndId id);
  
  /****
   * {@link ShapePoint} Methods
   ****/

  public Collection<ShapePoint> getAllShapePoints();

  public ShapePoint getShapePointForId(int id);

  /****
   * {@link Stop} Methods
   ****/

  public Collection<Stop> getAllStops();

  public Stop getStopForId(AgencyAndId id);

  /****
   * {@link StopTime} Methods
   ****/

  public Collection<StopTime> getAllStopTimes();

  public StopTime getStopTimeForId(int id);
  
  /****
   * {@link Transfer} Methods
   ****/
  
  public Collection<Transfer> getAllTransfers();
  
  public Transfer getTransferForId(int id);
  
  /****
   * {@link Trip} Methods
   ****/

  public Collection<Trip> getAllTrips();

  public Trip getTripForId(AgencyAndId id);


}
