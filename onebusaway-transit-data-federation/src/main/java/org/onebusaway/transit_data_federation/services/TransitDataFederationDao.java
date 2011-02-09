package org.onebusaway.transit_data_federation.services;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.transit_data_federation.model.RouteCollection;

/**
 * Data access operations for transit data federation bundle data. For the most
 * part, that means methods for working with {@link RouteCollection} objects.
 * 
 * @author bdferris
 * 
 */
public interface TransitDataFederationDao {

  /**
   * @return all the route collection objects in the bundle
   */
  public List<RouteCollection> getAllRouteCollections();

  /**
   * @param id the route collection id
   * @return the route collection with the specified id
   */
  public RouteCollection getRouteCollectionForId(AgencyAndId id);

  /**
   * @param stop
   * @return the set of all route collections serving the specified stop
   */
  public List<RouteCollection> getRouteCollectionsForStop(Stop stop);

  /**
   * @param serviceId a {@link ServiceCalendar} or {@link ServiceCalendarDate}
   *          service id
   * @return the set of all route collections operating in the specified
   *         serviceId
   */
  public List<AgencyAndId> getRouteCollectionIdsForServiceId(
      AgencyAndId serviceId);

  /**
   * @param serviceId a {@link ServiceCalendar} or {@link ServiceCalendarDate}
   *          service id
   * @param routeCollectionId the target route collection id
   * @return the set of trips servicing the specified route collection and
   *         operating on the specified service id
   */
  public List<AgencyAndId> getTripIdsForServiceIdAndRouteCollectionId(
      AgencyAndId serviceId, AgencyAndId routeCollectionId);

  /**
   * @param route the target route
   * @return the route collection containing the specified route
   */
  public RouteCollection getRouteCollectionForRoute(Route route);

  /**
   * @param stop
   * @return the set of all shape ids for trips serving the specified stop
   */
  public List<AgencyAndId> getShapeIdsForStop(Stop stop);
}
