package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import java.util.Comparator;
import java.util.List;

import org.onebusaway.transit_data_federation.services.tripplanner.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;

public class BlockFirstTimeComparator implements Comparator<BlockConfigurationEntry> {
  @Override
  public int compare(BlockConfigurationEntry o1, BlockConfigurationEntry o2) {

    List<BlockStopTimeEntry> stopTimes1 = o1.getStopTimes();
    List<BlockStopTimeEntry> stopTimes2 = o2.getStopTimes();

    if (stopTimes1.isEmpty())
      throw new IllegalStateException("block has no stop times: " + o1);
    if (stopTimes2.isEmpty())
      throw new IllegalStateException("block has no stop times: " + o2);

    BlockStopTimeEntry bst1 = stopTimes1.get(0);
    BlockStopTimeEntry bst2 = stopTimes2.get(0);
    
    StopTimeEntry st1 = bst1.getStopTime();
    StopTimeEntry st2 = bst2.getStopTime();

    return st1.getDepartureTime() - st2.getDepartureTime();
  }
}