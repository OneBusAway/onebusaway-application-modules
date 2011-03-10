package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.util.Comparator;
import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

public class BlockTripLayoverTimeComparator implements
    Comparator<BlockTripEntry> {
  @Override
  public int compare(BlockTripEntry o1, BlockTripEntry o2) {

    int t1 = getLayoverStartTimeForTrip(o1);
    int t2 = getLayoverStartTimeForTrip(o2);

    return t1 - t2;
  }

  public static int getLayoverStartTimeForTrip(BlockTripEntry blockTrip) {
    BlockTripEntry prevTrip = blockTrip.getPreviousTrip();
    if (prevTrip == null)
      throw new IllegalStateException(
          "blockTrip had no incoming trip, thus no layover");
    List<BlockStopTimeEntry> stopTimes = prevTrip.getStopTimes();
    BlockStopTimeEntry blockStopTime = stopTimes.get(stopTimes.size() - 1);
    StopTimeEntry stopTime = blockStopTime.getStopTime();
    return stopTime.getDepartureTime();
  }
  
  public static int getLayoverEndTimeForTrip(BlockTripEntry blockTrip) {
    List<BlockStopTimeEntry> stopTimes = blockTrip.getStopTimes();
    BlockStopTimeEntry blockStopTime = stopTimes.get(0);
    StopTimeEntry stopTime = blockStopTime.getStopTime();
    return stopTime.getArrivalTime();
  }
}