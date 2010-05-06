package org.onebusaway.gtfs.services;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;

import java.util.List;

/**
 * While {@link GtfsDao} has basic methods for retrieving collections of
 * entities and entities by id, {@link GtfsRelationalDao} adds some basic
 * methods for retrieving entities using more complex data relations.
 * 
 * You can imagine many complex queries that you might perform on GTFS data,
 * most of which will not be included here. These are just some basic relational
 * methods that we use to bootstrap other GTFS classes. To add more complex
 * queries, look at the specific mechanisms provided by classes implementing
 * {@link GtfsRelationalDao} and {@link GtfsDao}.
 * 
 * @author bdferris
 */
public interface GtfsRelationalDao extends GtfsDao {

  /****
   * Calendar Methods
   ****/

  /**
   * Find the min and max {@linkplain StopTime#getArrivalTime() arrivalTimes}
   * for all {@link StopTime} which map to the specified serviceId.
   * 
   * @param serviceId
   * @return an interval of the min and max arrival times in seconds from
   *         midnight
   */
  public int[] getArrivalTimeIntervalForServiceId(AgencyAndId serviceId);

  /**
   * Find the min and max {@linkplain StopTime#getDepartureTime()
   * departureTimes} for all {@link StopTime} which map to the specified
   * serviceId.
   * 
   * @param serviceId
   * @return an interval of the min and max departure times in seconds from
   *         midnight
   */
  public int[] getDepartureTimeIntervalForServiceId(AgencyAndId serviceId);

  /****
   * Route Methods
   ****/

  public List<Route> getRoutesForAgency(Agency agency);

  /****
   * {@link Trip} Methods
   ****/

  public List<Trip> getTripsForRoute(Route route);

  /****
   * {@link StopTime} Methods
   ****/

  public List<StopTime> getStopTimesForTrip(Trip trip);

  /****
   * {@link ShapePoint} Methods
   ****/

  public List<ShapePoint> getShapePointsForShapeId(AgencyAndId shapeId);
}
