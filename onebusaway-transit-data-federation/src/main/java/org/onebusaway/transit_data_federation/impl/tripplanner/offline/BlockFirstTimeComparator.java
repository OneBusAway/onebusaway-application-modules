package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import java.util.Comparator;
import java.util.List;

import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;

public class BlockFirstTimeComparator implements Comparator<BlockEntry> {
  @Override
  public int compare(BlockEntry o1, BlockEntry o2) {

    List<StopTimeEntry> stopTimes1 = o1.getStopTimes();
    List<StopTimeEntry> stopTimes2 = o2.getStopTimes();

    if (stopTimes1.isEmpty())
      throw new IllegalStateException("block has no stop times: " + o1);
    if (stopTimes2.isEmpty())
      throw new IllegalStateException("block has no stop times: " + o2);

    StopTimeEntry st1 = stopTimes1.get(0);
    StopTimeEntry st2 = stopTimes2.get(0);

    return st1.getDepartureTime() - st2.getDepartureTime();
  }
}