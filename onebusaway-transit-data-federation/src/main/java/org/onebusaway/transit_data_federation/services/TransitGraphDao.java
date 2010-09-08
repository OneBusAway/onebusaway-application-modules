package org.onebusaway.transit_data_federation.services;

import java.util.List;
import java.util.Set;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;

/**
 * Service interface that abstract operations on a transit graph, such as access
 * to stops and trips, away from the underlying graph implementation.
 * 
 * @author bdferris
 * @see StopEntry
 * @see TripEntry
 */
public interface TransitGraphDao {

  /**
   * @return the list of all stop entries in the transit graph
   */
  public Iterable<StopEntry> getAllStops();

  /**
   * @param id a stop id to query
   * @return the stop entry with the specified id, or null if not found
   */
  public StopEntry getStopEntryForId(AgencyAndId id);

  /**
   * @param bounds coordinate bounds query
   * @return a list of stop entries located within in the specified bounds
   */
  public List<StopEntry> getStopsByLocation(CoordinateBounds bounds);

  /**
   * @return the list of all block entries in the transit graph
   */
  public Iterable<BlockEntry> getAllBlocks();

  /**
   * @param blockId a block id to query
   * @return the block entry with the specified id, or null if not found
   */
  public BlockEntry getBlockEntryForId(AgencyAndId blockId);

  /**
   * @return the list of all trip entries in the transit graph
   */
  public Iterable<TripEntry> getAllTrips();

  /**
   * @param id a trip id to query
   * @return the trip entry with the specified id, or null if not found
   */
  public TripEntry getTripEntryForId(AgencyAndId id);

  /**
   * @param blockId a trip sequence block id to query
   * @return the list of all trip entries with the specified block id
   */
  public List<TripEntry> getTripsForBlockId(AgencyAndId blockId);

  /**
   * @param stopId a stop id to query
   * @return the set of all {@link RouteCollection} ids serving the specified
   *         stop
   */
  public Set<AgencyAndId> getRouteCollectionIdsForStop(AgencyAndId stopId);
  
  public List<BlockEntry> getBlocksForRouteCollectionId(AgencyAndId routeCollectionId);
}
