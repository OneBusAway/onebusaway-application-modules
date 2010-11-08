package org.onebusaway.transit_data_federation.services.blocks;

import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

public class BlockStopTimeIndex extends AbstractBlockStopTimeIndex<BlockTripIndex> {

  public static BlockStopTimeIndex create(BlockTripIndex blockTripIndex,
      int blockSequence) {

    ServiceInterval serviceInterval = computeServiceInterval(blockTripIndex,
        blockSequence);

    return new BlockStopTimeIndex(blockTripIndex, blockSequence, serviceInterval);
  }

  public int getArrivalTimeForIndex(int index) {
    BlockTripEntry trip = _blockTripIndex.getTrips().get(index);
    return trip.getArrivalTimeForIndex(_stopIndex);
  }

  public int getDepartureTimeForIndex(int index) {
    BlockTripEntry trip = _blockTripIndex.getTrips().get(index);
    return trip.getDepartureTimeForIndex(_stopIndex);
  }
  
  public double getDistanceAlongBlockForIndex(int index) {
    BlockTripEntry trip = _blockTripIndex.getTrips().get(index);
    return trip.getDistanceAlongBlockForIndex(_stopIndex);
  }

  public BlockStopTimeIndex(BlockTripIndex blockIndex, int stopIndex,
      ServiceInterval serviceInterval) {
    super(blockIndex, stopIndex, serviceInterval);
  }
}
