package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data.model.blocks.BlockBean;
import org.onebusaway.transit_data.model.blocks.BlockTripBean;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

/**
 * Service methods to lookup a {@link BlockBean} representation of a block: a
 * series of contiguous {@link Trip} objects.
 * 
 * @author bdferris
 * @see BlockBean
 * @see Trip
 */
public interface BlockBeanService {

  /**
   * @param block see {@link Trip#getBlockId()}
   * @return retrieve a bean representation of the specified block, or null if
   *         not found
   */
  public BlockBean getBlockForId(AgencyAndId blockId);

  public BlockTripBean getBlockTripAsBean(BlockTripEntry activeTrip);
}
