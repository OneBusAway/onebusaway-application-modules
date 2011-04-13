package org.onebusaway.transit_data_federation.services.transit_graph;

import java.util.List;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;

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
  public List<StopEntry> getAllStops();
  
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
  public List<BlockEntry> getAllBlocks();

  /**
   * @param blockId a block id to query
   * @return the block entry with the specified id, or null if not found
   */
  public BlockEntry getBlockEntryForId(AgencyAndId blockId);

  /**
   * @return the list of all trip entries in the transit graph
   */
  public List<TripEntry> getAllTrips();

  /**
   * @param id a trip id to query
   * @return the trip entry with the specified id, or null if not found
   */
  public TripEntry getTripEntryForId(AgencyAndId id);
}
