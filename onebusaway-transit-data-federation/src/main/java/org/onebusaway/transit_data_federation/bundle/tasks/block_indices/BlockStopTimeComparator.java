package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.util.Comparator;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;

public class BlockStopTimeComparator implements Comparator<BlockStopTimeEntry> {

  @Override
  public int compare(BlockStopTimeEntry o1, BlockStopTimeEntry o2) {

    int at1 = o1.getStopTime().getArrivalTime();
    int at2 = o2.getStopTime().getArrivalTime();

    int c = at1 - at2;

    if (c != 0)
      return c;

    int dt1 = o1.getStopTime().getDepartureTime();
    int dt2 = o2.getStopTime().getDepartureTime();

    return dt1 - dt2;
  }
}
