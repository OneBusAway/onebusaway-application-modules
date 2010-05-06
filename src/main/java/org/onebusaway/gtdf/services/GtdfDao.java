package org.onebusaway.gtdf.services;

import edu.washington.cs.rse.collections.stats.IntegerInterval;

import com.vividsolutions.jts.geom.Geometry;

import org.onebusaway.gtdf.model.ServiceCalendar;
import org.onebusaway.gtdf.model.CalendarDate;
import org.onebusaway.gtdf.model.Route;
import org.onebusaway.gtdf.model.ShapePoint;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.gtdf.model.Trip;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GtdfDao {

  public List<Route> getAllRoutes();

  public List<Route> getRoutesByStopId(String id);

  public Route getRouteByShortName(String route);

  public Set<String> getDirectionIdsByRoute(Route route);

  public Stop getStopById(String id);

  public List<Stop> getStopsByLocation(Geometry envelope);

  public List<Stop> getAllStops();

  /**
   * Find the min({@linkplain StopTime#getArrivalTime() arrivalTime}) and max(
   * {@linkplain StopTime#getDepartureTime() departureTime}) for all
   * {@link StopTime} which map to the specified serviceId.
   * 
   * @param serviceId
   * @return the min and max time in seconds from midnight
   */
  public IntegerInterval getPassingTimeIntervalForServiceId(String serviceId);

  public List<ServiceCalendar> getAllCalendars();

  public List<CalendarDate> getAllCalendarDates();

  public List<Trip> getAllTrips();

  public List<Trip> getTripsByRoute(Route route);

  public List<Trip> getTripsByBlockId(Set<String> blockIds);

  public List<String> getServiceIdsByStop(Stop stop);

  public List<StopTime> getAllStopTimes();

  public List<StopTime> getStopTimesByTrip(Trip trip);

  public List<StopTime> getStopTimesByStop(Stop stop);

  public List<StopTime> getStopTimesByStopAndServiceIds(Stop stop,
      Set<String> serviceIds);

  public List<StopTime> getStopTimesByStopAndServiceIdsAndTimeRange(Stop stop,
      Set<String> serviceIds, int timeFrom, int timeTo);

  public Map<Trip, StopTime> getFirstStopTimesByTrips(List<Trip> tripsInBlock);

  public Map<Trip, StopTime> getFirstStopTimesByRoute(Route route);

  public List<ShapePoint> getShapePointsByShapeId(String shapeId);

}
