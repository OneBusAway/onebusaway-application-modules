package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.geospatial.model.CoordinateBounds;

public interface BlockGeospatialService {

  /**
   * Determines the set of currently active blocks (according to scheduled
   * times) that pass through the specified bounds. We currently implement this
   * by looking for stops in the region, so you might miss a block if it passes
   * through but does not stop in a particular bounds.
   * 
   * @param bounds the region
   * @param timeFrom
   * @param timeTo
   * @return the list of SCHEDULED block instances in the given region along
   *         with their scheduled locations
   */
  public List<BlockInstance> getActiveScheduledBlocksPassingThroughBounds(
      CoordinateBounds bounds, long timeFrom, long timeTo);
}
