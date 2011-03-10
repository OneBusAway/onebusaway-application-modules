package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.util.Comparator;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

/**
 * Check two see if arrival and departure times from one block stop time are
 * strictly increasing when compared to another block stop time.
 * 
 * @author bdferris
 */
class BlockStopTimeStrictComparator implements Comparator<BlockStopTimeEntry> {

  @Override
  public int compare(BlockStopTimeEntry o1, BlockStopTimeEntry o2) {

    StopTimeEntry stA = o1.getStopTime();
    StopTimeEntry stB = o2.getStopTime();

    if (stA.getArrivalTime() == stB.getArrivalTime()
        && stA.getDepartureTime() == stB.getDepartureTime()) {
      return 0;
    } else if (stA.getArrivalTime() <= stB.getArrivalTime()
        && stA.getDepartureTime() <= stB.getDepartureTime()) {
      return -1;
    }

    return 1;
  }
}