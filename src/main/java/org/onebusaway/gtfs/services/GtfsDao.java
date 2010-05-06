package org.onebusaway.gtfs.services;

import org.onebusaway.gtfs.model.CalendarDate;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;

import edu.washington.cs.rse.collections.stats.IntegerInterval;
import edu.washington.cs.rse.collections.tuple.T2;
import edu.washington.cs.rse.collections.tuple.T3;

import com.vividsolutions.jts.geom.Geometry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GtfsDao {

  public List<Route> getAllRoutes();

  public List<Route> getRoutesByStopId(String id);

  public Route getRouteById(String id);

  public Route getRouteByShortName(String route);

  public List<Route> getRoutesByShortNames(Set<String> routeShortNames);

  public List<Route> getRoutesByLocation(final Geometry envelope);

  public Map<Integer, String[]> getRouteShortNamesByStopTimeIds(Set<Integer> ids);

  public Map<Stop, List<T2<Route, String>>> getRoutesAndDirectionIdsForStops(Collection<Stop> stops);

  public List<T3<Route, String, Stop>> getRoutesDirectionIdsAndStopsByLocation(final Geometry envelope);

  public Set<String> getDirectionIdsByRoute(Route route);

  public Stop getStopById(String id);

  public List<Stop> getStopsByIds(Collection<String> ids);

  public List<Stop> getStopsByLocation(Geometry envelope);

  public List<Stop> getStopsByLocation(Geometry envelope, int resultLimit);

  public List<Stop> getAllStops();

  /**
   * Find the min and max {@linkplain StopTime#getArrivalTime() arrivalTimes}
   * for all {@link StopTime} which map to the specified serviceId.
   * 
   * @param serviceId
   * @return an interval of the min and max arrival times in seconds from
   *         midnight
   */
  public IntegerInterval getArrivalTimeIntervalForServiceId(String serviceId);

  /**
   * Find the min and max {@linkplain StopTime#getDepartureTime()
   * departureTimes} for all {@link StopTime} which map to the specified
   * serviceId.
   * 
   * @param serviceId
   * @return an interval of the min and max departure times in seconds from
   *         midnight
   */
  public IntegerInterval getDepartureTimeIntervalForServiceId(String serviceId);

  public List<ServiceCalendar> getAllCalendars();

  public List<CalendarDate> getAllCalendarDates();

  public List<Trip> getAllTrips();

  public Trip getTripById(String id);

  public List<Trip> getTripsByIds(Collection<String> id);

  public List<Trip> getTripsByRoute(Route route);

  public List<String> getAllBlockIds();

  public List<Trip> getTripsByBlockId(String blockIds);

  public List<Trip> getTripsByBlockId(Set<String> blockIds);

  public List<String> getServiceIdsByStop(Stop stop);

  public Trip getPreviousTripInBlock(Trip trip);

  public Trip getNextTripInBlock(Trip trip);

  /*****************************************************************************
   * Stop Times Methods
   ****************************************************************************/

  public StopTime getStopTimeById(Integer id);

  public List<StopTime> getAllStopTimes();

  public List<StopTime> getStopTimesByIds(Collection<Integer> ids);

  public List<StopTime> getStopTimesByTrip(Trip trip);

  public List<StopTime> getStopTimesByBlockId(String blockId);

  public List<StopTime> getStopTimesByStop(Stop stop);

  public List<StopTime> getStopTimesByStopAndServiceIds(Stop stop, Set<String> serviceIds);

  public List<StopTime> getStopTimesByStopAndServiceIdsAndTimeRange(Stop stop, Set<String> serviceIds, int timeFrom,
      int timeTo);

  public Map<Trip, StopTime> getFirstStopTimesByTrips(Collection<Trip> tripsInBlock);

  public Map<Trip, StopTime> getLastStopTimesByTrips(Collection<Trip> tripsInBlock);

  public Map<Trip, StopTime> getFirstStopTimesByRoute(Route route);

  public List<ShapePoint> getShapePointsByShapeId(String shapeId);

  public List<ShapePoint> getShapePointsByShapeIds(Set<String> shapeId);

  public List<ShapePoint> getShapePointsByShapeIdAndDistanceRange(String shapeId, double distanceFrom, double distanceTo);

  public List<ShapePoint> getShapePointsByShapeIdAndDistanceFrom(String shapeId, double distanceFrom);

  public List<ShapePoint> getShapePointsByShapeIdAndDistanceTo(String shapeId, double distanceTo);

}
