package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.blocks.BlockDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripsForAgencyQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;

/**
 * Service methods for querying the real-time status and position of a
 * particular block of trips.
 * 
 * @author bdferris
 * @see StatusBean
 * @see BlockDetailsBean
 */
public interface BlockStatusBeanService {

  /**
   * @param blockId see {@link Trip#getBlockId()}
   * @param serviceDate the service date the block is operating under
   *          (Unix-time)
   * @param time the time of operation to query
   * @param inclusion TODO
   * @return the status info for a particular block operating on the specified
   *         service date and time
   */
  public BlockDetailsBean getBlock(AgencyAndId blockId, long serviceDate,
      long time, TripDetailsInclusionBean inclusion);

  /**
   * 
   * @param vehicleId
   * @param time
   * @param detailsInclusionBean controls what will be included in the
   *          response
   * @return trip details for the trip operated by the specified vehicle at the
   *         specified time, or null if not found
   */
  public BlockDetailsBean getBlockForVehicle(AgencyAndId vehicleId, long time,
      TripDetailsInclusionBean inclusion);

  /**
   * 
   * @param query
   * @return the list of active blocks matching agency query criteria
   */
  public ListBean<BlockDetailsBean> getBlocksForAgency(
      TripsForAgencyQueryBean query);

  /**
   * 
   * @param query
   * @return the list of active blocks matching the route query criteria
   */
  public ListBean<BlockDetailsBean> getBlocksForRoute(
      TripsForRouteQueryBean query);

  /**
   * @param query
   * @return the list of active blocks matching the query criteria
   */
  public ListBean<BlockDetailsBean> getBlocksForBounds(
      TripsForBoundsQueryBean query);
}
