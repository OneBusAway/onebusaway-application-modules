package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.util.Comparator;
import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

/**
 * Check two see if all arrival and departure times from one block trip are
 * strictly increasing when compared to another block trip.
 * 
 * @author bdferris
 */
class BlockTripStrictComparator implements Comparator<BlockTripEntry> {

  private static final BlockStopTimeStrictComparator _c = new BlockStopTimeStrictComparator();

  @Override
  public int compare(BlockTripEntry a, BlockTripEntry b) {

    List<BlockStopTimeEntry> stopTimesA = a.getStopTimes();
    List<BlockStopTimeEntry> stopTimesB = b.getStopTimes();

    boolean allEqual = true;

    for (int i = 0; i < stopTimesA.size(); i++) {
      int c = _c.compare(stopTimesA.get(i), stopTimesB.get(i));
      if (c > 0)
        return c;
      if (c < 0)
        allEqual = false;
    }

    if (allEqual)
      return 0;

    return -1;
  }

}